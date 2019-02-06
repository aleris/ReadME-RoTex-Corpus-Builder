package ro.readme.rotex.sources

import org.jsoup.Connection
import org.jsoup.Jsoup
import ro.readme.rotex.retrySocketTimeoutException
import ro.readme.rotex.skipFileIfExists
import ro.readme.rotex.utils.PdfTextExtractor
import ro.readme.rotex.utils.TextCleaner
import java.nio.file.Paths

class PaulGomaSource: Source() {
    override val sourceKey = "paul-goma"
    override val originalLink = "http://www.paulgoma.com/lista-completa"
    override val downloadLink = "https://drive.google.com/open?id=1DPEssf7eHCml0jCsvGORsfHEhaAWyNyj"

    override fun downloadOriginal(override: Boolean) {
        val linksPageDoc = Jsoup.connect("http://www.paulgoma.com/lista-completa/").get()
        val links = linksPageDoc.select(".postcontent a")
        for (link in links.subList(15, links.size)) {
            val href = link.attr("href")
            if (href.isNotBlank()) {
                print(href)
                val pageDoc = Jsoup.connect(href).get()
                val docLink = pageDoc.select(".postcontent dd.caption a")
                val docName = docLink.text()
                println("\t$docName ... ")
                val fileName = TextCleaner(docName).stripForSafeFileName().cleaned
                val destinationFilePath = Paths.get(sourceKey, fileName)
                skipFileIfExists(destinationFilePath, override) {
                    retrySocketTimeoutException(3) {
                        val docHref = docLink.attr("href")
                        val bytes = Jsoup.connect(docHref)
                            .userAgent("Mozilla")
                            .maxBodySize(6 * 1024 * 1024)
                            .ignoreContentType(true)
                            .method(Connection.Method.POST)
                            .timeout(15 * 1000)
                            .execute()
                            .bodyAsBytes()
                        destinationFilePath.toFile().writeBytes(bytes)
                        println(" OK")
                    }
                }
            }
        }
    }

    private fun additionalTextCleaner(text: String): String {
        return text
            .replace('¶', 'ă')
            .replace('æ', 'ă')
            .replace('î', 'î')
            .replace('§', 'ș')
            .replace('∞', 'ț')
            .replace('†', 'ț')
            .replace('£', 'Ș')
            .replace('∑', 'Ș')
            .replace('¢', 'Ț')
            .replace('™', 'Ă')
            .replace('˜', 'Â')
            .replace('ï', 'Î')
            .replace(Regex("\\d+P A U L G O M A -"), "")
    }

    override fun extractText(override: Boolean) {
        PdfTextExtractor(additionalTextClean = ::additionalTextCleaner).extractAll(sourceKey, override)
    }
}
