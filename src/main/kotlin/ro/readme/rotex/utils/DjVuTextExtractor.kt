package ro.readme.rotex.utils

import java.io.File
import java.io.PrintWriter
import java.nio.file.Path

class DjVuTextExtractor(private val additionalTextClean: ((text: String) -> String)? = null,
                        private val checkInRomanian: Boolean = false): TextExtractor() {
    override fun extractAll(sourceKey: String, override: Boolean) {
        super.extractAll(sourceKey, override, ".djvu")
    }

    override fun extract(inputFile: File, printWriter: PrintWriter) {
        print("${inputFile.name} ... ")

        // the processing bellow is a simplified version of the one in PDF and some areas are duplicated
        // TODO: consider refactoring to a common PDF & DJVU extractor

        val tempTxtPath = PathUtils.textFileIntermediaryPath(inputFile.parentFile.name, inputFile.name)

        if (!tempTxtPath.toFile().exists()) {
            extractTextToTempTxtFile(inputFile, tempTxtPath)
        }

        val text = tempTxtPath.toFile().readText()

        if (text.isNotBlank()) {
            if (checkInRomanian) {
                if (!DexDictionary.isLikelyInRomanian(text)) {
                    println("skipped, not in romanian")
                    return
                }
            }

            val textCleaned = getCleanedText(text, true)

            printWriter.print(textCleaned)

        } else {
            println("no text")
        }
    }


    private fun looksLikePageNumber(trimmedLine: String) =
        trimmedLine.matches(Regex("(Pag\\.|Pagina|p.)?\\s*\\d+(\\s+\\d+)?", RegexOption.IGNORE_CASE))

    private fun getCleanedText(
        text: String,
        fixLines: Boolean
    ): String {
        val textCleaner = TextCleaner(text)
            .correctCedilaDiacritics()
            .removeDuplicateLinesIgnoringNumbers()
            .removeLikelyPageNumber()

        if (null != additionalTextClean) {
            textCleaner.cleaned = additionalTextClean.invoke(textCleaner.cleaned)
        }

        if (fixLines) {
            textCleaner
                .replaceMultipleSpacesWithNewLine()
                .stitchLinesSimple()
        }

        return textCleaner.cleaned
    }

    private fun extractTextToTempTxtFile(inputFile: File, txtFilePath: Path) {
        txtFilePath.parent.toFile().mkdirs()
        val process = ProcessBuilder("djvutxt", inputFile.absolutePath, txtFilePath.toString()).start()
        process.waitFor()
    }
}
