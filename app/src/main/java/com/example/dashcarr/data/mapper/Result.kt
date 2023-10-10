package com.example.dashcarr.data.mapper

suspend fun <T: Any> safeCall(
    call: suspend () -> Result<T>
): Result<T> {
    return try {
        call()
    }
    catch (e: Exception) {
        e.printStackTrace()
        Result.failure(e)
    }
}