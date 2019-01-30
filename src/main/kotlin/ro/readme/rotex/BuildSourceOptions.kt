package ro.readme.rotex

class BuildSourceOptions(val override: Boolean = false,
                         val checkOverrideDeep: Boolean = false
) {
    companion object {
        val DEFAULT = BuildSourceOptions()
    }
}
