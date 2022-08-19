package com.ln.simplechat.utils

import android.net.Uri
import android.util.Log
import java.io.File

const val TAG = "URI_UTIL"

fun getFileUri(string: String): Uri? {
    with(string) {
        return when {
            startsWith("/") -> Uri.fromFile(File(this))
            startsWith("content://") -> Uri.parse(this)
            else -> {
                Log.e(TAG, "getFileUri: FILE NOT FOUND: $string")
                null
            }
        }
    }
}