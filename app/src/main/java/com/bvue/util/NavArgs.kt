package com.bvue.util

import android.util.Base64

/** URL-safe encoding for passing arbitrary strings (e.g. channel URLs) as navigation arguments. */
fun encodeArg(value: String): String =
    Base64.encodeToString(value.toByteArray(), Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)

fun decodeArg(value: String): String =
    String(Base64.decode(value, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING))
