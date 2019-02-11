package ro.readme.rotex.sources

import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream
import org.jsoup.Jsoup
import ro.readme.rotex.skipFileIfExists
import ro.readme.rotex.utils.PathUtils
import java.io.ByteArrayOutputStream
import java.util.zip.GZIPInputStream

class JrqAquis: Source() {
    override val sourceKey = "jrq-aquis"
    override val originalLink = "https://ec.europa.eu/jrc/en/language-technologies/jrc-acquis"
    override val downloadLink = "https://drive.google.com/open?id=1VoaAb7x3Y2mnJqiGeMnHJXkhQv7QZ2tC"

    override fun downloadOriginal(override: Boolean) {
        val href = "https://wt-public.emm4u.eu/Acquis/JRC-Acquis.3.0/corpus/jrc-ro.tgz"
        val destinationPath = PathUtils.originalFilePath(sourceKey, "jrc-ro.tgz")
        print("jrc-ro.tgz ... ")
        skipFileIfExists(destinationPath, override) {
            destinationPath.toFile().outputStream().use { outputStream ->
                Jsoup
                    .connect(href)
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

    override fun extractText(override: Boolean) {
        val outputFilePath = PathUtils.textFilePath(sourceKey)
        println("Extracting to $outputFilePath ...")
        skipFileIfExists(outputFilePath, override) {
            val originalFilePath = PathUtils.originalFilePath(sourceKey, "jrc-ro.tgz")
            val originalFile = originalFilePath.toFile()
            outputFilePath.toFile().outputStream().use { outputStream ->
                originalFile.inputStream().use { fileInputStream ->
                    GZIPInputStream(fileInputStream).use { compressedStream ->
                        TarArchiveInputStream(compressedStream).use { archiveStream ->
                            archiveStream.buffered().use { bufferedInputStream ->
                                var entry = archiveStream.nextTarEntry
                                do {
                                    if (entry.isFile) {
                                        print("${entry.name} ... ")
                                        val tempStream = ByteArrayOutputStream()
                                        bufferedInputStream.copyTo(tempStream)
                                        val doc = Jsoup.parse(tempStream.toByteArray().inputStream(), Charsets.UTF_8.name(), "")
                                        doc.select("p").forEach { p ->
                                            outputStream.write(p.text().toByteArray())
                                            outputStream.write("\n".toByteArray())
                                        }
                                        println("OK")
                                    }
                                    entry = archiveStream.nextTarEntry
                                } while (null != entry)
                            }
                        }
                    }
                }
            }
        }
        println("OK")
    }
}
