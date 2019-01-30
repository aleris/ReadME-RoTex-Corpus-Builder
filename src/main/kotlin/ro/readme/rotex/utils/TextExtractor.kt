package ro.readme.rotex.utils

import ro.readme.rotex.skipFileIfExists
import java.io.File
import java.io.PrintWriter
import java.nio.file.Path

abstract class TextExtractor {
    abstract fun extract(inputFile: File, printWriter: PrintWriter)

    abstract fun extractAll(sourceKey: String, override: Boolean)

    protected fun extractAll(sourceKey: String, override: Boolean, inputFilesExtension: String) {
        extractAll(override,
            PathUtils.originalDirectoryPath(sourceKey),
            inputFilesExtension,
            PathUtils.textFilePath(sourceKey)
        )
    }

    private fun extractAll(override: Boolean, inputDirectoryPath: Path, inputFilesExtension: String, outputFilePath: Path) {
        skipFileIfExists(outputFilePath, override) {
            outputFilePath.toFile().printWriter().use { printWriter ->
                inputDirectoryPath.toFile()
                    .walk()
                    .sorted()
                    .filter { it.isFile }
                    .filter { it.name.endsWith(inputFilesExtension) }
                    .forEach {
                        extract(it, printWriter)
                    }
            }
        }
    }
}
