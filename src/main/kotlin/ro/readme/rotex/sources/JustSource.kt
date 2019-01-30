package ro.readme.rotex.sources

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import ro.readme.rotex.retrySocketTimeoutException
import ro.readme.rotex.skipFileIfExists
import ro.readme.rotex.utils.PathUtils
import ro.readme.rotex.utils.TextCleaner
import ro.readme.rotex.utils.TextUtils
import java.io.File

class JustSource: Source {
    override val sourceKey = "just"
    override val originalLink = "http://legislatie.just.ro/Public/RezultateCautare?page=1"
    override val downloadLink = "https://drive.google.com/open?id=1o89XyGGpHeif3eRycfT_SZYHdUpautBO"

    override fun downloadOriginal(override: Boolean) = runBlocking {
        for (page in 1..17630) {
            launch(Dispatchers.IO) {
                println("Page $page ... ")
                retrySocketTimeoutException(5) {
                    val pageIndexDoc = Jsoup
                        .connect("http://legislatie.just.ro/Public/RezultateCautare?page=$page")
                        .timeout(60 * 1000)
                        .get()
                    val articles = pageIndexDoc.select(".search_result_page .search_result_item")
                    for (article in articles) {
                        val dateParts = getDateParts(article)
                        val docLink = article.select("a").first()
                        val name = docLink.text()
                        println(name)
                        val href = docLink.attr("href")
                        val id = href.substringAfterLast("/")
                        val fileName = TextCleaner("$name-$id.html").stripForSafeFileName().cleaned
                        val filePath =
                            PathUtils.originalFilePath(sourceKey, dateParts[0], dateParts[1], dateParts[2], fileName)
                        skipFileIfExists(filePath, override) {
                            val doc = Jsoup
                                .connect("http://legislatie.just.ro/$href")
                                .timeout(60 * 1000)
                                .get()
                            val html = doc.select("#forme_act_container").html()
                            filePath.toFile().writeText(
                                """
                                <html>
                                    <body>
                                        $html
                                    </body>
                                </html>
                            """.trimIndent()
                            )
                        }
                    }
                    println("Page $page DONE")
                }
            }
        }
    }

    private fun getDateParts(article: Element): Array<String> {
        val prefix = "Data intrarii in vigoare: "
        for (c in article.children()) {
            val text = c.text()
            val i = text.indexOf(prefix)
            if (-1 != i) {
                val dateString = text.substring(prefix.length).trim()
                val parts = dateString.split(" ")
                val date = parts[0]
                val month = TextUtils.getMonthAsTowDigitNumberString(parts[1])
                val year = parts[2]
                return arrayOf(year, month, date)
            }
        }
        return arrayOf("0", "0", "0")
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
                            val doc = Jsoup.parse(file, Charsets.UTF_8.name(), "")
                            val text = doc.select(".S_DEN,.S_PAR,.S_EMT_TTL,.S_EMT_BDY,.S_PUB_TTL,.S_PUB_BDY,.S_ART_TTL")
                                .joinToString("\n") { it.text() }
                            val cleaned = TextCleaner(text).correctCedilaDiacritics().cleaned
                            printWriter.println(cleaned)
                            printWriter.println()
                            println("OK")
                        }
                }
        }
    }
}
