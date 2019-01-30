package ro.readme.rotex

import java.util.*


object ConfigProperties {
    val dataDirectoryPath: String
        get() = properties.getProperty("dataDirectoryPath")

    const val originalDirectoryName = "original"
    const val textDirectoryName = "text"
    const val compressedTextDirectoryName = "text-compressed"
    const val dexDirectoryName = ".dex"

    private val properties by lazy {
        val properties = Properties()
        properties.load(javaClass.classLoader.getResource("config.properties").openStream())
        properties
    }
}
