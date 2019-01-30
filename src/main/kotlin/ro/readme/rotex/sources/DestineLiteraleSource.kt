package ro.readme.rotex.sources

import org.jsoup.Jsoup
import ro.readme.rotex.retrySocketTimeoutException
import ro.readme.rotex.skipFileIfExists
import ro.readme.rotex.utils.PathUtils
import ro.readme.rotex.utils.PdfTextExtractor
import ro.readme.rotex.utils.TextCleaner

class DestineLiteraleSource: Source {
    override val sourceKey = "destine-literale"
    override val originalLink = "http://www.scriitoriiromani.com/DestineLiterare.html"
    override val downloadLink = "https://drive.google.com/open?id=1cBnAKvgMihuG_xiL3jTO8_bBTY9R43Ri"

    override fun downloadOriginal(override: Boolean) {
        val indexDoc = Jsoup.connect("http://www.scriitoriiromani.com/DestineLiterare.html").get()
        val links = indexDoc.select("#e10 a")
        for (link in links) {
            val href = link.attr("href")
            val fileName = TextCleaner(href.substring("DestineLiterare/RevistePDF/".length)).stripForSafeFileName().cleaned
            print("$fileName ... ")
            val destinationFilePath = PathUtils.originalFilePath(sourceKey, fileName)
            skipFileIfExists(destinationFilePath, override) {
                retrySocketTimeoutException(3) {
                    try {
                        val bytes = Jsoup.connect("http://www.scriitoriiromani.com/$href")
                            .timeout(60 * 1000)
                            .maxBodySize(60 * 1024 * 1024)
                            .ignoreContentType(true)
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

    override fun extractText(override: Boolean) {
        PdfTextExtractor().extractAll(sourceKey, override)
    }
}
