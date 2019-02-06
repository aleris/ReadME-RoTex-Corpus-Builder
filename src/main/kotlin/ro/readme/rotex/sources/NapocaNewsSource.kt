package ro.readme.rotex.sources

import org.jsoup.HttpStatusException
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import ro.readme.rotex.retrySocketTimeoutException
import ro.readme.rotex.skipFileIfExists
import ro.readme.rotex.utils.PathUtils
import ro.readme.rotex.utils.TextCleaner
import java.io.File
import java.io.PrintWriter
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.stream.Stream

class NapocaNewsSource: Source() {
    override val sourceKey = "napoca-news"
    override val originalLink = "http://www.napocanews.ro/"
    override val downloadLink = "https://drive.google.com/open?id=18GDMbRrGAVoykMajbuGvDLWgpbult8_p"

    override fun downloadOriginal(override: Boolean) {
        var page = 1
        while (true) {
            println("Page $page")

            try {
                val pageDoc = Jsoup
                    .connect("http://www.napocanews.ro/page/$page")
                    .timeout(150 * 1000)
                    .get()

                val links = pageDoc.select("#masonry .post .panel-body a")
                for (link in links) {
                    val href = link.attr("href")
                    if (href.endsWith(".html")) {
                        writeArticle(href, override)
                    }
                }

                page++
            } catch (e: HttpStatusException) {
                if (e.statusCode == 404) {
                    break
                }
                println("ERR")
                e.printStackTrace()
            }
        }
    }

    private fun writeArticle(href: String, override: Boolean) {
        val name = href.substringAfterLast("/")
        print("$name ... ")
        val nameWithExtension = if (name.endsWith(".html")) name else "$name.html"
        val outputPath = PathUtils.originalFilePath(sourceKey, nameWithExtension)

        skipFileIfExists(outputPath, override) {
            retrySocketTimeoutException(3) {
                val articleDoc = Jsoup.connect(href).timeout(150 * 1000).get()
                val articleTitle = articleDoc.select(".post h1").text()

                val content = articleDoc.select(".post .post-content")

                content.select(".really_simple_share").remove()
                content.select(".really_simple_share_clearfix").remove()
                content.select("#comments").remove()
                content.select(".post-meta-top").remove()
                content.select("#navigation").remove()

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
