package com.example.dashcarr.extensions

import android.widget.Toast
import androidx.annotation.MainThread
import androidx.fragment.app.Fragment
import com.example.dashcarr.R

/**
 * Shows a short-duration toast message with the error information from a throwable.
 *
 * This extension function is designed for use within a [Fragment]. It extracts the error message
 * from the provided [throwable] and displays it as a short-duration toast.
 *
 * @param throwable The throwable from which to extract the error message.
 */
@MainThread
fun Fragment.toastThrowableShort(throwable: Throwable) {
    val message = throwable.message ?: getString(R.string.error_unknown)
    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
}

@MainThread
fun Fragment.toastErrorUnknownShort() {
    Toast.makeText(requireContext(), getString(R.string.error_unknown), Toast.LENGTH_SHORT).show()
}

@MainThread
fun Fragment.toastShort(message: String) {
    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
}