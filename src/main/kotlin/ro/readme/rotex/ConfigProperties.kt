package ro.readme.rotex

import java.nio.file.Paths
import java.util.*


object ConfigProperties {
    var path: String? = null

    fun initialize(path: String) {
        this.path = path
    }
    val dataDirectoryPath: String
        get() = properties.getProperty("dataDirectoryPath")

    const val originalDirectoryName = "original"
    const val textDirectoryName = "text"
    const val compressedTextDirectoryName = "text-compressed"
    const val dexDirectoryName = "dex"

    private val properties by lazy {
        val properties = Properties()
        println()
        if (null == path) {
            println("[WARNING] Using default configuration properties, data path might be invalid.")
            properties.load(javaClass.classLoader.getResource("config.properties").openStream())
        } else {
            println("Using configuration properties from $path.")
            properties.load(Paths.get(path).toFile().inputStream())
        }
        println()
        properties
    }
}
