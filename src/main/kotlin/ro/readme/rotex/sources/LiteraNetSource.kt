package ro.readme.rotex.sources

import org.jsoup.Connection
import org.jsoup.Jsoup
import ro.readme.rotex.skipFileIfExists
import ro.readme.rotex.utils.EPubTextExtractor
import ro.readme.rotex.utils.PathUtils
import ro.readme.rotex.utils.PdfTextExtractor
import ro.readme.rotex.utils.TextCleaner

class LiteraNetSource: Source() {
    override val sourceKey = "litera-net"
    override val originalLink = "http://editura.liternet.ro/catalog/1/Romana/toate-cartile.html"
    override val downloadLink = "https://drive.google.com/open?id=1jbAjDqiiM9axfnA8k-afLnuweOSAEdOh"

    override fun downloadOriginal(override: Boolean) {
        val pageDoc = Jsoup.connect("http://editura.liternet.ro/catalog/1/Romana/toate-cartile.html").get()
        val bookLinks = pageDoc.select(".yui-b p a")
        for (bookLink in bookLinks) {
            val bookTitle = bookLink.text()
            print("$bookTitle ... ")
            try {
                val bookHref = "http://editura.liternet.ro" + bookLink.attr("href")
                val bookDocConfirm = Jsoup.connect(bookHref).get()
                val fileName = TextCleaner(bookTitle).stripForSafeFileName().cleaned
                val destinationFilePath = PathUtils.originalFilePath(sourceKey, "$fileName.pdf")
                skipFileIfExists(destinationFilePath, override) {
                    var linkColl = bookDocConfirm.select("#prezentare_download .toolbar_download_dublu a")
                    if (linkColl.size == 0) {
                        linkColl = bookDocConfirm.select("#prezentare_download .toolbar_download_simplu a")
                    }
                    if (linkColl.size > 0) {
                        val downloadLink = if (linkColl.size > 1) {
                            "http://editura.liternet.ro" + linkColl.first().attr("href")
                        } else {
                            "http://editura.liternet.ro" + linkColl.attr("href")
                        }
                        val bookDoc = Jsoup.connect(downloadLink).get()
                        val downloadLinkPost = bookDoc.select(".yui-b form").attr("action")
                        val downloadLinkPdf = "https:$downloadLinkPost"
                        val confirmHiddenName = bookDoc.select(".yui-b form p input").first().attr("name")
                        if (!downloadLinkPost.isEmpty()) {
                            val bytes = Jsoup.connect(downloadLinkPdf)
                                .data(confirmHiddenName, "da")
                                .userAgent("Mozilla")
                                .maxBodySize(6 * 1024 * 1024)
                                .ignoreContentType(true)
                                .method(Connection.Method.POST)
                                .timeout(15 * 1000)
                                .execute()
                                .bodyAsBytes()
                            destinationFilePath.toFile().writeBytes(bytes)
                            println("OK")
                        } else {
                            println(" no download link")
                        }
                    }
                }
            } catch (e: Exception) {
                println("ERR")
                e.printStackTrace()
            }
        }
    }

    override fun extractText(override: Boolean) {
        PdfTextExtractor().extractAll(sourceKey, override)
    }
}
