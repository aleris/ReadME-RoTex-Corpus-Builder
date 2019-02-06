package ro.readme.rotex.sources

import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream
import org.jsoup.Jsoup
import ro.readme.rotex.skipFileIfExists
import ro.readme.rotex.utils.PathUtils

class DcepSource: Source() {
    override val sourceKey = "dcep"
    override val originalLink = "https://wt-public.emm4u.eu/Resources/DCEP-2013/DCEP-Download-Page.html"
    override val downloadLink = "https://drive.google.com/open?id=1SJsMPS_8UuYDx1KerZI8uZ-AUN0JnvZx"

    override fun downloadOriginal(override: Boolean) {
        val destinationFilePath = PathUtils.originalFilePath(sourceKey,"DCEP-sentence-RO-pub.tar.bz2")
        print("Downloading to $destinationFilePath ... ")
        skipFileIfExists(destinationFilePath, override) {
            val downloadHref = "https://wt-public.emm4u.eu/Resources/DCEP-2013/sentences/DCEP-sentence-RO-pub.tar.bz2"
            val stream = Jsoup.connect(downloadHref)
                .timeout(30 * 60 * 1000)
                .maxBodySize(700 * 1024 * 1024)
                .ignoreContentType(true)
                .execute()
                .bodyStream()
            destinationFilePath.toFile().outputStream().use {
                stream.copyTo(it)
            }
            println("OK")
        }
    }

    override fun extractText(override: Boolean) {
        val outputFilePath = PathUtils.textFilePath(sourceKey)
        println("Extracting to $outputFilePath ...")
        skipFileIfExists(outputFilePath, override) {
            val originalFilePath = PathUtils.originalFilePath(sourceKey, "DCEP-sentence-RO-pub.tar.bz2")
            val originalFile = originalFilePath.toFile()
            outputFilePath.toFile().outputStream().use { outputStream ->
                originalFile.inputStream().use { fileInputStream ->
                    BZip2CompressorInputStream(fileInputStream).use { compressedStream ->
                        TarArchiveInputStream(compressedStream).use { archiveStream ->
                            archiveStream.buffered().use { bufferedInputStream ->
                                var entry = archiveStream.nextTarEntry
                                do {
                                    if (entry.isFile) {
                                        print("${entry.name} ... ")
                                        bufferedInputStream.copyTo(outputStream)
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
