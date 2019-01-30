package ro.readme.rotex

import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream
import org.apache.commons.compress.utils.IOUtils
import ro.readme.rotex.sources.*
import java.nio.file.Files
import org.reflections.Reflections
import ro.readme.rotex.utils.*
import kotlin.reflect.full.createInstance


class BuildController {
    private val allBuildSources by lazy {
        val reflections = Reflections(Source::class.java.`package`.name)
        val sourceClasses = reflections.getSubTypesOf(Source::class.java)

        return@lazy sourceClasses.map {
            val source = it.kotlin.createInstance()
            BuildSource(source)
        }
    }

    private fun runBuildSources(list: List<BuildSource>) {
        println("ReadME RoTex Corpus Build")
        for (buildSource in list) {
            println("Running build source on ${buildSource.source.sourceKey}:")
            downloadOriginal(buildSource.source, buildSource.options)

//            extractText(buildSource.source, buildSource.options)
//
//            compressInSourceTextDirectory(buildSource.source, buildSource.options)
//            println("Build ${buildSource.source.sourceKey} OK")
        }

//        println("Gathering statistics...")
//        DexDictionary.ensureLoaded()
//        println()
//
//        val statisticsGatherer = StatisticsGatherer()
//        val statisticsDataList = statisticsGatherer.get(list.map { it.source })
//        statisticsGatherer.printTable(statisticsDataList.sortedBy { it.totalWordCount })
//
//        println()
//        println()
//        println("ReadME RoTex Corpus Build DONE")
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
        runBuildSources(listOf(
            JustSource()
        ).map { BuildSource(it, BuildSourceOptions(checkOverrideDeep = true)) })
        // , BuildSourceOptions(checkOverrideDeep = true)

//        runBuildSources(allBuildSources)
    }

    private fun downloadOriginal(source: Source, options: BuildSourceOptions) {
        if (!options.override &&
            !options.checkOverrideDeep &&
            PathUtils.originalDirectoryPath(source.sourceKey).toFile().exists()) {
            println("${source.sourceKey} exists, skipping download without deep checking")
            return
        }
        source.downloadOriginal(options.override)
    }

    private fun extractText(source: Source, options: BuildSourceOptions) {
        source.extractText(options.override)
    }

    private fun compressInSourceTextDirectory(source: Source, options: BuildSourceOptions) {
        val sourceKey = source.sourceKey
        val filePath = PathUtils.textFilePath(sourceKey)
        val destinationPath = PathUtils.compressedTextFilePath(sourceKey)
        print("Compressing to $destinationPath ... ")
        skipFileIfExists(destinationPath, options.override) {
            destinationPath.toFile().parentFile.mkdirs()
            Files.newOutputStream(destinationPath).use { fo ->
                GzipCompressorOutputStream(fo).use { gzo ->
                    TarArchiveOutputStream(gzo).use { o ->
                        val file = filePath.toFile()
                        val entry = o.createArchiveEntry(file, file.name)
                        o.putArchiveEntry(entry)
                        Files.newInputStream(filePath).use { i ->
                            i.copyTo(o)
                        }
                        o.closeArchiveEntry()
                        o.finish()
                    }
                }
            }
            println("OK")
        }
    }
}
