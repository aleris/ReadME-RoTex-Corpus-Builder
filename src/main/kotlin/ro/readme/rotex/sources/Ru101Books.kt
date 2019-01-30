package ro.readme.rotex.sources

import org.jsoup.Jsoup
import ro.readme.rotex.retrySocketTimeoutException
import ro.readme.rotex.skipFileIfExists
import ro.readme.rotex.utils.DexDictionary
import ro.readme.rotex.utils.PathUtils
import ro.readme.rotex.utils.PdfTextExtractor
import ro.readme.rotex.utils.TextCleaner

class Ru101Books: Source {
    override val sourceKey = "ru-101-books"
    override val originalLink = "http://www.101books.ru/"
    override val downloadLink = ""

    override fun downloadOriginal(override: Boolean) {
        for (page in 1..107) {
            println("Page $page")
            val linksPageDoc = Jsoup.connect("http://www.101books.ru/?page=$page#books").get()
            val boxes = linksPageDoc.select(".container .content .book-show")
            for (box in boxes) {
                val title = box.select(".book-title").text()
                val author = box.select(".boook-author").text()
                val fileName = TextCleaner("$author - $title.pdf").stripForSafeFileName().cleaned
                print("$fileName ... ")
                val destinationFilePath = PathUtils.originalFilePath(sourceKey, fileName)
                skipFileIfExists(destinationFilePath, override) {
                    retrySocketTimeoutException(5) {
                        val docHref = box.select("a").attr("href")
                        val doc = Jsoup.connect(docHref).get()
                        val textOfBook = doc.select(".text-of-book").text()
                        if (DexDictionary.isLikelyInRomanian(textOfBook)) {
                            val downloadHref = doc.select("a.pdf-link").attr("href")
                            try {
                                val bytes = Jsoup.connect(downloadHref)
                                    .userAgent("Mozilla")
                                    .maxBodySize(160 * 1024 * 1024)
                                    .ignoreContentType(true)
                                    .timeout(3 * 60 * 1000)
                                    .execute()
                                    .bodyAsBytes()
                                destinationFilePath.toFile().writeBytes(bytes)
                                println("OK")
                            } catch (e: Exception) {
                                println("ERR")
                                e.printStackTrace()
                            }
                        } else {
                            println("skipping, not in romanian")
                        }
                    }
                }
            }
        }
    }

    override fun extractText(override: Boolean) {
        PdfTextExtractor().extractAll(sourceKey, override)
    }
}
