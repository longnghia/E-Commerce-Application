package com.ln.simplechat.utils.media

import android.content.Intent
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import com.luck.picture.lib.utils.ToastUtils

fun startRecordSoundAction(fragment: Fragment, requestCode: Int) {
    val recordAudioIntent = Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION)
    if (recordAudioIntent.resolveActivity(fragment.requireActivity().packageManager) != null) {
        fragment.startActivityForResult(recordAudioIntent, requestCode)
    } else {
        ToastUtils.showToast(fragment.context, "The system is missing a recording component")
    }
}