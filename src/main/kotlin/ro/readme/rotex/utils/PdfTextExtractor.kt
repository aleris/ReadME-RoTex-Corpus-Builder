package ro.readme.rotex.utils

import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper
import ro.readme.rotex.ConfigProperties
import java.io.File
import java.io.PrintWriter


class PdfTextExtractor(private val forceOCR: Boolean = false,
                       private val redoOCR: Boolean = false,
                       private val additionalTextClean: ((text: String) -> String)? = null,
                       private val minimumPages: Int = 0,
                       private val checkInRomanian: Boolean = false): TextExtractor() {

    private val textStripper = PDFTextStripper()

    override fun extractAll(sourceKey: String, override: Boolean) {
        super.extractAll(sourceKey, override, ".pdf")
    }

    override fun extract(inputFile: File, printWriter: PrintWriter) {
//        printWriter.println(inputFile.nameWithoutExtension)

        print("${inputFile.name} ... ")

        try {
            var useInputFile = inputFile
            if (forceOCR or redoOCR) {
                useInputFile = ocrFile(inputFile, forceOCR, redoOCR, Runtime.getRuntime().availableProcessors())
            }

            PDDocument.load(useInputFile).use { doc ->
                val pageCount = doc.pages.count
                if (minimumPages <= pageCount) {
                    val repeatedMap = buildRepeatedLinesMap(doc)

                    if (checkInRomanian) {
                        val firsPagesBuilder = getFirstPagesText(doc, 10)

                        if (!DexDictionary.isLikelyInRomanian(firsPagesBuilder.toString())) {
                            println("skipped, not in romanian")
                            return
                        }
                    }

                    for (index in 0 until pageCount) {
                        val cleaned = getPageCleanedText(doc, index, additionalTextClean, true)

                        val lines = cleaned.lines()
                        val lineCount = lines.size
                        lines.withIndex().forEach { (index, line) ->
                            val trimmedLine = line.trim()
                            val mappedLine = stripMappedLine(trimmedLine    )
                            var ignore = false
                            if (repeatedMap.containsKey(mappedLine)) { // probably header/footer text
                                ignore = true
                            }

                            if (isOnPageEdges(index, lineCount) && looksLikePageNumber(trimmedLine)) {
                                ignore = true
                            }

                            if (!ignore) {
                                printWriter.println(trimmedLine)
                            }
                        }
                    }

                    printWriter.println()
                } else {
                    println("skip, $pageCount pages, minimum is $minimumPages")
                }
            }
            println("OK")
        } catch (e: Exception) {
            println("ERR")
            e.printStackTrace()
        }
    }

    private fun getFirstPagesText(
        doc: PDDocument,
        maxPagesCount: Int
    ): StringBuilder {
        val firsPagesBuilder = StringBuilder()
        for (index in 0 until Math.max(maxPagesCount, doc.pages.count)) {
            val cleaned = getPageCleanedText(doc, index, additionalTextClean, false)
            firsPagesBuilder.append(cleaned)
        }
        return firsPagesBuilder
    }

    private fun getPageCleanedText(
        doc: PDDocument,
        pageIndex: Int,
        additionalTextClean: ((text: String) -> String)? = null,
        fixLines: Boolean
    ): String {
        textStripper.startPage = pageIndex + 1
        textStripper.endPage = pageIndex + 1
        val pageText = textStripper.getText(doc)

        val textCleaner = TextCleaner(pageText)
            .correctCedilaDiacritics()

        if (null != additionalTextClean) {
            textCleaner.cleaned = additionalTextClean(textCleaner.cleaned)
        }

        if (fixLines) {
            textCleaner
                .replaceMultipleSpacesWithNewLine()
                .stitchLines()
        }

        return textCleaner.cleaned
    }

    fun countPages(inputFile: File): Int {
        PDDocument.load(inputFile).use {
            return it.pages.count
        }
    }

    private fun looksLikePageNumber(trimmedLine: String) =
        trimmedLine.matches(Regex("(Pag\\.|Pagina|p.)?\\s*\\d+(\\s+\\d+)?", RegexOption.IGNORE_CASE))

    private fun isOnPageEdges(lineIndex: Int, totalLineCount: Int, edgesLineCount: Int = 4): Boolean =
        lineIndex in 0..edgesLineCount ||
                lineIndex in totalLineCount - edgesLineCount..totalLineCount

    private fun buildRepeatedLinesMap(doc: PDDocument): Map<String, Int> {
        val repeatedHap = HashMap<String, Int>()

        for (index in 0 until doc.pages.count) {
            val cleaned = getPageCleanedText(doc, index, additionalTextClean, true)

            val lines = cleaned.lines()
            lines.withIndex().forEach { (index, line) ->
                val mappedLine = stripMappedLine(line)
                if (mappedLine.isNotBlank()) {
                    repeatedHap.compute(mappedLine) { _, v -> if (null == v) 1 else v + 1 }
                }
            }
        }

        val threshold = Math.max(5, doc.pages.count / 5)
        return repeatedHap.filter { me -> threshold < me.value }
    }

    private fun stripMappedLine(line: String) = line.toLowerCase()
        .replace(Regex("^(?:\\d *)+", RegexOption.MULTILINE), "")
        .replace(Regex("(?:\\d *)+$", RegexOption.MULTILINE), "")
        .replace(Regex("\\s(?:\\d *)+\\s", RegexOption.MULTILINE), "")
        .replace(Regex("(?:\\d *)+\\s+(?:\\d *)+", RegexOption.MULTILINE), "")
        .replace(Regex("[IVXLCDM]+", RegexOption.MULTILINE), "")

    private fun ocrFile(inputFile: File, forceOCR: Boolean, redoOCR: Boolean, jobCount: Int): File {
        val dataDirectoryFile = File(ConfigProperties.dataDirectoryPath)

        val relativeFilePath = inputFile
            .relativeTo(dataDirectoryFile)
            .toPath().toString()

        val outputFile = PathUtils.textFileIntermediaryPath(inputFile.parentFile.name, inputFile.name)
            .toFile()
        if (outputFile.exists()) {
            return outputFile
        }

        println()
        println("Applying OCR ... ")

        outputFile.parentFile.mkdirs()

        val relativeOutputPath = outputFile
            .relativeTo(dataDirectoryFile)
            .toPath().toString()

        val params = arrayListOf("docker", "run", "--rm",
            "-v", "${ConfigProperties.dataDirectoryPath}:/home/docker",
            "ocrmypdf",
            "-l", "ron",
            "--jobs", jobCount.toString()//,
            //"--remove-vectors"//,
//            "--clean-final"//,
            //"--optimize", "3"
        )

        if (forceOCR) {
            params.add("--force-ocr")
        }

        if (redoOCR) {
            params.add("--redo-ocr")
        }

        val process = ProcessBuilder(*params.toTypedArray(),
            relativeFilePath,
            relativeOutputPath
        ).start()

        val errorStringBuilder = StringBuilder()
        process.errorStream.bufferedReader().lines().forEach {
            println(it)
            errorStringBuilder.appendln(it)
        }
        process.inputStream.bufferedReader().lines().forEach {
            println(it)
        }

        process.waitFor()

        if (errorStringBuilder.contains("ERROR")) {
            throw Exception(errorStringBuilder.toString())
        }

        println("OCR OK")

        return outputFile
    }
}
