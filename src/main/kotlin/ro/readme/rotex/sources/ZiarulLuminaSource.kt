package ro.readme.rotex.sources

import org.jsoup.Jsoup
import ro.readme.rotex.retrySocketTimeoutException
import ro.readme.rotex.skipFileIfExists
import ro.readme.rotex.utils.PathUtils
import java.io.File
import java.io.PrintWriter
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.stream.Stream

class ZiarulLuminaSource: Source() {
    override val sourceKey = "ziarul-lumina"
    override val originalLink = "http://ziarullumina.ro"
    override val downloadLink = "https://drive.google.com/open?id=1mpg7qaLH1__XWTzLUELO3LqmBrUZqNzs"

    override fun downloadOriginal(override: Boolean) {
        val start = LocalDate.parse("2007-05-28")
        val end = LocalDate.now()

        Stream.iterate(start) { date -> date.plusDays(1) }
            .limit(ChronoUnit.DAYS.between(start, end) + 1)
            .forEach { date ->
                println(date)
                val datePageDoc = Jsoup.connect("http://ziarullumina.ro/editie/$date").timeout(150 * 1000).get()

                val outputPath = PathUtils.originalFilePath(
                    sourceKey,
                    date.year.toString(),
                    date.month.value.toString().padStart(2, '0'),
                    "$date.html")

                skipFileIfExists(outputPath, override) {
                    File(outputPath.toUri())
                        .printWriter().use { printWriter ->
                            printWriter.println("""
                                <html>
                                    <body>
                            """.trimIndent())
                            val articlesLinks = datePageDoc.select(".container .content a")

                            for (articleLink in articlesLinks) {
                                val articleHref = articleLink.attr("href")
                                val articleName = articleHref.substringAfterLast("/").substringBeforeLast(".html")
                                if (articleName != "-") {
                                    writeArticle(printWriter, articleHref, articleName)
                                }
                            }
                            printWriter.println("""
                                    </body>
                                </html
                            """.trimIndent())
                        }
                }
            }
    }

    private fun writeArticle(printWriter: PrintWriter, articleHref: String, articleName: String) {
        print("\t$articleName ... ")
        retrySocketTimeoutException(3) {
            val articleDoc = Jsoup.connect(articleHref).timeout(150 * 1000).get()
            val articleTitle = articleDoc.select(".container .art_content_left h1").text()
            val author = articleDoc.select(".container .art_content_left .added a").text()

            val txtNode = articleDoc.select(".container .art_content_left .txt").first()

            if (null != txtNode) {
                printWriter.println("\t\t\t<article title=\"$articleTitle\" author=\"$author\">")
                printWriter.println(txtNode.html())

                printWriter.println("\t\t\t</article>")
                printWriter.println()
                println(" OK")
            } else {
                println(" NOT OK: no txt node")
            }

        }
    }

    override fun extractText(override: Boolean) {
        val outputPath = PathUtils.textFilePath(sourceKey)
        skipFileIfExists(outputPath, override) {
            File(outputPath.toUri())
                .printWriter().use { printWriter ->
                    val inputDirectoryPath = PathUtils.originalDirectoryPath(sourceKey)
                    File(inputDirectoryPath.toUri())
                        .walk()
                        .sorted()
                        .filter { it.isFile }
                        .filter { it.name.endsWith(".html") }
                        .forEach { file ->
                            print("${file.name} ... ")
                            printWriter.println(file.nameWithoutExtension)
                            val doc = Jsoup.parse(file, Charsets.UTF_8.name(), "")
                            val articles = doc.select("article")
                            articles.forEach { article ->
                                val title = article.attr("title")
                                printWriter.println(title)
                                val author = article.attr("author")
                                printWriter.println(author)
                                article.children().forEach {
                                    printWriter.println(it.text())
                                }
                            }
                            println("OK")
                        }
                }
        }
    }
}
