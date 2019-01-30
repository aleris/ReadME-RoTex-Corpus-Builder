package ro.readme.rotex

import java.net.SocketException
import java.net.SocketTimeoutException
import java.nio.file.Path

fun retrySocketTimeoutException(maxRetries: Int, body: (currentRetry: Int) -> Any) {
    var currentRetry = maxRetries
    do {
        try {
            body(currentRetry)
            currentRetry = 0
        } catch (e: Exception) {
            when(e) {
                is SocketTimeoutException, is SocketException -> {
                    currentRetry--
                    if (currentRetry == 0) {
                        throw e
                    }
                    Thread.sleep(3000)
                }
                else -> throw e
            }
        }
    }
    while (0 < currentRetry)
}

fun skipFileIfExists(path: Path, override: Boolean, body: () -> Any) {
    val file = path.toFile()
    if (!file.exists() || override ) {
        file.parentFile.mkdirs()
        body()
    } else {
        println("exists, skip no override")
    }
}
