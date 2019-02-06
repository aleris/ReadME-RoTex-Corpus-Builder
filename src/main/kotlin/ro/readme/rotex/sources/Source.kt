package ro.readme.rotex.sources

abstract class Source {
    open val enabled = true

    abstract val sourceKey: String

    abstract val originalLink: String

    abstract val downloadLink: String

    abstract fun downloadOriginal(override: Boolean)

    abstract fun extractText(override: Boolean)
}
