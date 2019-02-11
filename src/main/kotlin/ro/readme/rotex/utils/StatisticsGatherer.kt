package ro.readme.rotex.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import ro.readme.rotex.sources.AllSource
import ro.readme.rotex.sources.Source
import java.io.BufferedReader
import java.io.FileReader

class StatisticsGatherer {
    fun get(sources: List<Source>, includeAll: Boolean = true): List<StatisticsData> {
        val statisticsDataList = ArrayList<StatisticsData>()
        runBlocking {
            for (source in sources) {
                launch(Dispatchers.IO) {
                    val statisticsData = get(source)
                    if (null != statisticsData) {
                        statisticsDataList.add(statisticsData)
                        println("Computing for ${source.sourceKey} OK")
                    } else {
                        println("${source.sourceKey} text file does not exists, skipping.")
                    }
                }
            }
        }
        if (includeAll) {
            val all = totalOf(statisticsDataList)
            statisticsDataList.add(all)
        }
        println()
        return statisticsDataList
    }

    fun printList(list: List<StatisticsData>) {
        list.forEach { it.printLines() }
    }

    fun printTable(list: List<StatisticsData>) {
        StatisticsData.printMarkDownHeader()
        list.forEach { it.printMarkDownRow() }
    }

    fun get(source: Source): StatisticsData? {
        val sourceKey = source.sourceKey
        val textFilePath = PathUtils.textFilePath(sourceKey)
        val textFile = textFilePath.toFile()
        if (!textFile.exists()) {
            return null
        }

        val fileSize = textFile.length()

        val compressedTextFilePath = PathUtils.compressedTextFilePath(sourceKey)
        val compressedTextFile = compressedTextFilePath.toFile()
        val compressedFileSize = compressedTextFile.length()

        var wordCount = 0
        var inDex = 0

        val wordSet = HashSet<String>()
        FileReader(textFile).use { fileReader ->
            BufferedReader(fileReader).use { bufferedReader ->
                bufferedReader.use { reader ->
                    val tokenizer = SimpleTokenizer()
                    var lineString = reader.readLine()
                    while (null != lineString) {
                        val allLettersOnly = tokenizer.tokenize(lineString)
                            .map { it.getCoveredText(lineString) }
                            .filter { TextUtils.hasOnlyLetters(it) }
                        wordCount += allLettersOnly.size
                        inDex += allLettersOnly.filter { DexDictionary.containsWord(it) }.size
                        wordSet.addAll(allLettersOnly)
                        lineString = reader.readLine()
                    }
                }
            }
        }

        return StatisticsData(source,
            fileSize,
            compressedFileSize,
            wordCount,
            inDex,
            wordSet)
    }

    private fun totalOf(list: List<StatisticsData>): StatisticsData {
        var fileSizeUncompressed = 0L
        var fileSizeCompressed = 0L
        var totalWordCount = 0
        var inDex = 0
        val dexWordSet = HashSet<String>()
        for (d in list) {
            fileSizeUncompressed += d.fileSizeUncompressed
            fileSizeCompressed += d.fileSizeCompressed
            totalWordCount += d.totalWordCount
            inDex += d.totalWordsInDexCount
            dexWordSet.addAll(d.wordSet)
        }

        return StatisticsData(
            AllSource(),
            fileSizeUncompressed,
            fileSizeCompressed,
            totalWordCount,
            inDex,
            dexWordSet)
    }
}
