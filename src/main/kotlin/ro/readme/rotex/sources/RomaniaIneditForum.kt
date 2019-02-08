package ro.readme.rotex.sources

import com.coremedia.iso.Hex
import org.apache.commons.compress.archivers.ArchiveException
import org.apache.commons.compress.archivers.ArchiveStreamFactory
import org.apache.tika.Tika
import org.apache.tika.config.TikaConfig
import org.apache.tika.mime.MimeTypes
import org.jsoup.Connection
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import ro.readme.rotex.skipFileIfExists
import ro.readme.rotex.utils.PathUtils
import java.io.File
import java.io.InputStream
import java.io.PrintWriter
import java.security.MessageDigest


class RomaniaIneditForum: Source() {
    override val enabled = false
    override val sourceKey = "romania-inedit-forum"
    override val originalLink = "http://romania-inedit.3xforum.ro/topic/83/Carti_in_limba_romana/"
    override val downloadLink = ""

    private val tika = Tika(TikaConfig(javaClass.classLoader.getResourceAsStream("tika-config.xml")))

    private var digestSha1 = MessageDigest.getInstance("SHA-1")!!

    override fun downloadOriginal(override: Boolean) {
        val firstPageDoc = Jsoup
            .connect("http://romania-inedit.3xforum.ro/topic/83/Carti_in_limba_romana/")
            .timeout(60 * 1000)
            .get()

        val pageCount = getPageCount(firstPageDoc)

        for (page in 1..pageCount) {
            println("Topic page $page of $pageCount")
            val pageDoc = Jsoup
                .connect("http://romania-inedit.3xforum.ro/topic/83/Carti_in_limba_romana/$page/")
                .timeout(60 * 1000)
                .get()
            val topics = pageDoc.select(".puntopic")
            for (topic in topics) {
                val link = topic.select("a").first()
                downloadFromTopic(link, override)
            }
        }
    }

    private fun downloadFromTopic(link: Element, override: Boolean) {
        val topicFirstPageHref = link.attr("href")
        println("Topic $topicFirstPageHref")
        if (topicFirstPageHref.contains("B_Revista_REBUS")) {
            println("skipping B_Revista_REBUS")
            return
        }
        val topicUrl = "http://romania-inedit.3xforum.ro$topicFirstPageHref"
        val postDoc = Jsoup
            .connect(topicUrl)
            .timeout(60 * 1000)
            .get()
        val pageCount = getPageCount(postDoc)
        for (page in 1..pageCount) {
            println("Post page $page of $pageCount in topic $topicFirstPageHref")
            val pageHref = topicUrl.replace("/1/", "/$page/")
            downloadFromTopicPage(pageHref, override)
        }
    }

    private fun downloadFromTopicPage(pageHref: String, override: Boolean) {
        val postPageDoc = Jsoup
            .connect(pageHref)
            .timeout(60 * 1000)
            .get()

        val links = postPageDoc.select(".punplain a[target=\"_blank\"]")
        for (link in links) {
            val href = link.attr("href")

            if (href.startsWith("http") &&
                !href.startsWith("http://romania-inedit.3xforum.ro/")) {
                downloadFromExternal(href, override)
            }
        }
    }

    private fun downloadFromExternal(href: String, override: Boolean) {
        print("$href ... ")
        try {
            val doc = Jsoup
                .connect(href)
                .timeout(60 * 1000)
                .get()

            val downloadLinkData = findDownloadLinkByExternalHref(href, doc)

            if (null != downloadLinkData) {
                downloadFromLink(downloadLinkData, override)
            } else {
                println("DOWNLOAD LINK NOT FOUND: $href")
            }
        } catch (e: Exception) {
            println("ERR")
            e.printStackTrace()
        }
    }

    private fun downloadFromLink(downloadLinkData: ExternalDownloadLinkData, override: Boolean) {
        val downloadHref = downloadLinkData.href
        val response = Jsoup.connect(downloadHref)
            .method(downloadLinkData.method)
            .userAgent("Mozilla")
            .maxBodySize(200 * 1024 * 1024)
            .ignoreContentType(true)
            .timeout(30 * 60 * 1000)
            .execute()

        val extension = determineExtension(downloadHref, response)

        val fileName = when {
            downloadLinkData.fileName.isNullOrBlank() -> getFileName(downloadHref, extension)
            downloadLinkData.fileName.endsWith(extension) -> downloadLinkData.fileName
            downloadLinkData.fileName.isNotBlank() -> "${downloadLinkData.fileName}.$extension"
            else -> getFileName(downloadHref, extension)
        }

        val destinationFilePath = PathUtils.originalFilePath(sourceKey, fileName)
        print("to ${destinationFilePath.fileName} ... ")
        skipFileIfExists(destinationFilePath, override) {
            val stream = response.bodyStream()
            destinationFilePath.toFile().outputStream().use {
                stream.copyTo(it)
                println("OK")
            }
        }
    }

    private fun getFileName(downloadHref: String, extension: String): String {

        val lastPart = downloadHref.substringAfterLast("/")
        return if (!lastPart.isBlank() && lastPart.length < 128) {
            when {
                lastPart.endsWith("-$extension") -> {
                    val withoutDashExtension = lastPart.replace(Regex("-$extension$"), "")
                    "$withoutDashExtension.$extension"
                }
                lastPart.endsWith(".$extension") -> lastPart
                else -> "$lastPart.$extension"
            }
        } else {
            val downloadHashHex = Hex.encodeHex(digestSha1.digest(downloadHref.toByteArray()))
            "$downloadHashHex.$extension"
        }
    }

    data class ExternalDownloadLinkData(
        val href: String,
        val fileName: String? = null,
        val method: Connection.Method = Connection.Method.GET)

    private fun findDownloadLinkByExternalHref(pageHref: String, doc: Document): ExternalDownloadLinkData? {
        return when {
            pageHref.contains("zippy") -> ExternalDownloadLinkData("https://www13.zippyshare.com" +
                    doc
                        .select("a#dlbutton")
                        .first()
                        .attr("href"))

            pageHref.contains("mediafire") ->
                ExternalDownloadLinkData(doc
                    .select("a[aria-label=\"Download file\"]")
                    .first()
                    .attr("href"),
                    doc.select(".filename").first().text())

            pageHref.contains("yadi") -> {
                val name = doc.select(".file-name,.listing-item__title").first()
                ExternalDownloadLinkData(getYadiskDirectDownloadLink(pageHref),
                    name?.text()
                )
            }

            pageHref.contains("fileshare") -> {
                val formSubmit = doc.select("form input[value=\"DOWNLOAD NOW\"]").first()
                if ( null != formSubmit) {
                    ExternalDownloadLinkData(
                        formSubmit.parent().attr("action"),
                        doc.select(".textsize14 hr").text(),
                        Connection.Method.POST
                    )
                } else {
                    null
                }
            }

            else -> {
                val link = doc
                    .select("a:contains(Download),a:contains(Descărcați),a:contains(Descărcare)")
                    .first()

                if (null != link) {
                    ExternalDownloadLinkData(link.attr("href"))
                } else {
                    null
                }
            }
        }
    }

    private fun determineExtension(downloadHref: String, response: Connection.Response): String {
        when {
            downloadHref.endsWith("-pdf") -> return "pdf"
            downloadHref.endsWith("-doc") -> return "doc"
            downloadHref.endsWith("-docx") -> return "docx"
            downloadHref.endsWith("-rar") -> return "rar"
            downloadHref.endsWith("-zip") -> return "zip"
        }

        val contentDisposition = response.header("Content-Disposition")
        if (!contentDisposition.isNullOrBlank()) {
            val fileNameQuoted = contentDisposition.substringAfterLast("filename=")
            if (fileNameQuoted.isNotBlank()) {
                val fileName = fileNameQuoted.trim('"')
                if (fileName.isNotBlank()) {
                    val extension = fileName.substringAfterLast(".")
                    if (extension.isNotBlank()) {
                        return extension
                    }
                }
            }
        }

        val extensionFromHref = downloadHref.substringAfterLast(".")
        if (extensionFromHref.isNotBlank()) {
            return extensionFromHref
        }

        val contentType = response.header("Content-Type")
        if (!contentType.isNullOrBlank()) {
            val allTypes = MimeTypes.getDefaultMimeTypes()
            val type = allTypes.forName(contentType)
            return type.extension.substringAfterLast(".")
        }

        return "unknown"
    }

    private fun getPageCount(pageDoc: Document): Int {
        try {
            return pageDoc
                .select(".punspacer").last()
                .select("a[href^=/topic/],a[href^=/post/]").last()
                .text().toInt()
        } catch (e: Exception) {
            return 1
        }
    }

    override fun extractText(override: Boolean) {
//        val outputPath = PathUtils.textFilePath(sourceKey)
//        skipFileIfExists(outputPath, override) {
//            outputPath.toFile().printWriter().use { printWriter ->
//                val originalDirectoryPath = PathUtils.originalDirectoryPath(sourceKey)
//                originalDirectoryPath.toFile()
//                    .walk()
//                    .filter { it.isFile }
//                    .forEach { file ->
//                        // is archive?
//                        if (isArchive(file)) {
//                            // detect best entry
//
//                            val best = determineBestEntry(file)
//
//                            if (null == best) {
//                                println("No known entry found in ${file.name}, skipping")
//                                return@forEach
//                            }
//
//                            ArchiveStreamFactory().createArchiveInputStream(file.inputStream()).use { ais ->
//                                var entry = ais.nextEntry
//                                do {
//                                    if (entry.name == best.name) {
//                                        extractText(printWriter, ais.buffered())
//                                        return@forEach
//                                    }
//                                    entry = ais.nextEntry
//                                } while (null != entry)
//                            }
//                        } else {
//                            if (orderedExtensions.contains(file.extension)) {
//                                extractText(printWriter, file.inputStream())
//                            } else {
//                                println("Don't know how to process ${file.name}, skipping")
//                            }
//                        }
//                    }
//            }
//        }
    }

    private fun extractText(printWriter: PrintWriter, inputStream: InputStream) {
        val text = tika.parseToString(inputStream)
        printWriter.write(text)
    }

    data class ArchiveEntryInfo(val name: String, val extension: String, val size: Long)

    private fun isArchive(file: File): Boolean {
        return try {
            ArchiveStreamFactory.detect(file.inputStream())
            true
        } catch (e: ArchiveException) {
            false
        }
    }

    private val orderedExtensions = setOf("doc", "docx", "rtf", "epub", "txt", "pdf"/*, "djvu"*/)

    private fun determineBestEntry(file: File): ArchiveEntryInfo? {
        val entries = ArrayList<ArchiveEntryInfo>()
        ArchiveStreamFactory().createArchiveInputStream(file.inputStream()).use { ais ->
            var entry = ais.nextEntry
            do {
                entries.add(
                    ArchiveEntryInfo(
                        entry.name,
                        entry.name.substringAfterLast("."),
                        entry.size
                    )
                )
                entry = ais.nextEntry
            } while (null != entry)
        }

        entries.sortByDescending { it.size }

        for (extension in orderedExtensions) {
            for (entry in entries) {
                if (entry.extension == extension) return entry
            }
        }

        return null
    }

    private fun getYadiskDirectDownloadLink(href: String): String {
        val process = ProcessBuilder("yadisk-direct", href).start()
        process.waitFor()
        return process.inputStream.bufferedReader().readText()
    }
}
