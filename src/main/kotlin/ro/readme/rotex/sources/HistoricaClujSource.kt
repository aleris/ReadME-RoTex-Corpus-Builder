package ro.readme.rotex.sources

import org.jsoup.Connection
import org.jsoup.Jsoup
import ro.readme.rotex.retrySocketTimeoutException
import ro.readme.rotex.skipFileIfExists
import ro.readme.rotex.utils.PathUtils
import ro.readme.rotex.utils.PdfTextExtractor

class HistoricaClujSource: Source() {
    override val sourceKey = "historica-cluj"
    override val originalLink = "http://www.historica-cluj.ro/menu/arhiva_anuar.php"
    override val downloadLink = "https://drive.google.com/open?id=1FW4S4Mv0OriX8zMyNZzrCU3gc0ajmXLg"

    override fun downloadOriginal(override: Boolean) {
        val yearsPageDoc = Jsoup.connect("http://www.historica-cluj.ro/menu/arhiva_anuar.php").get()
        val yearsLinks = yearsPageDoc.select(".ContentsArea a")
        for (yearLink in yearsLinks) {
            val yearTitle = yearLink.text()
            println(yearTitle)
            val yearHref = yearLink.attr("href")
            val completeYearHref = "http://www.historica-cluj.ro/menu/$yearHref"
            val yearPageDoc = Jsoup.connect(completeYearHref).timeout(15 * 1000).get()
            val daysLinks = yearPageDoc.select(".ContentsArea a")
            for (sectionLink in daysLinks) {
                val sectionText = sectionLink.text()
                if (sectionText == "PDF") {
                    val sectionHref = sectionLink.attr("href")
                    print("\tDay: $sectionHref ... ")
                    retrySocketTimeoutException(3) {
                        val docTitle = sectionHref.replace("../anuare/", "").replace("/", "_")
                        val completeSectionHref = "http://www.historica-cluj.ro/${sectionHref.substring(3)}"
                        val bytes = Jsoup.connect(completeSectionHref)
                            .userAgent("Mozilla")
                            .maxBodySize(6 * 1024 * 1024)
                            .ignoreContentType(true)
                            .method(Connection.Method.POST)
                            .timeout(15 * 1000)
                            .execute()
                            .bodyAsBytes()
                        val destinationFilePath = PathUtils.originalFilePath(sourceKey, "$docTitle.pdf")
                        skipFileIfExists(destinationFilePath, override) {
                            val destinationFile = destinationFilePath.toFile()
                            destinationFile.writeBytes(bytes)
                            println("OK")
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
