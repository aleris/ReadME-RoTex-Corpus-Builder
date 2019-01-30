package ro.readme.rotex.sources

class AllSource: Source {
    override val sourceKey = "all-readme-rotex"
    override val originalLink = "https://github.com/aleris/ReadME-RoTex-Corpus-Builder"
    override val downloadLink = "https://drive.google.com/open?id=1A-emtgS2QOjDtGNxR6VHgsM_EZvzOdvE"

    override fun downloadOriginal(override: Boolean) {
        // ignore
    }

    override fun extractText(override: Boolean) {
        // ignore
    }
}
