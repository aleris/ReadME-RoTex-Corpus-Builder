package ro.readme.rotex.sources

import org.jsoup.Jsoup
import ro.readme.rotex.skipFileIfExists
import ro.readme.rotex.utils.PathUtils
import java.util.zip.ZipInputStream

class DgtAquis: Source() {
    override val sourceKey = "dgt-aquis"
    override val originalLink = "https://ec.europa.eu/jrc/en/language-technologies/dgt-acquis"
    override val downloadLink = "https://drive.google.com/open?id=1GUjlUH2_N7NKetAPjDT3BTk0VKPwy7ll"

    override fun downloadOriginal(override: Boolean) {
        val href = "https://ec.europa.eu/jrc/en/language-technologies/dgt-acquis/da1-ft"
        val doc = Jsoup.connect(href).get()
        val links = doc.select(".panel-body.content a")
        for (link in links) {
            val name = "ro.zip"
            if (link.text() == name) {
                val zipHref = link.attr("href")
                val code = zipHref.substringBeforeLast("/").substringAfterLast("/")
                val fileName = "$code-$name"
                print("$fileName ... ")
                val destinationPath = PathUtils.originalFilePath(sourceKey, fileName)
                skipFileIfExists(destinationPath, override) {
                    destinationPath.toFile().outputStream().use { outputStream ->
                        Jsoup
                            .connect(zipHref)
                            .maxBodySize(150 * 1024 * 1024)
                            .ignoreContentType(true)
                            .timeout(10 * 60 * 1000)
                            .execute()
                            .bodyStream().use {
                                it.copyTo(outputStream)
                            }
                        println("OK")
                    }
                }
            }
        }
    }

    override fun extractText(override: Boolean) {
        val outputFilePath = PathUtils.textFilePath(sourceKey)
        skipFileIfExists(outputFilePath, override) {
            outputFilePath.toFile().outputStream().use { outputStream ->
                val originalDirectoryPath = PathUtils.originalDirectoryPath(sourceKey)
                originalDirectoryPath.toFile()
                    .walk()
                    .filter { it.isFile }
                    .filter { it.extension == "zip" }
                    .sorted()
                    .forEach { originalFile ->
                        println("${originalFile.name} ... ")
                        originalFile.inputStream().use { fileInputStream ->
                            ZipInputStream(fileInputStream).use { compressedStream ->
                                var entry = compressedStream.nextEntry
                                do {
                                    if (!entry.isDirectory) {
                                        print("${entry.name} ... ")
                                        compressedStream.copyTo(outputStream)
                                        println("OK")
                                    }
                                    entry = compressedStream.nextEntry
                                } while (null != entry)
                            }
                        }
                    }
            }
            println("OK")
        }
    }
}
