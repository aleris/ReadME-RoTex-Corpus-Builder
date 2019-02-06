package ro.readme.rotex.sources

import org.jsoup.Jsoup
import ro.readme.rotex.skipFileIfExists
import ro.readme.rotex.utils.EPubTextExtractor
import ro.readme.rotex.utils.PathUtils
import ro.readme.rotex.utils.TextCleaner

class BibliotecaPeMobilSource: Source() {
    override val sourceKey = "biblioteca-pe-mobil"
    override val originalLink = "https://scoala.bibliotecapemobil.ro"
    override val downloadLink = "https://drive.google.com/open?id=1PUef4yUwYVsFaJrQzc6_u7909NgJEUeA"

    override fun downloadOriginal(override: Boolean) {
        for (category in arrayOf("Proza", "Poezie", "Cultura generala")) {
            val categHref = "https://scoala.bibliotecapemobil.ro/gen.php?type=$category"
            val categDoc = Jsoup.connect(categHref).get()
            val bookLinks = categDoc.select(".swiper-slide a")
            for (bookLink in bookLinks) {
                val bookHref = bookLink.attr("href")
                val bookDoc = Jsoup.connect(bookHref).get()
                val title = bookDoc.select(".section h1").first().text()
                val author = bookDoc.select(".section h2").first().text()
                val fileName = TextCleaner("$author - $title").stripForSafeFileName().cleaned
                print("$fileName ... ")
                val destinationFilePath = PathUtils.originalFilePath(sourceKey, category, "$fileName.epub")
                skipFileIfExists(destinationFilePath, override) {
                    val downloadHref = bookDoc.select(".download a[data-type=\"epub\"]").attr("href")
                    try {
                        val bytes = Jsoup.connect(downloadHref)
                            .timeout(60 * 1000)
                            .maxBodySize(6 * 1024 * 1024)
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
        EPubTextExtractor().extractAll(sourceKey, override)
    }
}
