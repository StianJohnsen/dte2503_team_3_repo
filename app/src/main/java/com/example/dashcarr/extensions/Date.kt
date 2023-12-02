package com.example.dashcarr.extensions

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Formats a given timestamp into a readable date string using the [PROJECT_DATE_TIME_FORMAT].
 *
 * @param creationDate The timestamp to be formatted.
 * @return A formatted date string.
 */
fun getFormattedDate(creationDate: Long): String =
    SimpleDateFormat(PROJECT_DATE_TIME_FORMAT, Locale.getDefault()).format(Date(creationDate))