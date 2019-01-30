package ro.readme.rotex.utils

import org.apache.commons.collections4.trie.PatriciaTrie
import org.apache.commons.lang3.SerializationUtils
import ro.readme.rotex.ConfigProperties
import java.io.File
import java.nio.file.Paths
import java.sql.DriverManager

object DexDictionary {

    private const val dexDbUrl = "jdbc:mariadb://localhost/DEX?user=root&password=DEX"

    private val wordsTrie by lazy {
        val wordsTrieFilePath = Paths.get(
            ConfigProperties.dataDirectoryPath,
            ConfigProperties.dexDirectoryName,
            "wordsTrie.bin"
        )

        val wordsTrieFile = wordsTrieFilePath.toFile()
        if (!wordsTrieFile.exists()) {
            buildWordsTrieFromDexDb(wordsTrieFile)
        } else {
            loadWordsTrieFromFile(wordsTrieFile)
        }
    }

    fun ensureLoaded() {
        println("There are ${TextUtils.formatDecimal(wordsTrie.size)} word forms in dictionary.")
    }

    private fun loadWordsTrieFromFile(wordsTrieFile: File): PatriciaTrie<Void> {
        print("Loading word forms trie from ${wordsTrieFile.absolutePath} ... ")
        val wordsTrie: PatriciaTrie<Void> = SerializationUtils.deserialize(wordsTrieFile.inputStream())
        println("OK")
        return wordsTrie
    }

    private fun buildWordsTrieFromDexDb(wordsTrieFile: File): PatriciaTrie<Void> {
        print("Words trie in ${wordsTrieFile.absolutePath} not found, building ... ")
        val wordsTrie = PatriciaTrie<Void>()
        DriverManager.getConnection(dexDbUrl).use { conn ->
            val query = conn.prepareStatement("select formUtf8General from InflectedForm")
            query.fetchSize = 1000
            val r = query.executeQuery()
            while (r.next()) {
                val word = r.getString(1).toLowerCase()
                wordsTrie[word] = null
            }
        }
        println("OK")
        print("Saving words trie to ${wordsTrieFile.absolutePath} ... ")
        wordsTrieFile.parentFile.mkdirs()
        SerializationUtils.serialize(wordsTrie, wordsTrieFile.outputStream())
        println("OK")
        return wordsTrie
    }

    fun containsWord(word: String) = wordsTrie.contains(word.toLowerCase())

    val wordCount = wordsTrie.size

    fun isLikelyInRomanian(text: String): Boolean {
        var total = 0
        var inDex = 0
        SimpleTokenizer().tokenize(text).forEach {
            val word = it.getCoveredText(text)
            if (TextUtils.hasOnlyLetters(word)) {
                total++
                if (containsWord(word)) {
                    inDex++
                }
            }
        }
        return (total * 3 / 4) < inDex
    }
}
