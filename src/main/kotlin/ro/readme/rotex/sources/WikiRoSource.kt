package ro.readme.rotex.sources

import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream
import org.jsoup.Jsoup
import org.xml.sax.Attributes
import org.xml.sax.helpers.DefaultHandler
import ro.readme.rotex.skipFileIfExists
import ro.readme.rotex.utils.PathUtils
import ro.readme.rotex.utils.TextCleaner
import java.io.*
import javax.xml.parsers.SAXParserFactory

class WikiRoSource: Source {
    override val sourceKey = "wiki-ro"
    override val originalLink = "https://dumps.wikimedia.org/rowiki/latest/rowiki-latest-pages-meta-current.xml.bz2"
    override val downloadLink = "https://drive.google.com/open?id=1UGxDkF_EzSOiie_vs8uio3_FSr6_SL-7"

    override fun downloadOriginal(override: Boolean) {
        val destinationFilePath = PathUtils.originalFilePath(sourceKey,"rowiki-latest-pages-meta-current.xml.bz2")
        print("Downloading to $destinationFilePath ... ")
        skipFileIfExists(destinationFilePath, override) {
            val downloadHref = "https://dumps.wikimedia.org/rowiki/latest/rowiki-latest-pages-meta-current.xml.bz2"
            val stream = Jsoup.connect(downloadHref)
                .timeout(30 * 60 * 1000)
                .maxBodySize(700 * 1024 * 1024)
                .ignoreContentType(true)
                .execute()
                .bodyStream()
            destinationFilePath.toFile().outputStream().use {
                stream.copyTo(it)
            }
            println("OK")
        }
    }

    override fun extractText(override: Boolean) {
        val outputFilePath = PathUtils.textFilePath(sourceKey)
        print("Extracting to $outputFilePath ... ")
        skipFileIfExists(outputFilePath, override) {
            val originalFilePath = PathUtils.originalFilePath(sourceKey, "rowiki-latest-pages-meta-current.xml.bz2")
            val originalFile = originalFilePath.toFile()
            originalFile.inputStream().use { fileInputStream ->
                BZip2CompressorInputStream(fileInputStream).use { compressedStream ->
                    val saxFactory = SAXParserFactory.newInstance()
                    val saxParser = saxFactory.newSAXParser()
                    compressedStream.buffered().use { bufferedInputStream ->
                        outputFilePath.toFile().bufferedWriter().use { bufferedWriter ->
                            val saxParserHandler = SaxParserHandler(bufferedWriter)
                            saxParser.parse(bufferedInputStream, saxParserHandler)
                        }
                    }
                }
            }
        }
        println("OK")
    }

    inner class SaxParserHandler(
        private val bufferedWriter: BufferedWriter
    ): DefaultHandler() {

        private var inPage = false
        private var inRevision = false
        private var inPageId = false
        private var inPageText = false

        private var pageId: String = ""

        private val buffer = StringBuilder()

        override fun startElement(uri: String, localName: String, name: String, attributes: Attributes) {
            when (name) {
                "page" -> inPage = true
                "revision" -> inRevision = true
                "id" -> if (inPage && !inRevision) inPageId = true
                "text" -> if (inPage && inRevision) inPageText = true
            }
        }

        override fun endElement(uri: String, localName: String, name: String) {
            when (name) {
                "page" -> inPage = false
                "revision" -> inRevision = false
                "id" -> if (inPage && !inRevision) inPageId = false
                "text" -> {
                    if (inPage && inRevision) inPageText = false

                    val cleaned = cleanText(buffer.toString())

                    bufferedWriter.write(cleaned)
                    bufferedWriter.write("\n")

                    buffer.setLength(0)
                }
            }
        }

        override fun characters(ch: CharArray, start: Int, length: Int) {
            when {
                inPageId -> pageId = String(ch, start, length)
                inPageText -> {
                    val text = String(ch, start, length)
                    buffer.append(text)
                }
            }
        }
    }

    fun cleanText(content: String): String {
        val cleaned = content
            .replace(Regex("\\[http:[^ ]+ +(.+?)\\]", RegexOption.MULTILINE), "$1")

            .replace(Regex("^[\\|!][ \\-].+\$", setOf(RegexOption.MULTILINE)), "")
            .replace(Regex("\\{\\{([^\\|]+?)\\}\\}|\\{\\{(?:.+\\|)?(.+?)\\}\\}", setOf(RegexOption.MULTILINE)), "$1$2")
            .replace(Regex("^\\{\\{.+?^\\}\\}\$", setOf(RegexOption.MULTILINE, RegexOption.DOT_MATCHES_ALL)), "")
            .replace(Regex("^\\{(?:\\{|\\|).+\$", setOf(RegexOption.MULTILINE)), "")
            .replace(Regex("^.*(?:}|\\|)}\$", setOf(RegexOption.MULTILINE)), "")

            .replace(Regex("\\[\\[Fișier:.+?\\]\\]", RegexOption.MULTILINE), "")
            .replace(Regex("\\[\\[File:.+?\\]\\]", RegexOption.MULTILINE), "")
            .replace(Regex("\\[\\[([^\\|]+?)\\]\\]|\\[\\[(?:.+\\|)?(.+?)\\]\\]", RegexOption.MULTILINE), "$1$2")

            .replace(Regex("=+\\s*([^=]+)\\s*=+", RegexOption.MULTILINE), "\n$1.\n")

            .replace("''", "")
            .replace("'''", "")
            .replace(Regex("'(.+?)'", RegexOption.MULTILINE), "$1")

            .replace("&quot;", "\"")
            .replace("&apos;", "'")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&amp;", "&")
            .replace("&nbsp;", " ")

            .replace(Regex("<.+?>", setOf(RegexOption.DOT_MATCHES_ALL, RegexOption.MULTILINE)), "")
            .replace(Regex(" +", RegexOption.MULTILINE), " ")
            .replace(Regex("\\n+", RegexOption.MULTILINE), "\n")

        return TextCleaner(cleaned).correctCedilaDiacritics().cleaned
    }
}
