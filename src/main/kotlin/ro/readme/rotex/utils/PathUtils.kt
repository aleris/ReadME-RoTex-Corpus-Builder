package ro.readme.rotex.utils

import ro.readme.rotex.ConfigProperties
import java.nio.file.Path
import java.nio.file.Paths

object PathUtils {
    fun textFilePath(sourceKey: String): Path =
        Paths.get(
            ConfigProperties.dataDirectoryPath,
            ConfigProperties.textDirectoryName,
            "$sourceKey.txt"
        )

    fun originalDirectoryPath(sourceKey: String): Path =
        Paths.get(
            ConfigProperties.dataDirectoryPath,
            ConfigProperties.originalDirectoryName,
            sourceKey
        )

    fun originalFilePath(sourceKey: String, fileName: String): Path =
        Paths.get(
            ConfigProperties.dataDirectoryPath,
            ConfigProperties.originalDirectoryName,
            sourceKey,
            fileName
        )

    fun originalFilePath(sourceKey: String, subDirectoryName1: String, fileName: String): Path =
        Paths.get(
            ConfigProperties.dataDirectoryPath,
            ConfigProperties.originalDirectoryName,
            sourceKey,
            subDirectoryName1,
            fileName
        )

    fun originalFilePath(sourceKey: String, subDirectoryName1: String, subDirectoryName2: String, fileName: String): Path =
        Paths.get(
            ConfigProperties.dataDirectoryPath,
            ConfigProperties.originalDirectoryName,
            sourceKey,
            subDirectoryName1,
            subDirectoryName2,
            fileName
        )

    fun originalFilePath(sourceKey: String, subDirectoryName1: String, subDirectoryName2: String, subDirectoryName3: String, fileName: String): Path =
        Paths.get(
            ConfigProperties.dataDirectoryPath,
            ConfigProperties.originalDirectoryName,
            sourceKey,
            subDirectoryName1,
            subDirectoryName2,
            subDirectoryName3,
            fileName
        )

    fun compressedTextFilePath(sourceKey: String): Path =
        Paths.get(
            ConfigProperties.dataDirectoryPath,
            ConfigProperties.compressedTextDirectoryName,
            "$sourceKey.txt.tar.gz"
        )
}
