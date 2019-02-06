package ro.readme.rotex.sources

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import ro.readme.rotex.retrySocketTimeoutException
import ro.readme.rotex.skipFileIfExists
import ro.readme.rotex.utils.PathUtils
import ro.readme.rotex.utils.TextCleaner

class TionSource: Source() {
    override val enabled = false
    override val sourceKey = "tion"
    override val originalLink = "http://www.tion.ro/date/2019"
    override val downloadLink = ""

    override fun downloadOriginal(override: Boolean) {
        val archiveDoc = Jsoup
            .connect("http://www.tion.ro/date/2005")
            .timeout(150 * 1000)
            .get()

        val yearsLinks = archiveDoc.select("#nnp_archives-4 ul").first().select("a")
        for (yearLink in yearsLinks) {
            val year = yearLink.text()
            println("Year $year")

            val yearDoc = Jsoup
                .connect(yearLink.attr("href"))
                .timeout(150 * 1000)
                .get()

            val pageCount = getPageCount(yearDoc)
            for (page in 1..pageCount) {
                println("Page $page of $pageCount")

                val pageDoc = Jsoup
                    .connect("http://www.tion.ro/date/$year/$page")
                    .timeout(150 * 1000)
                    .get()

                val links = pageDoc.select(".news-index .article h2 a,.NewsIndex .Article h3 a,.NewsIndex .Article h2 a")
                for (link in links) {
                    val href = link.attr("href")
                    writeArticle(year, href, override)
                }
            }
        }
    }

    private fun getPageCount(yearDoc: Document) = try {
        yearDoc.select(".Direct a,.direct a").last().text().toInt()
    } catch (e: Exception) {
        1
    }

    private fun writeArticle(year: String, href: String, override: Boolean) {
        var name = href.substringAfterLast("/")
        if (name.matches(Regex("\\d+"))) {
            name = href.substringBeforeLast("/").substringAfterLast("/")
        }
        print("$name ... ")
        val nameWithExtension = "$name.html"
        val outputPath = PathUtils.originalFilePath(sourceKey, year, nameWithExtension)

        skipFileIfExists(outputPath, override) {
            retrySocketTimeoutException(3) {
                try {
                    val articleDoc = Jsoup.connect(href).timeout(150 * 1000).get()
                    val articleTitle = articleDoc.select(".news-detail .article-title").text()
                    val authorLink = articleDoc.select(".author-div a")
                    val author = if (null == authorLink) "" else authorLink.text()
                    val content = articleDoc.select(".news-detail .article")

                    content.select(".scl-bar").remove()
                    content.select(".tags").remove()

                    val html = content.html()
                    if (html.isNotBlank()) {
                        outputPath.toFile().writeText(
                            """
                        <html>
                            <body>
                                <article title="$articleTitle" author="$author">
                                    $html
                                </article>
                            </body>
                        </html
                    """.trimIndent()
                        )
                        println("OK")
                    } else {
                        println("empty text, skipping")
                    }
                } catch (e: Exception) {
                    println("ERR")
                    e.printStackTrace()
                }
            }
        }
    }

    override fun extractText(override: Boolean) {
        val outputPath = PathUtils.textFilePath(sourceKey)
        skipFileIfExists(outputPath, override) {
            outputPath
                .toFile()
                .printWriter()
                .use { printWriter ->
                    val inputDirectoryPath = PathUtils.originalDirectoryPath(sourceKey)
                    inputDirectoryPath
                        .toFile()
                        .walk()
                        .sorted()
                        .filter { it.isFile }
                        .filter { it.name.endsWith(".html") }
                        .forEach { file ->
                            print("${file.name} ... ")
//                            printWriter.println(file.nameWithoutExtension)
                            val doc = Jsoup.parse(file, Charsets.UTF_8.name(), "")
                            val article = doc.select("article").first()
                            val title = article.attr("title")
                            val author = article.attr("author")
                            printWriter.println(title)
                            printWriter.println(author)
                            article.children().forEach {
                                val cleaned =
                                    TextCleaner(it.text()).correctCedilaDiacritics()
                                        .cleaned
                                printWriter.println(cleaned)
                            }
                            printWriter.println()
                            println("OK")
                        }
                }
        }
    }
}
