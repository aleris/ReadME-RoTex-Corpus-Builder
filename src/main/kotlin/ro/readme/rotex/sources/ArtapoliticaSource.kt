package ro.readme.rotex.sources

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import ro.readme.rotex.retrySocketTimeoutException
import ro.readme.rotex.skipFileIfExists
import ro.readme.rotex.utils.PathUtils
import ro.readme.rotex.utils.TextCleaner

class ArtapoliticaSource: Source() {
    override val sourceKey = "artapolitica"
    override val originalLink = "http://www.tion.ro/date/2019"
    override val downloadLink = "https://drive.google.com/open?id=1RdJpRz88tlvEb6f5gBL5ovEWf54dU2W2"

    override fun downloadOriginal(override: Boolean) {
        val mainDoc = Jsoup
            .connect("http://artapolitica.ro")
            .timeout(150 * 1000)
            .get()

        val pageCount = getPageCount(mainDoc)
        for (page in 1..pageCount) {
            println("Page $page of $pageCount")

            val pageDoc = Jsoup
                .connect("http://artapolitica.ro/page/$page/")
                .timeout(150 * 1000)
                .get()

            val links = pageDoc.select("#main a")
            for (link in links) {
                val href = link.attr("href")
                if (href.matches(Regex("http://artapolitica.ro/\\d{4}/\\d{2}/\\d{2}/.+"))) {
                    writeArticle(href, override)
                }
            }
        }
    }

    private fun getPageCount(yearDoc: Document): Int {
        val pageLinks = yearDoc.select(".page-numbers a")
        return pageLinks.elementAt(pageLinks.size - 2).text().toInt()
    }


    private fun writeArticle(href: String, override: Boolean) {
        val name = href.substringBeforeLast("/").substringAfterLast("/")
        print("$name ... ")
        val nameWithExtension = "$name.html"
        val dateParts = href
            .substringAfter("http://artapolitica.ro/")
            .substringBeforeLast("/")
            .substringBeforeLast("/")
            .split("/")
        val outputPath = PathUtils.originalFilePath(sourceKey, dateParts[0], dateParts[1], dateParts[2], nameWithExtension)

        skipFileIfExists(outputPath, override) {
            retrySocketTimeoutException(3) {
                val articleDoc = Jsoup.connect(href).timeout(150 * 1000).get()
                val articleTitle = articleDoc.select(".post-title").text()
                val content = articleDoc.select(".inner-post-entry")

                content.select(".fb-like").remove()
                content.select(".hatom-extra").remove()
                content.select(".post-tags").remove()
                content.select("style").remove()

                if (null != content) {
                    val html = content.html()
                    if (html.isNotBlank()) {
                        outputPath.toFile().writeText(
                            """
                            <html>
                                <body>
                                    <article title="$articleTitle">
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
                } else {
                    println("NOT OK: no content")
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
                            printWriter.println(title)
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
