package com.example.dashcarr.extensions

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun getFormattedDate(creationDate: Long): String =
    SimpleDateFormat(PROJECT_DATE_FORMAT, Locale.getDefault()).format(Date(creationDate))