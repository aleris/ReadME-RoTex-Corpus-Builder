package ro.readme.rotex

import org.apache.commons.compress.archivers.tar.TarArchiveEntry
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream
import ro.readme.rotex.sources.*
import java.nio.file.Files
import org.reflections.Reflections
import ro.readme.rotex.utils.*
import java.nio.file.Paths
import kotlin.reflect.full.createInstance


class BuildController {
    private val allBuildSources by lazy {
        val reflections = Reflections(Source::class.java.`package`.name)
        val sourceClasses = reflections.getSubTypesOf(Source::class.java)

        return@lazy sourceClasses.map {
            val source = it.kotlin.createInstance()
            BuildSource(source)
        }.filter { it.source.sourceKey != AllSource().sourceKey }
    }

    private fun runBuildSources(list: List<BuildSource>) {
        println("ReadME RoTex Corpus Build")
        println()

        for (buildSource in list) {
            println()
            println(buildSource.source.sourceKey)
            println("".padEnd(buildSource.source.sourceKey.length, '-'))
            downloadOriginal(buildSource.source, buildSource.options)

            extractText(buildSource.source, buildSource.options)

            compressInSourceTextDirectory(buildSource.source, buildSource.options)
            println("Build ${buildSource.source.sourceKey} OK")
        }

        buildAllCompressedFile()

        println("Gathering statistics...")
        DexDictionary.ensureLoaded()
        println()

        val statisticsGatherer = StatisticsGatherer()
        val statisticsDataList = statisticsGatherer.get(list.map { it.source })
        statisticsGatherer.printTable(statisticsDataList.sortedBy { it.totalWordCount })

        println()
        println()
        println("ReadME RoTex Corpus Build DONE")
    }

    fun run(list: List<BuildSourceConfig>) {
        val buildSources = list.map { config ->
            allBuildSources.find { buildSource ->
                buildSource.source.sourceKey == config.sourceKey
            }!!
        }
        runBuildSources(buildSources)
    }

    fun run() {
//        runBuildSources(listOf(
//            DcepSource()
//        ).map { BuildSource(it) })
//         , BuildSourceOptions(checkOriginalDeep = true)

        runBuildSources(allBuildSources)
    }

    private fun downloadOriginal(source: Source, options: BuildSourceOptions) {
        println("[DOWNLOAD] ${source.sourceKey}")
        if (!options.override &&
            !options.checkOriginalDeep &&
            PathUtils.originalDirectoryPath(source.sourceKey).toFile().exists()) {
            println("${source.sourceKey} exists, skipping download without deep checking")
            return
        }
        source.downloadOriginal(options.override)
    }

    private fun extractText(source: Source, options: BuildSourceOptions) {
        println("[EXTRACT] ${source.sourceKey}")
        source.extractText(options.override)
    }

    private fun compressInSourceTextDirectory(source: Source, options: BuildSourceOptions) {
        val sourceKey = source.sourceKey
        val filePath = PathUtils.textFilePath(sourceKey)
        if (filePath.toFile().exists()) {
            val destinationPath = PathUtils.compressedTextFilePath(sourceKey)
            println("[COMPRESS] ${source.sourceKey}")
            print("Compressing to $destinationPath ... ")
            skipFileIfExists(destinationPath, options.override) {
                destinationPath.toFile().parentFile.mkdirs()
                Files.newOutputStream(destinationPath).use { fo ->
                    GzipCompressorOutputStream(fo).use { gzo ->
                        TarArchiveOutputStream(gzo).use { tar ->
                            val file = filePath.toFile()
                            val entry = tar.createArchiveEntry(file, file.name)
                            tar.putArchiveEntry(entry)
                            Files.newInputStream(filePath).use { i ->
                                i.copyTo(tar)
                            }
                            tar.closeArchiveEntry()
                            tar.finish()
                        }
                    }
                }
                println("OK")
            }
        }
    }

    private fun buildAllCompressedFile() {
        val source = AllSource()
        println()
        println("[COMPRESS] ${source.sourceKey}")
        val destinationPath = PathUtils.compressedTextFilePath(source.sourceKey)
        skipFileIfExists(destinationPath, false) {
            val destinationFile = destinationPath.toFile()
            destinationFile.outputStream().use { outputStream ->
                GzipCompressorOutputStream(outputStream).use { gzo ->
                    TarArchiveOutputStream(gzo).use { tar ->
                        val entry = TarArchiveEntry("${source.sourceKey}.txt")

                        val files = Paths.get(
                            ConfigProperties.dataDirectoryPath,
                            ConfigProperties.textDirectoryName
                        ).toFile()
                            .listFiles()
                            .filter { it.extension == "txt" }
                            .sorted()

                        entry.size = files.fold(0L) { a, f -> a + f.length()}

                        tar.putArchiveEntry(entry)

                        files.forEach { file ->
                            print("${file.name} ... ")
                            file.inputStream().copyTo(tar)
                            println("OK")
                        }
                        tar.closeArchiveEntry()
                        tar.finish()
                    }
                }
            }
            println("OK")
        }
    }
}
