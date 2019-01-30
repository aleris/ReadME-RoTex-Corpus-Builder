package ro.readme.rotex.sources

class AllSource: Source {
    override val sourceKey = "all"
    override val originalLink = "n/a"
    override val downloadLink = ""

    override fun downloadOriginal(override: Boolean) {
        // ignore
    }

    override fun extractText(override: Boolean) {
        // ignore
    }
}
