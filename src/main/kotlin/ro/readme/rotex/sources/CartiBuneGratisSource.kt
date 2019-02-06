package ro.readme.rotex.sources

import org.jsoup.Jsoup
import ro.readme.rotex.utils.EPubTextExtractor
import ro.readme.rotex.utils.PathUtils
import ro.readme.rotex.utils.TextCleaner
import java.lang.Exception

class CartiBuneGratisSource: Source() {
    override val sourceKey = "carti-bune-gratis"
    override val originalLink = "http://cartibunegratis.blogspot.ro"
    override val downloadLink = "https://drive.google.com/open?id=1DwnMXUsvbcLZSZPMLJ9GB9HEC9Mv1_c3"

    override fun downloadOriginal(override: Boolean) {
        val pageDoc =
            Jsoup.connect("http://cartibunegratis.blogspot.ro/p/catalog-de-carti-gratuite-in-romana.html").get()
        val links = pageDoc.select(".entry-content table a")
        for (link in links) {
            val href = link.attr("href")
            val bookDoc = Jsoup.connect(href).get()
            val titleParagraph = bookDoc.select("h3.entry-title").text()
            val title = getFileName(titleParagraph)
            print("$title ")
            val destinationPath = PathUtils.originalFilePath(
                sourceKey,
                "$title.epub"
            )
            val destinationFile = destinationPath.toFile()
            val downloadLink = bookDoc.select(".entry-content a:contains(EPUB)").attr("href")
            if (downloadLink.contains("gutenberg")) {
                println("SKIP")
            } else if (!destinationFile.exists() || override) {
                try {
                    destinationFile.parentFile.mkdirs()
                    val parts = downloadLink.split("&")
                    val parts2 = parts.first { it.length > 15 }.split("=")
                    val id = parts2[parts2.size - 1]
                    val directDownloadLink = "https://drive.google.com/uc?export=download&id=$id"

                    val bytes = Jsoup.connect(directDownloadLink)
                        .maxBodySize(6 * 1024 * 1024)
                        .ignoreContentType(true)
                        .execute()
                        .bodyAsBytes()
                    destinationFile.writeBytes(bytes)
                    println("OK")
                } catch (e: Exception) {
                    println("!ERR")
                    println(e.message)
                    e.printStackTrace()
                }
            } else {
                println("exists, skip no override")
            }
        }
    }
    private fun getFileName(titleParagraph: String): String {
        val name = titleParagraph.replace(Regex(".*\\d+\\s*(:|\\.|-)\\s*"), "")
        val parts = name.split(" - ")
        val fixedName = if (1 < parts.size && parts[1].matches(Regex("[A-Z].+\\s[A-Z].+"))) {
            "${parts[1]} - ${parts[0]}"
        } else {
            name
        }
        return TextCleaner(fixedName).stripForSafeFileName().cleaned
    }

    override fun extractText(override: Boolean) {
        EPubTextExtractor().extractAll(sourceKey, override)
    }
}
