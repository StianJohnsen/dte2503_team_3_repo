package com.example.dashcarr.extensions

import android.widget.Toast
import androidx.annotation.MainThread
import androidx.fragment.app.Fragment
import com.example.dashcarr.R

@MainThread
fun Fragment.toastThrowableShort(throwable: Throwable) {
    val message = throwable.message ?: getString(R.string.error_unknown)
    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
}