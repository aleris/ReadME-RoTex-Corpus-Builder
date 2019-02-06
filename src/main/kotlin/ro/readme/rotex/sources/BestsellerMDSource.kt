package ro.readme.rotex.sources

import org.jsoup.Jsoup
import ro.readme.rotex.*
import ro.readme.rotex.utils.EPubTextExtractor
import ro.readme.rotex.utils.PathUtils
import ro.readme.rotex.utils.TextCleaner
import java.nio.file.Paths

class BestsellerMDSource: Source() {

    override val sourceKey = "bestseller-md"
    override val originalLink = "https://www.bestseller.md"
    override val downloadLink = "https://drive.google.com/open?id=1PvUXcnvPo6dOyA8L6lCuqbsAvlWp3BeV"

    override fun downloadOriginal(override: Boolean) {
        for (page in 1..8) {
            println("Page $page")
            val pageDoc = Jsoup
                .connect("https://www.bestseller.md/ebooks.html?dir=asc&limba=130&p=$page&price=-1")
                .timeout(10 * 1000)
                .get()
            val links = pageDoc.select("h2.product-name a")
            for (link in links) {
                val href = link.attr("href")
                val bookDoc = Jsoup.connect(href).timeout(10 * 1000).get()
                val downloadList = bookDoc.select("#downloadable-links-list li")
                for (li in downloadList) {
                    if (li.select("label").text().trim() == "ePub") {
                        val title = bookDoc.select(".product-name span").text()
                            .replace(Regex(" \\[.+]| \\(.+\\)"), "")
                        val author = bookDoc.select(".product-autor h3").text()
                        val fileName = TextCleaner("$author - $title").stripForSafeFileName().cleaned
                        print("$fileName ... ")
                        val destinationPath = PathUtils.originalFilePath(sourceKey, "$fileName.epub")
                        skipFileIfExists(destinationPath, override) {
                            val bookHref = li.select("a").attr("href")
                            val bytes = Jsoup.connect(bookHref)
                                .timeout(60 * 1000)
                                .maxBodySize(6 * 1024 * 1024)
                                .ignoreContentType(true)
                                .execute()
                                .bodyAsBytes()
                            destinationPath.toFile().writeBytes(bytes)
                            println("OK")
                        }
                    }
                }
            }
        }
    }

    override fun extractText(override: Boolean) {
        EPubTextExtractor().extractAll(sourceKey, override)
    }
}
