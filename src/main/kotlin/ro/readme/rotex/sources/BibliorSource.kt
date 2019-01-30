package ro.readme.rotex.sources

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import ro.readme.rotex.*
import ro.readme.rotex.utils.PathUtils
import ro.readme.rotex.utils.TextCleaner
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.nio.file.Paths

class BibliorSource: Source {
    override val sourceKey = "biblior"
    override val originalLink = "http://biblior.net/carti"
    override val downloadLink = "https://drive.google.com/open?id=1nPpJdXbCAL13a2yIM9PgJiIXxZeRzGDF"

    override fun downloadOriginal(override: Boolean) {
        val linksPageDoc = Jsoup.connect("http://biblior.net/carti").get()
        val lettersLinks = linksPageDoc.select("#main .attachment-before a")
        for (letterLink in lettersLinks) {
            println(letterLink.text())
            val letterHref = letterLink.attr("href")
            val letterDoc = Jsoup.connect("http://biblior.net$letterHref").get()

            val booksLinks = letterDoc.select("#main .view-content tr a")
            for (bookLink in booksLinks) {
                writeBook(override, bookLink)
            }
        }
    }

    private fun writeBook(override: Boolean, bookLink: Element) {
        val bookHref = bookLink.attr("href")
        val bookTitle = bookLink.text()

        val bookDoc = Jsoup.connect("http://biblior.net$bookHref").timeout(15 * 1000).get()
        val authorText = bookDoc.select("#content .content>p>strong").text()
        val author = if (authorText.isBlank()) "Multiple Authors" else authorText
        println("\t$author - $bookTitle")
        val fileName = TextCleaner("$author - $bookTitle").stripForSafeFileName().cleaned
        val destinationPath = PathUtils.originalFilePath(
            sourceKey,
            "$fileName.html"
        )
        skipFileIfExists(destinationPath, override) {
            val destinationFile = destinationPath.toFile()
            FileWriter(destinationFile).use {
                BufferedWriter(it).use { fileBufferedWriter ->

                    fileBufferedWriter.write(
                        """
                        <html>
                            <body>
                                <section title="$bookTitle" author="$author">
                    """.trimIndent()
                    )

                    val chaptersLinks = bookDoc.select(".book-navigation li a")

                    if (null != chaptersLinks && chaptersLinks.size > 0) {
                        for (chapterLink in chaptersLinks) {
                            writeChapter(fileBufferedWriter, chapterLink)
                        }
                    } else {
                        writeChapter(fileBufferedWriter, bookLink)
                    }

                    fileBufferedWriter.write(
                        """
                                </section>
                            </body>
                        </html>
                    """.trimIndent()
                    )
                }
            }

            println("\tOK")
        }
    }

    private fun writeChapter(fileBufferedWriter: BufferedWriter, chapterLink: Element) {
        val chapterTitle = chapterLink.text()
        println("\t\t$chapterTitle")

        val chapterHref = chapterLink.attr("href")

        val chapterDoc = Jsoup.connect("http://biblior.net$chapterHref").timeout(15 * 1000).get()

        writeChapterContent(chapterDoc, fileBufferedWriter)

        val otherPages = chapterDoc.select("ul.pager li a")

        for (otherPage in otherPages) {
            val pagerText = otherPage.text()
            if (pagerText.matches(Regex("\\d+"))) {
                val pageHref = otherPage.attr("href")
                val otherChapterDoc = Jsoup.connect("http://biblior.net$pageHref").timeout(15 * 1000).get()
                writeChapterContent(otherChapterDoc, fileBufferedWriter)
            }
        }
    }

    private fun writeChapterContent(chapterDoc: Document, fileBufferedWriter: BufferedWriter) {
        val title = chapterDoc.select("#main h1.title").text()
        val chapter = chapterDoc.select("#content .content")
        chapter.select(".fb-social-like-widget").remove()
        fileBufferedWriter.write(
            """
            <article title="$title">
                ${chapter.html()}
            </article>
            """
        )
    }

    private fun cleanText(text: String): String {
        return TextCleaner(Jsoup.parse(text).text())
            .correctCedilaDiacritics()
            .cleaned
            .trimStart()
    }

    override fun extractText(override: Boolean) {
        val outputPath =
            PathUtils.textFilePath(sourceKey)
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
                        .filter { it.isFile }
                        .filter { it.name.endsWith(".html") }
                        .filter { !it.name.contains("James Samuelson - Roumania. Past and Present") }
                        .filter { !it.name.contains("Th. Hebbelynck - Le Tour du Monde - En Roumanie") }
                        .filter { !it.name.contains("D. Mitrany  - Rumania Her History and Politics") }
                        .filter { !it.name.contains("Multiple Authors - Roumanian Fairy Tales") }
                        .forEach { file ->
                            println(file.name)
                            val doc = Jsoup.parse(file, Charsets.UTF_8.name(), "")
                            val sections = doc.select("section")
                            for (section in sections) {
                                val title = section.attr("title")
                                val author = section.attr("author").replace("Multiple Authors", "Mai mulți autori")
                                printWriter.println(title)
                                printWriter.println(author)
                                printWriter.println()
                                val articles = section.select("article")
                                for (article in articles) {
                                    val articleTitle = article.attr("title")
                                    printWriter.println(articleTitle)
                                    printWriter.println()

                                    val paragraphs = article.select("p")
                                    for (paragraph in paragraphs) {
                                        val text = paragraph.html()
                                            .replace(Regex("<br /> +"), "\n")
                                        val cleanedText = cleanText(text)
                                        printWriter.println(cleanedText)
                                    }
                                }
                                printWriter.println()
                            }
                            printWriter.println()
                            printWriter.println()
                        }
                }
        }
    }
}
