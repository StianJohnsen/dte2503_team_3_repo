package com.example.dashcarr.extensions

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment

/**
 * Extension function for Activity to hide the keyboard.
 * If a target view is provided, the keyboard is hidden for that specific view.
 * Otherwise, it sets the soft input mode of the window to always hide the soft keyboard.
 *
 * @param target The target view for which the keyboard should be hidden. Can be null.
 */
fun Fragment.hideKeyboard() {
    requireActivity().hideKeyboard(view)
}

fun Activity.hideKeyboard(target: View?) {
    val keyboard = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

    target?.let {
        keyboard.hideSoftInputFromWindow(target.windowToken, 0)
    } ?: let {
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
    }
}