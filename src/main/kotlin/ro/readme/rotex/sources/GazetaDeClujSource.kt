package ro.readme.rotex.sources

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import ro.readme.rotex.retrySocketTimeoutException
import ro.readme.rotex.skipFileIfExists
import ro.readme.rotex.utils.PathUtils
import ro.readme.rotex.utils.TextCleaner
import java.io.File

class GazetaDeClujSource: Source() {
    override val sourceKey = "gazeta-de-cluj"
    override val originalLink = "https://gazetadecluj.ro/stiri/stiri-cluj"
    override val downloadLink = ""

    override fun downloadOriginal(override: Boolean) {
        val categories = arrayOf(
            "stiri-cluj",
            "stiri-investigatii",
            "stiri-justitie",
            "stiri-politica",
            "administratie",
            "stiri-economic",
            "societate",
            "externe"
        )
        for (category in categories) {
            println(category)
            val categoryFirstPageDoc = Jsoup
                .connect("https://gazetadecluj.ro/stiri/$category")
                .timeout(60*1000)
                .get()
            val pageCount = getPageCount(categoryFirstPageDoc)
            for (page in 1..pageCount) {
                println("$category, page $page of $pageCount")
                retrySocketTimeoutException(3) {
                    val pageHref = "https://gazetadecluj.ro/stiri/$category/page/$page/"
                    val pageDoc = Jsoup.connect(pageHref).get()
                    val links = pageDoc.select(".td-ss-main-content .entry-title a")
                    for (link in links) {
                        downloadArticle(override, link)
                    }
                }
            }
        }
    }

    private fun downloadArticle(override: Boolean, link: Element) {
        val href = link.attr("href")
        val doc = Jsoup
            .connect(href)
            .timeout(60*1000)
            .get()
        val name = href.substringBeforeLast("/").substringAfterLast("/")
        val date = doc.select(".entry-date").attr("datetime")
        val dateParts = date.substring(0, 8).split("-")
        val year = dateParts[0]
        val month = dateParts[1]
        val filePath = PathUtils.originalFilePath(sourceKey, year, month, "$name.html")
        skipFileIfExists(filePath, override) {
            retrySocketTimeoutException(3) {
                try {
                    print("$name ... ")
                    val title = doc.select(".entry-title").text()
                    val author = doc.select(".td-post-author-name a").text()

                    doc.select(".td-post-sharing-top").remove()
                    doc.select("script").remove()

                    val html = doc.select(".td-post-content").html()
                    val article = """
                    <html>
                        <body>
                            <article title="$title" author="$author" date="$date">
                                $html
                            </article>
                        </body>
                    </html>
                """.trimIndent()
                    filePath.toFile().writeText(article)
                    println("OK")
                } catch (e: Exception) {
                    println("ERR")
                    e.printStackTrace()
                }
            }
        }
    }

    private fun getPageCount(pageDoc: Document): Int {
        val t = pageDoc.select(".pages").text()
        val prefix = "din "
        val i = t.indexOf(prefix)
        val pt = t.substring(i + prefix.length)
        return pt.toInt()
    }

    override fun extractText(override: Boolean) {
        val outputPath = PathUtils.textFilePath(sourceKey)
        skipFileIfExists(outputPath, override) {
            File(outputPath.toUri())
                .printWriter().use { printWriter ->
                    val inputDirectoryPath = PathUtils.originalDirectoryPath(sourceKey)
                    File(inputDirectoryPath.toUri())
                        .walk()
                        .filter { it.isFile }
                        .filter { it.name.endsWith(".html") }
                        .forEach { file ->
                            print("${file.name} ... ")
                            val doc = Jsoup.parse(file, Charsets.UTF_8.name(), "")
                            val articles = doc.select("article")
                            articles.forEach { article ->
                                val title = article.attr("title")
                                printWriter.println(title)
                                val author = article.attr("author")
                                printWriter.println(author)
                                article.children().forEach {
                                    val cleaned =
                                        TextCleaner(it.text()).correctCedilaDiacritics()
                                            .cleaned
                                    printWriter.println(cleaned)
                                }
                            }
                            println("OK")
                        }
                }
        }
    }
}
