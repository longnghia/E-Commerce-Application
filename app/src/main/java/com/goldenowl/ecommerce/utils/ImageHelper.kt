package com.goldenowl.ecommerce.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.quality
import id.zelory.compressor.constraint.size
import java.io.File

object ImageHelper {
    private suspend fun compressImageFile(context: Context, file: File): File {
        val compressedImageFile = Compressor.compress(context, file) {
            quality(Constants.UPLOAD_QUALITY)
            size(Constants.UPLOAD_MAX_SIZE)
        }
        Log.d(
            "ImageHelper",
            "compressImageFile: compressed $file successfully: ${compressedImageFile.length() / 1024} KB"
        )
        return compressedImageFile
    }

    suspend fun compressImageUri(context: Context, uri: Uri): File {
        val file = FileHelper.from(context, uri)
        return if (file.length() > Constants.UPLOAD_MAX_SIZE)
            compressImageFile(context, file)
        else file
    }
}