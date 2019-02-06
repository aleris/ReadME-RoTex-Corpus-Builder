package ro.readme.rotex.sources

import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import ro.readme.rotex.retrySocketTimeoutException
import ro.readme.rotex.skipFileIfExists
import ro.readme.rotex.utils.PathUtils
import ro.readme.rotex.utils.TextCleaner

class JurisprudentaSource: Source() {
    override val sourceKey = "jurisprudenta"
    override val originalLink = "https://www.jurisprudenta.com/jurisprudenta/"
    override val downloadLink = ""

    override fun downloadOriginal(override: Boolean) {
        var page = 1
        while (true) {
            println("Page $page")

            val pageDoc = Jsoup
                .connect("https://www.jurisprudenta.com/jurisprudenta/?pagina=$page")
                .timeout(150 * 1000)
                .get()

            val links = pageDoc.select(".list-group a")

            if (0 == links.size) {
                break
            }

            for (link in links) {
                writeArticle(link, override)
            }

            page++
        }
    }

    private fun writeArticle(link: Element, override: Boolean) {
        val href = link.attr("href")
        val dateParts = link.select("h4").text().substringAfterLast(" - ").split(".")
        val name = href.substringBeforeLast("/").substringAfterLast("/")
        print("$name ... ")
        val outputPath = PathUtils.originalFilePath(
            sourceKey,
            dateParts[2],
            dateParts[1],
            dateParts[0],
            "$name.html"
        )

        skipFileIfExists(outputPath, override) {
            retrySocketTimeoutException(3) {
                val articleDoc = Jsoup.connect("https://www.jurisprudenta.com$href").timeout(150 * 1000).get()
                val articleTitle = articleDoc.select(".container h1").text()

                val firstRow = articleDoc.select(".container .row").first()

                firstRow.select(".pad5px").first().remove()
                firstRow.select(".ap_container").remove()

                val content = firstRow.select(".row .col-md-8").first()
                outputPath.toFile().writeText("""
                        <html>
                            <body>
                                <article title="$articleTitle">
                                    ${content.html()}
                                </article>
                            </body>
                        </html
                    """.trimIndent()
                )
                println("OK")
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
//                            val title = article.attr("title")
//                            printWriter.println(title)
                            article.select(".pj").forEach {
                                val cleaned =
                                    TextCleaner(it.text()).correctHtmlEntitiesDiacritics().correctCedilaDiacritics()
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
