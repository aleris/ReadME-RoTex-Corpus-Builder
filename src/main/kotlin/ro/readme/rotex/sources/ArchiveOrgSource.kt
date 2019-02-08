package ro.readme.rotex.sources

import org.apache.tika.Tika
import org.apache.tika.config.TikaConfig
import org.jsoup.Jsoup
import ro.readme.rotex.retrySocketTimeoutException
import ro.readme.rotex.skipFileIfExists
import ro.readme.rotex.utils.DexDictionary
import ro.readme.rotex.utils.PathUtils
import ro.readme.rotex.utils.TextCleaner
import java.io.File
import java.io.PrintWriter

class ArchiveOrgSource: Source() {
    override val sourceKey = "archive-org"
    override val originalLink = "https://archive.org/"
    override val downloadLink = "https://drive.google.com/open?id=18NoxsiiMD1bclaTFPhKM_X94ziDRQ-Cu"

    private val tika = Tika(TikaConfig(javaClass.classLoader.getResourceAsStream("tika-config.xml")))

    override fun downloadOriginal(override: Boolean) {
        getIdentifiers().forEach { id ->
            download(id, override)
        }
    }

    private fun download(id: String, override: Boolean) {
        print("$id ... ")
        val fileIndexDoc = Jsoup
            .connect("https://archive.org/download/$id")
            .timeout(60 * 1000)
            .get()
        val links = fileIndexDoc.select(".directory-listing-table a")
        val hrefs = links
            .map { it.attr("href") }
            .filter { !it.contains("/details/") }
        var href = hrefs.find { it.endsWith(".txt") && !it.contains("_meta")}
        if (null == href) {
            href = hrefs.find { it.endsWith(".epub") }
        }
        if (null == href) {
            href = hrefs.find { it.endsWith(".doc") }
        }
        if (null == href) {
            href = hrefs.find { it.endsWith(".docx") }
        }
        if (null != href) {
            print("$href ... ")
            val destinationPath = PathUtils.originalFilePath(sourceKey, href)
            skipFileIfExists(destinationPath, override) {
                retrySocketTimeoutException(3) {
                    Jsoup
                        .connect("https://archive.org/download/$id/$href")
                        .timeout(3 * 60 * 1000)
                        .maxBodySize(30 * 1024 * 1024)
                        .ignoreContentType(true)
                        .execute()
                        .bodyStream().use { downloadStream ->
                            destinationPath.toFile().outputStream().use { downloadStream.copyTo(it) }
                        }
                    println("OK")
                }
            }
        } else {
            println("no txt, epub, doc, docx found, skipping")
        }
    }

    private fun getIdentifiers() = sequence {
        val identifiersJsonString = Jsoup
            .connect("https://archive.org/advancedsearch.php?q=mediatype%3A%28texts%29+AND+languageSorter%3A%28Romanian%29&fl%5B%5D=identifier&sort%5B%5D=&sort%5B%5D=&sort%5B%5D=&rows=6000&page=1&output=json")
            .timeout(3 * 60 * 1000)
            .maxBodySize(10 * 1024 * 1024)
            .ignoreContentType(true)
            .execute()
            .bodyStream()
            .reader()
            .readText()

        val prefix = "\"identifier\":\""
        val suffix = "\"},{\""
        val suffixEnd = "\"}]}}"
        var pi = 0
        var more = true
        var skip = true
        do {
            val si = identifiersJsonString.indexOf(prefix, pi)
            val st = si + prefix.length
            var ei = identifiersJsonString.indexOf(suffix, st)
            if (-1 != ei) {
                pi = ei
            } else {
                ei = identifiersJsonString.indexOf(suffixEnd, st)
                pi = ei
                more = false
            }
            val id = identifiersJsonString.substring(st, ei)
            if (!skip) {
                yield(id)
            }
            if (skip && id == "JoseSaramagoEseuDespreOrbire") {
                skip = false
            }
        } while (more)
    }

    override fun extractText(override: Boolean) {
        val outputPath = PathUtils.textFilePath(sourceKey)
        skipFileIfExists(outputPath, override) {
            outputPath.toFile().printWriter().use { printWriter ->
                val originalDirectoryPath = PathUtils.originalDirectoryPath(sourceKey)
                originalDirectoryPath.toFile()
                    .walk()
                    .filter { it.isFile }
                    .forEach { file ->
                        extractText(printWriter, file)
                    }
            }
        }
    }

    private fun extractText(printWriter: PrintWriter, file: File) {
        print("${file.name} ... ")
        try {
            val text = tika.parseToString(file)
            if (DexDictionary.isLikelyInRomanian(text)) {
                val cleaned = TextCleaner(text)
                    .correctCedilaDiacritics()
                    .stripLineBrakeReplacement()
                    .removeDuplicateLinesIgnoringNumbers()
                    .removeLikelyPageNumber()
                    .stitchLinesSimple()
                    .removeDuplicateBlankLines()
                    .cleaned
                printWriter.write(cleaned)
                printWriter.println()
                printWriter.println()
                println("OK")
            } else {
                println("likely not in romanian, skipping")
            }
        } catch (e: Exception) {
            println("ERR")
            e.printStackTrace()
        }
    }
}
