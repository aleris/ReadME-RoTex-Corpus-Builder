package ro.readme.rotex.sources

interface Source {
    val sourceKey: String

    val originalLink: String

    val downloadLink: String

    fun downloadOriginal(override: Boolean)

    fun extractText(override: Boolean)
}
