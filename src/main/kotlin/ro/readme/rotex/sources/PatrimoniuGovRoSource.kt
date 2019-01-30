package ro.readme.rotex.sources

import org.jsoup.Connection
import org.jsoup.Jsoup
import ro.readme.rotex.ConfigProperties
import ro.readme.rotex.retrySocketTimeoutException
import ro.readme.rotex.skipFileIfExists
import ro.readme.rotex.utils.PathUtils
import ro.readme.rotex.utils.PdfTextExtractor
import java.io.File
import java.nio.file.Paths

class PatrimoniuGovRoSource: Source {
    override val sourceKey = "patrimoniu-gov-ro"
    override val originalLink = "http://www.cimec.ro/Biblioteca-Digitala/Biblioteca.html"
    override val downloadLink = ""

    override fun downloadOriginal(override: Boolean) {
        val mainPageDoc = Jsoup.connect("http://www.cimec.ro/Biblioteca-Digitala/Biblioteca.html").get()
        val downloadLinks = mainPageDoc.select(".mainlist200 h1 a")
        for (downloadLink in downloadLinks) {
            val downloadHref = downloadLink.attr("href")
            if (downloadHref.isNotBlank()) {
                val fileName = downloadHref.substringAfter("filename=")
                if (!fileName.contains("-cuprins.pdf")) {
                    print("$fileName ...")
                    val destinationFilePath = PathUtils.originalFilePath(sourceKey, fileName)
                    skipFileIfExists(destinationFilePath, override) {
                        retrySocketTimeoutException(3) {
                            try {
                                val bytes = Jsoup.connect(downloadHref)
                                    .userAgent("Mozilla")
                                    .maxBodySize(600 * 1024 * 1024)
                                    .ignoreContentType(true)
                                    .method(Connection.Method.POST)
                                    .timeout(120 * 1000)
                                    .execute()
                                    .bodyAsBytes()
                                destinationFilePath.toFile().writeBytes(bytes)
                                println("OK")
                            } catch (e: Exception) {
                                println("ERR")
                                e.printStackTrace()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun additionalTextClean(text: String): String {
        return text.replace(Regex("\\d*(?:\\s|\\n)*WWW.cimec.ro(?:\\s|\\n)*\\d*", RegexOption.MULTILINE), "")
    }

    override fun extractText(override: Boolean) {
        PdfTextExtractor(
            redoOCR = true,
            additionalTextClean = ::additionalTextClean
        ).extractAll(sourceKey, override)
    }
}
