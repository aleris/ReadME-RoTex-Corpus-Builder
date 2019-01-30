package ro.readme.rotex.utils

import nl.siegmann.epublib.epub.EpubReader
import org.jsoup.Jsoup
import ro.readme.rotex.skipFileIfExists
import java.io.File
import java.io.PrintWriter
import java.nio.file.Path

class EPubTextExtractor : TextExtractor() {
    override fun extractAll(sourceKey: String, override: Boolean) {
        super.extractAll(sourceKey, override, ".epub")
    }

    override fun extract(inputFile: File, printWriter: PrintWriter) {
        try {
            print("${inputFile.name} ... ")
            val book = EpubReader().readEpub(inputFile.inputStream())
            printWriter.println(inputFile.nameWithoutExtension)
            printWriter.println()
            book.contents.forEach {
                val html = it.reader.readText()
                val paragraphs = Jsoup.parse(html).select("body p")
                paragraphs.forEach { p ->
                    val cleaned = TextCleaner(p.text())
                        .correctCedilaDiacritics()
                        .replaceEPubMultipleSpacesWithNewLine()
                        .cleaned.trimStart()
                    printWriter.println(cleaned)
                }
            }
            println("OK")
        } catch (e: Exception) {
            println("ERR")
            e.printStackTrace()
        }
    }
}
