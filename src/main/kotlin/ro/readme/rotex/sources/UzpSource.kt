package ro.readme.rotex.sources

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import ro.readme.rotex.retrySocketTimeoutException
import ro.readme.rotex.skipFileIfExists
import ro.readme.rotex.utils.PathUtils
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.stream.Stream

class UzpSource: Source() {
    override val sourceKey = "uzp"
    override val originalLink = "https://uzp.org.ro"
    override val downloadLink = "https://drive.google.com/open?id=1ST5FJ7AzUk-94cdT4tBQvCOhCyJ26HOn"

    override fun downloadOriginal(override: Boolean) {
        val start = LocalDate.parse("2017-08-01")
        val end = LocalDate.now()

        Stream.iterate(start) { date -> date.plusMonths(1) }
            .limit(ChronoUnit.MONTHS.between(start, end) + 1)
            .forEach { date ->
                println(date)
                val month = date.monthValue.toString().padStart(2, '0')
                val datePageDoc = Jsoup
                    .connect("https://uzp.org.ro/date/${date.year}/$month")
                    .timeout(150 * 1000)
                    .get()

                val pageCount = getPageCount(datePageDoc)
                for (page in 1..pageCount) {
                    println("Page $page")

                    val pageDoc = Jsoup
                        .connect("https://uzp.org.ro/date/${date.year}/$month/page/$page/")
                        .timeout(150 * 1000)
                        .get()

                    val links = pageDoc.select(".blog-item-holder .blog-title a")
                    for (link in links) {
                        writeArticle(date, link, override)
                    }
                }
            }
    }


    private fun getPageCount(datePageDoc: Document): Int {
        val lastPageLink = datePageDoc.select(".gdl-pagination a").last()
        return if (null != lastPageLink) {
            lastPageLink
                .attr("href")
                .substringBeforeLast("/")
                .substringAfterLast("/")
                .toInt()
        } else {
            1
        }
    }

    private fun writeArticle(date: LocalDate, link: Element, override: Boolean) {
        val href = link.attr("href")
        val name = href.substringBeforeLast("/").substringAfterLast("/")
        print("$name ... ")
        val outputPath = PathUtils.originalFilePath(
            sourceKey,
            date.year.toString(),
            date.monthValue.toString().padStart(2, '0'),
            "$name.html"
        )

        skipFileIfExists(outputPath, override) {
            retrySocketTimeoutException(3) {
                val articleDoc = Jsoup.connect(href).timeout(150 * 1000).get()
                val articleTitle = articleDoc.select(".blog-title").text()

                val content = articleDoc.select(".blog-content")

                content.select(".blog-info-wrapper").remove()

                if (null != content) {
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
                                printWriter.println(it.text())
                            }
                            printWriter.println()
                            println("OK")
                        }
                }
        }
    }
}
