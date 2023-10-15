package com.example.dashcarr.data.mapper

/**
 * A utility function for safely making a suspended call and handling exceptions.
 *
 * @param call The suspended function call to be executed.
 * @return A [Result] containing the result of the call. On success, [Result.success] is returned,
 *         and on failure, [Result.failure] with the corresponding exception is returned.
 */
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