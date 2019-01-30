package ro.readme.rotex

class BuildSourceOptions(val override: Boolean = false,
                         val checkOriginalDeep: Boolean = false
) {
    companion object {
        val DEFAULT = BuildSourceOptions()
    }
}
