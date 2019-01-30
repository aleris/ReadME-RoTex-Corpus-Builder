package ro.readme.rotex.sources

import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import ro.readme.rotex.*
import ro.readme.rotex.utils.PathUtils
import ro.readme.rotex.utils.TextCleaner
import ro.readme.rotex.utils.TextUtils
import java.io.File
import java.nio.file.Paths

class DezbateriParlamentareSource: Source {
    override val sourceKey = "dezbateri-parlamentare"
    override val originalLink = "http://www.cdep.ro/pls/steno/steno.home?idl=1"
    override val downloadLink = "https://drive.google.com/open?id=1QDZfoV_ftVKRTPEDpkr3grcxYDnX_ULU"

    override fun downloadOriginal(override: Boolean) {
        downloadCam(override, 2, 1996, 2018)
        downloadCam(override, 1, 2002, 2010)
        downloadCam(override, 0, 1996, 2018)
    }

    private fun downloadCam(override: Boolean, cam: Int, yearStart: Int, yearEnd: Int) {
        val camName = getCamName(cam)
        println("Cam $camName")
        for (year in yearStart..yearEnd) {
            downloadYear(override, cam, camName, year)
        }
    }

    private fun downloadYear(override: Boolean, cam: Int, camName: String, year: Int) {
        println("Year $camName-$year")
        try {
            val yearDoc = Jsoup.connect("http://www.cdep.ro/pls/steno/steno.calendar?cam=$cam&an=$year&idl=1").get()
            val months = yearDoc.select("#pageContent center table td")
            for (month in months) {
                val monthName = month.select("pre font:first-of-type").text()
                println("Month $camName-$year-$monthName")
                val dayLinks = month.select("a")
                for (dayLink in dayLinks) {
                    val day = dayLink.text()
                    println("Day $camName-$year-$monthName-$day")
                    downloadDay(dayLink, override, camName, year, monthName, day)
                }
                println()
            }
        } catch (e: Exception) {
            println("!ERR")
            println(e.message)
            e.printStackTrace()
        }
    }

    private fun downloadDay(
        dayLink: Element,
        override: Boolean,
        camName: String,
        year: Int,
        monthName: String,
        day: String
    ) {
        try {
            val month = TextUtils.getMonthAsTowDigitNumberString(monthName)
            val fullName = "$camName-$year-$month-${day.padStart(2, '0')}"
            val outputPath = PathUtils.originalFilePath(sourceKey, camName, year.toString(), month, "$fullName.html")
            skipFileIfExists(outputPath, override) {
                retrySocketTimeoutException(3) {
                    val summaryDoc = Jsoup.connect("http://www.cdep.ro/" + dayLink.attr("href")).get()
                    val sectionLinks = summaryDoc.select("#pageContent table b a")
                    val html = StringBuilder()
                    html.append(
                        """
                            <html lang="ro">
                                <body>
                        """.trimIndent()
                    )
                    for (sectionLink in sectionLinks) {
                        val sectionHtml = getDownloadedContentSectionHtml(sectionLink)
                        html.append(sectionHtml)
                    }
                    html.append(
                        """
                            </body>
                        </html>
                        """.trimIndent()
                    )
                    File(outputPath.toUri()).writeText(html.toString())
                }
            }
        } catch (e: Exception) {
            println("!ERR")
            println(e.message)
            e.printStackTrace()
        }
    }

    private fun getDownloadedContentSectionHtml(sectionLink: Element): String {
        val section = TextCleaner(sectionLink.text()).stripForSafeFileName().cleaned
        val contentDoc = Jsoup.connect("http://www.cdep.ro/" + sectionLink.attr("href")).get()
        val pageContent = contentDoc.getElementById("pageContent")
        val html = """
            <section name="$section">
                ${pageContent.html()}
            </section>
        """.trimIndent()
        return html
    }

    private fun getCamName(cam: Int): String =
        when (cam) {
            2 -> "deputies"
            1 -> "senate"
            0 -> "common"
            else -> "unknown"
        }

    override fun extractText(override: Boolean) {
        val outputPath = PathUtils.textFilePath(sourceKey)
        skipFileIfExists(outputPath, override) {
            File(outputPath.toUri())
                .printWriter().use { printWriter ->
                    val inputDirectoryPath = Paths.get(
                        ConfigProperties.dataDirectoryPath,
                        ConfigProperties.originalDirectoryName,
                        sourceKey
                    )
                    File(inputDirectoryPath.toUri())
                        .walk()
                        .sorted()
                        .filter { it.isFile }
                        .filter { it.name.endsWith(".html") }
                        .forEach { file ->
                            println(file.name)
                            val doc = Jsoup.parse(file, Charsets.UTF_8.name(), "")
                            val textSections = doc.select(".headline,.headlinetext1,.textn")
                            textSections
                                .map { it.text().trim() }
                                .filter { !it.isEmpty() }
                                .map { TextCleaner(it).correctCedilaDiacritics().cleaned }
                                .filter { !it.matches(Regex("\\.+")) }
                                .forEach {
                                    if (it.startsWith("Domnul") || it.startsWith("Doamna")) {
                                        val withName = it.split(':', ignoreCase = false, limit = 2)
                                        printWriter.print(withName[0])
                                        printWriter.println(":")
                                        if (1 < withName.size && !withName[1].isBlank()) {
                                            printWriter.println(withName[1].trim())
                                        }
                                    } else {
                                        printWriter.println(it)
                                    }
                                    printWriter.println()
                                }

                            printWriter.println()
                        }
                }
        }
    }
}
