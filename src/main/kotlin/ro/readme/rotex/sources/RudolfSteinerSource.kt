package ro.readme.rotex.sources

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import ro.readme.rotex.skipFileIfExists
import ro.readme.rotex.utils.PathUtils
import ro.readme.rotex.utils.TextCleaner
import java.io.File
import java.io.PrintWriter

class RudolfSteinerSource: Source() {
    override val sourceKey = "rudolf-steiner"
    override val originalLink = "http://www.spiritualrs.net/Lucrari_GA.html"
    override val downloadLink = "https://drive.google.com/open?id=1vbVfDe2Dv774kOyl18AYxw7zn5-_SXFb"

    override fun downloadOriginal(override: Boolean) {
        val titleSet = HashSet<String>()
        val linksPageDoc = Jsoup.connect("http://www.spiritualrs.net/Lucrari_GA.html").get()
        val rows = linksPageDoc.select("#lucrari tr")
        for (row in rows) {
            val link = row.select("a")
            if (null != link) {
                val linkText = link.text()
                if (linkText.contains("online")) {
                    val title = row.select("td b").text()
                    if (title.isNotBlank() && !titleSet.contains(title)) {
                        titleSet.add(title)
                        print("$title ... ")
                        val fileName = TextCleaner(title).stripForSafeFileName().cleaned
                        val destinationFilePath = PathUtils.originalFilePath(sourceKey, "$fileName.html")
                        skipFileIfExists(destinationFilePath, override) {
                            File(destinationFilePath.toUri())
                                .printWriter().use { printWriter ->
                                    printWriter.println(
                                        """
                                            <html>
                                                <body>
                                        """.trimIndent())
                                    writeBook(printWriter, link.last())
                                    printWriter.println("""
                                                </body>
                                            </html
                                        """.trimIndent())
                                }
                        }
                        println("OK")
                    }
                }
            }
        }
    }

    private fun writeBook(printWriter: PrintWriter, link: Element) {
        val href = link.attr("href")
        val bookDoc = Jsoup
            .connect("http://www.spiritualrs.net/$href")
            .timeout(15 * 1000)
            .get()
        val coverBody = getSectionBody(bookDoc)
        printWriter.println(coverBody)

        val bookIndex = href.substringBeforeLast('/').substringAfterLast('/')
        val gaIndex = if (bookIndex.contains("-")) bookIndex.substringBefore("-") else bookIndex

        val chapterLinks = bookDoc.select("section a")
        for (chapterLink in chapterLinks) {
            val chapterHref = chapterLink.attr("href")
            if (chapterHref.contains("${gaIndex}_")) {
                val chapterDoc =
                    Jsoup
                        .connect("http://www.spiritualrs.net/Conferinte/$bookIndex/$chapterHref")
                        .timeout(15 * 1000)
                        .get()

                val sectionBody = getSectionBody(chapterDoc)
                printWriter.println(sectionBody)
            }
        }

//                val elements = bookDoc.select("body p, body h1, body h2, body h3")
//
//                var add = false
//                val coverSet = HashSet<String>()
//                for (element in elements) {
//                    if (element.text().contains("Rudolf Steiner")) {
//                        add = true
//                    }
//                    if (element.attr("class") == "navbar") {
//                        add = false
//                    }
//                    if (add) {
//                        val coverLine = cleanText(element.text()).trim()
//                        if (!coverSet.contains(coverLine)) {
//                            fileBufferedWriter.write(coverLine)
//                            fileBufferedWriter.write("\n")
//                            coverSet.add(coverLine)
//                        }
//                    }
//                }
//                fileBufferedWriter.write("\n")
//                fileBufferedWriter.write("\n")
//
//                val bookIndex = href.substringBeforeLast('/').substringAfterLast('/')
//                var gaIndex = if (bookIndex.contains("-")) bookIndex.substringBefore("-") else bookIndex
//
//                val chapterLinks = bookDoc.select("body a")
//                for (chapterLink in chapterLinks) {
//                    val chapterHref = chapterLink.attr("href")
//                    if (chapterHref.contains("${gaIndex}_")) {
//                        printLines("\t${chapterLink.text()}")
//                        try {
//                            val chapterDoc = Jsoup.connect("http://www.spiritualrs.net/Conferinte/$bookIndex/$chapterHref").timeout(15 * 1000).get()
//
//                            val chapterTitle = chapterDoc.select("body h3").text()
//                            if (null != chapterTitle) {
//                                fileBufferedWriter.write("\n")
//                                fileBufferedWriter.write("\n")
//                                fileBufferedWriter.write(cleanText(chapterTitle))
//                                fileBufferedWriter.write("\n")
//                                fileBufferedWriter.write("\n")
//                            }
//
//                            val chapterLines = chapterDoc.select("body p")
//                            for (line in chapterLines) {
//                                fileBufferedWriter.write(cleanText(line.text()))
//                                fileBufferedWriter.write("\n")
//                            }
//                            println(" OK")
//                        } catch (ex: Exception) {
//                            println(" !err")
//                            println(ex.message)
//                            ex.printStackTrace()
//                        }
//                    }
//                }
//
//                fileBufferedWriter.write("\n")
//                fileBufferedWriter.write("\n")

            }

    private fun getSectionBody(bookDoc: Document): Element {
        val body = bookDoc.select("body").first()
        try {
            body.select("table").first().remove()
        } catch (e: Exception) {}
        try {
            body.select("hr").first().remove()
        } catch (e: Exception) {}
        try {
            body.select(".navbar").first().remove()
        } catch (e: Exception) {}
        try {
            body.select("hr").last().remove()
        } catch (e: Exception) {}
        try {
            body.tagName("section")
        } catch (e: Exception) {}
        return body
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
                            val articles = doc.select("section")
                            articles.forEach { section ->
                                section.children().forEach {
                                    printWriter.println(it.text())
                                }
                            }
                            println("OK")
                        }
                }
        }
    }
}
