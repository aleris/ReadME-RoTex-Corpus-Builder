package ro.readme.rotex.sources

import org.apache.pdfbox.io.MemoryUsageSetting
import org.apache.pdfbox.multipdf.PDFMergerUtility
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.jsoup.Connection
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import ro.readme.rotex.ConfigProperties
import ro.readme.rotex.retrySocketTimeoutException
import ro.readme.rotex.skipFileIfExists
import ro.readme.rotex.utils.PathUtils
import ro.readme.rotex.utils.PdfTextExtractor
import ro.readme.rotex.utils.TextCleaner
import java.io.File
import java.io.FileWriter
import java.nio.file.Paths
import java.util.ArrayList
import java.util.HashMap

class BibliotecaDigitalaAseSource: Source() {
    override val sourceKey = "biblioteca-digitala-ase"
    override val originalLink = "http://www.biblioteca-digitala.ase.ro/biblioteca"
    override val downloadLink = "https://drive.google.com/open?id=1VPg0vrflrCQWS6coytOZR2LndjXroFQM"

    override fun downloadOriginal(override: Boolean) {
        for (letter in 'A'..'Z') {
            print(letter)
            val linksPageDoc = Jsoup
                .connect("http://www.biblioteca-digitala.ase.ro/biblioteca/model/carte1.asp?titlu=$letter")
                .timeout(15 * 1000)
                .get()
            val bookLinks = linksPageDoc.select("a")
            for (bookLink in bookLinks) {
                val bookHref = bookLink.attr("href")
                if (bookHref.contains("carte2")) {
                    writeBook(override, bookLink)
                }
            }
        }
    }

    private fun writeBook(override: Boolean, bookLink: Element) {
        val bookHref = bookLink.attr("href")
        val bookTitle = bookLink.text()

        print("\t$bookTitle")

        val fileName = TextCleaner(bookTitle).stripForSafeFileName().cleaned
        val destinationFilePath = PathUtils.originalFilePath(sourceKey, "$fileName.pdf")
        skipFileIfExists(destinationFilePath, override) {

            val bookId = bookHref.substringAfterLast("id=").substringBeforeLast("&")

            val cookies = HashMap<String, String>()

            val bookConnection =
                Jsoup.connect("http://www.biblioteca-digitala.ase.ro/biblioteca/carte2.asp?id=$bookId&idb=")
                    .timeout(15 * 1000)
                    .method(Connection.Method.GET)
            val bookResponse = bookConnection.execute()
            cookies.putAll(bookResponse.cookies())
            val bookDoc = bookResponse.parse()

            val pdfMergerUtility = PDFMergerUtility()

            val tempDirectoryName = ".temp-$fileName"
            pdfMergerUtility.destinationFileName = destinationFilePath.normalize().toString()

            val title = bookDoc.select(".titlucarte").text()
            val titleFile = PathUtils.originalFilePath(sourceKey, tempDirectoryName, "title.pdf").toFile()

            createPdfWithTitle(titleFile, title)

            pdfMergerUtility.addSource(titleFile)

            val chapterLinks = bookDoc.select("a")
            for ((chapterNumber, chapterLink) in chapterLinks.withIndex()) {
                val chapterHref = chapterLink.attr("href")
                val chapterConnection =
                    Jsoup.connect("http://www.biblioteca-digitala.ase.ro/biblioteca/$chapterHref")
                        .userAgent("Mozilla")
                        .maxBodySize(10 * 1024 * 1024)
                        .ignoreContentType(true)
                        .method(Connection.Method.GET)
                        .timeout(150 * 1000)

                cookies.forEach { name, value -> chapterConnection.cookie(name, value) }

                val bytes = chapterConnection.execute().bodyAsBytes()

                val chapterDestinationFile = PathUtils.originalFilePath(sourceKey,tempDirectoryName,
                    "chapter-$chapterNumber.pdf").toFile()
                chapterDestinationFile.writeBytes(bytes)

                pdfMergerUtility.addSource(chapterDestinationFile)

            }

            pdfMergerUtility.mergeDocuments(MemoryUsageSetting.setupMainMemoryOnly())

            Paths.get(
                ConfigProperties.dataDirectoryPath,
                ConfigProperties.originalDirectoryName,
                sourceKey,
                tempDirectoryName
            )
                .toFile().deleteRecursively()

            println(" OK")
        }
    }

    private fun createPdfWithTitle(destinationFile: File, title: String) {
        // Create a document and add a page to it
        PDDocument().use { document ->
            val page = PDPage(PDRectangle.A4)
            document.addPage(page)
            val contentStream = PDPageContentStream(document, page)

            val pdfFont = PDType1Font.HELVETICA
            val fontSize = 25f
            val leading = 1.5f * fontSize

            val mediabox = page.mediaBox
            val margin = 72f
            val width = mediabox.width - 2 * margin
            val startX = mediabox.lowerLeftX + margin
            val startY = mediabox.upperRightY - margin

            var text = title
            val lines = ArrayList<String>()
            var lastSpace = -1
            while (text.isNotEmpty()) {
                var spaceIndex = text.indexOf(' ', lastSpace + 1)
                if (spaceIndex < 0)
                    spaceIndex = text.length
                var subString = text.substring(0, spaceIndex)
                val size = fontSize * pdfFont.getStringWidth(subString) / 1000
                if (size > width) {
                    if (lastSpace < 0)
                        lastSpace = spaceIndex
                    subString = text.substring(0, lastSpace)
                    lines.add(subString)
                    text = text.substring(lastSpace).trim { it <= ' ' }
                    lastSpace = -1
                } else if (spaceIndex == text.length) {
                    lines.add(text)
                    text = ""
                } else {
                    lastSpace = spaceIndex
                }
            }

            contentStream.beginText()
            contentStream.setFont(pdfFont, fontSize)
            contentStream.newLineAtOffset(startX, startY)
            for (line in lines) {
                contentStream.showText(line)
                contentStream.newLineAtOffset(0f, -leading)
            }
            contentStream.endText()
            contentStream.close()

            document.save(destinationFile)
        }
    }

    override fun extractText(override: Boolean) {
        PdfTextExtractor(checkInRomanian = true).extractAll(sourceKey, override)
    }
}
