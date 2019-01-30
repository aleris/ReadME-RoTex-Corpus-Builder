package ro.readme.rotex.sources

import org.jsoup.Connection
import org.jsoup.Jsoup
import ro.readme.rotex.retrySocketTimeoutException
import ro.readme.rotex.skipFileIfExists
import ro.readme.rotex.utils.PdfTextExtractor
import ro.readme.rotex.utils.TextCleaner
import java.nio.file.Paths

class BziSource: Source {
    override val sourceKey = "bzi"
    override val originalLink = "https://www.bzi.ro/arhiva"
    override val downloadLink = ""

    override fun downloadOriginal(override: Boolean) {
        val yearsPageDoc = Jsoup.connect("https://www.bzi.ro/arhiva/").get()
        val yearsLinks = yearsPageDoc.select("#mainContainer div.ui div.item:first-child ul a")
        for (yearLink in yearsLinks) {
            val yearHref = yearLink.attr("href")
            println("Year: $yearHref")
            val yearPageDoc = Jsoup.connect(yearHref).timeout(15 * 1000).get()
            val daysLinks = yearPageDoc.select("#mainContainer div.ui div.item #arhiva-calendar a")
            for (dayLink in daysLinks) {
                val dayHref = dayLink.attr("href")
                System.out.println("\tDay: $dayHref")
                val dayPageDoc = Jsoup.connect(dayHref).timeout(15 * 1000).get()
                val paperLink = dayPageDoc.select("#mainContainer div.ui div.item:nth-child(2) a:first-child")
                val paperTitle = paperLink.text()
                val prefix = "Editia tiparita din "
                if (paperTitle.startsWith(prefix)) {
                    val fileName = TextCleaner(paperTitle.removePrefix(prefix)).stripForSafeFileName().cleaned
                    val destinationFilePath = Paths.get(sourceKey, fileName)
                    print("\t\t$paperTitle ... ")
                    skipFileIfExists(destinationFilePath, override) {
                        retrySocketTimeoutException(3) {
                            val paperHref = paperLink.attr("href")
                            try {
                                val bytes = Jsoup.connect(paperHref)
                                    .userAgent("Mozilla")
                                    .maxBodySize(6 * 1024 * 1024)
                                    .ignoreContentType(true)
                                    .method(Connection.Method.POST)
                                    .timeout(60 * 1000)
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

    private fun additionalTextCleaner(text: String): String {
        return text
            .replace('`', 'ă')
            .replace('\\', 'î')
            .replace('[', 'ș')
            .replace(']', 'ț')
            .replace('{', 'Ș')
            .replace('}', 'Ț')
            .replace('', '•')
            .replace('å', 'Ă')
            .replace('˜', 'Â')
            .replace('|', 'Î')
            .replace('~', 'Ă')
            .replace('', '•')
            .replace('¶', 'ă')
            .replace('∞', 'ț')
            .replace('§', 'ș')
            .replace(Regex("([A-Z])Î([A-Z .,!“])"), "$1Ă$2")
            .replace(Regex("([a-z])î([a-z .,!“])"), "$1ă$2")
            .replace(Regex("(\\s)ă(?=[bcdefghjklmnpqrsștțuvxyzBCDEFGHJKLMNPQRSȘTȚUVXYZ])", RegexOption.MULTILINE), "$1î")
            .replace(Regex("(\\s)Ă(?=[bcdefghjklmnpqrsștțuvxyzBCDEFGHJKLMNPQRSȘTȚUVXYZ])", RegexOption.MULTILINE), "$1Î")
            .replace(Regex("([bcdefghjklmnpqrsștțuvxyzBCDEFGHJKLMNPQRSȘTȚUVXYZ])î(\\s)", RegexOption.MULTILINE), "$1ă$2")
            .replace(Regex("([bcdefghjklmnpqrsștțuvxyzBCDEFGHJKLMNPQRSȘTȚUVXYZ])Î(\\s)", RegexOption.MULTILINE), "$1Ă$2")
            .replace(Regex("([bcdefghjklmnpqrsștțuvxyz])(\\s)â([bcdefghjklmnpqrsștțuvxyz])"), "$1â")
    }

    override fun extractText(override: Boolean) {
        PdfTextExtractor(
            additionalTextClean = ::additionalTextCleaner,
            minimumPages = 2).extractAll(sourceKey, override)
    }
}
