package ro.readme.rotex.sources

import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import ro.readme.rotex.retrySocketTimeoutException
import ro.readme.rotex.skipFileIfExists
import ro.readme.rotex.utils.PathUtils
import ro.readme.rotex.utils.TextCleaner
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.stream.Stream

class CertitudineaSource: Source {
    override val sourceKey = "certitudinea"
    override val originalLink = "http://www.certitudinea.ro"
    override val downloadLink = ""

    override fun downloadOriginal(override: Boolean) {
        val start = LocalDate.parse("2013-01-01")
        val end = LocalDate.now()

        Stream.iterate(start) { date -> date.plusMonths(1) }
            .limit(ChronoUnit.MONTHS.between(start, end) + 1)
            .forEach { date ->
                println(date)
                val month = date.monthValue.toString().padStart(2, '0')
                val datePageDoc = Jsoup
                    .connect("http://www.certitudinea.ro/arhiva/$month/${date.year}/articole")
                    .timeout(150 * 1000)
                    .get()

                val pageLinks = datePageDoc.select(".pagination .nav a")
                for (pageLink in pageLinks) {
                    val pageHref = pageLink.attr("href")
                    println("Page $pageHref")

                    val pageDoc = Jsoup
                        .connect(pageHref)
                        .timeout(150 * 1000)
                        .get()

                    val links = pageDoc.select(".pagini_row .row_title a")
                    for (link in links) {
                        writeArticle(date, link, override)
                    }
                }
            }
    }

    private fun writeArticle(date: LocalDate, link: Element, override: Boolean) {
        val href = link.attr("href")
        val name = href.substringAfterLast("/")
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
                val articleTitle = articleDoc.select(".pagina_continut h1").text()

                val content = articleDoc.select(".pagina_continut .continut")

                content.select("p:contains(if gte mso)").remove()
                content.select("strong:contains(Afisari)").remove()

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
