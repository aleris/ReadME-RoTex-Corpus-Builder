package ro.readme.rotex.utils

import ro.readme.rotex.sources.Source

data class StatisticsData(val source: Source,
                          val fileSizeUncompressed: Long,
                          val fileSizeCompressed: Long,
                          val totalWordCount: Int,
                          val wordSet: HashSet<String>
                          ) {
    fun printLines() {
        println("Statistics for ${source.sourceKey}:")
        println("Original link: ${source.originalLink}")
        println("Download link: ${source.downloadLink}")
        println("Text file size (uncompressed): ${TextUtils.formatDecimal(fileSizeUncompressed / (1000 * 1000))} MB (${TextUtils.formatDecimal(fileSizeUncompressed)} bytes)")
        println("Text file size (compressed): ${TextUtils.formatDecimal(fileSizeCompressed / (1000 * 1000))} MB (${TextUtils.formatDecimal(fileSizeCompressed)} bytes)")
        println("Total words count: ${TextUtils.formatDecimal(totalWordCount)}")
        val totalTypesCount = wordSet.size
        println("Total types (unique words) count: ${TextUtils.formatDecimal(totalTypesCount)}")
        val typesCountAlsoInDex = wordSet.count { DexDictionary.containsWord(it) }
        val typesCountTotalInDex = DexDictionary.wordCount
        val typesInDexPercent = TextUtils.formatPercent(typesCountAlsoInDex.toDouble() * 100 / typesCountTotalInDex)
        println("DEX types coverage: $typesInDexPercent (${TextUtils.formatDecimal(typesCountAlsoInDex)} of ${TextUtils.formatDecimal(typesCountTotalInDex)})")
        println()
    }

    companion object {
        fun printMarkDownHeader() {
            println("Source Key | Source Link | Uncompressed size | Compressed size | Word Count | Types Count | DEX Coverage | Download")
        }
    }

    fun printMarkDownRow() {
        print(source.sourceKey)
        print(" | ")
        print(source.originalLink)
        print(" | ")
        print(source.downloadLink)
        print(" | ")
        print("${TextUtils.formatDecimal(fileSizeUncompressed / (1000 * 1000))} MB (${TextUtils.formatDecimal(fileSizeUncompressed)} bytes)")
        print(" | ")
        print("${TextUtils.formatDecimal(fileSizeCompressed / (1000 * 1000))} MB (${TextUtils.formatDecimal(fileSizeCompressed)} bytes)")
        print(" | ")
        print(TextUtils.formatDecimal(totalWordCount))
        print(" | ")
        val totalTypesCount = wordSet.size
        print(TextUtils.formatDecimal(totalTypesCount))
        print(" | ")
        val typesCountAlsoInDex = wordSet.count { DexDictionary.containsWord(it) }
        val typesCountTotalInDex = DexDictionary.wordCount
        val typesInDexPercent = TextUtils.formatPercent(typesCountAlsoInDex.toDouble() * 100 / typesCountTotalInDex)
        print("$typesInDexPercent (${TextUtils.formatDecimal(typesCountAlsoInDex)} of ${TextUtils.formatDecimal(typesCountTotalInDex)})")
        print(" | ")
        print("[Download](${source.downloadLink})")
        println()
    }
}