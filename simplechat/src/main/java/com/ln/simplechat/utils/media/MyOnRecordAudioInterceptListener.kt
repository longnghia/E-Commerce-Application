package com.ln.simplechat.utils.media

import android.Manifest
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.ln.simplechat.R
import com.luck.picture.lib.interfaces.OnRecordAudioInterceptListener
import com.luck.picture.lib.permissions.PermissionChecker
import com.luck.picture.lib.permissions.PermissionResultCallback

class MyOnRecordAudioInterceptListener : OnRecordAudioInterceptListener {
    override fun onRecordAudio(fragment: Fragment, requestCode: Int) {
        val recordAudio = arrayOf(Manifest.permission.RECORD_AUDIO)
        if (PermissionChecker.isCheckSelfPermission(fragment.context, recordAudio)) {
            startRecordSoundAction(fragment, requestCode)
        } else {
            PermissionChecker.getInstance().requestPermissions(
                fragment,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                object : PermissionResultCallback {
                    override fun onGranted() {
                        startRecordSoundAction(fragment, requestCode)
                    }

                    override fun onDenied() {
                        Toast.makeText(
                            fragment.context,
                            fragment.getText(R.string.need_record_permission),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
        }
    }
}