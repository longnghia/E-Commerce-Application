package com.ln.simplechat.utils.media

import android.content.Context
import android.net.Uri
import com.luck.picture.lib.engine.CompressFileEngine
import com.luck.picture.lib.interfaces.OnKeyValueResultCallbackListener
import com.luck.picture.lib.utils.DateUtils
import top.zibin.luban.Luban
import top.zibin.luban.OnNewCompressListener
import java.io.File

class ImageFileCompressEngine : CompressFileEngine {
    override fun onStartCompress(context: Context, source: ArrayList<Uri>, call: OnKeyValueResultCallbackListener) {
        Luban.with(context)
            .load(source)

            .ignoreBy(1024)
            .setRenameListener { filePath ->
                val indexOf = filePath.lastIndexOf(".")
                val postfix = if (indexOf != -1) filePath.substring(indexOf) else ".jpg"
                DateUtils.getCreateFileName("CMP_") + postfix
            }.setCompressListener(object : OnNewCompressListener {
                override fun onStart() {}
                override fun onSuccess(source: String, compressFile: File) {
                    call.onCallback(source, compressFile.absolutePath)
                }

                override fun onError(source: String, e: Throwable) {
                    call.onCallback(source, null)
                }
            }).launch()
    }
}