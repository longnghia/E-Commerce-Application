package com.goldenowl.ecommerce.ui.customview

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.goldenowl.ecommerce.R


class MessageDialogFragment : DialogFragment() {
    interface MessageDialogListener {
        fun onDialogPositiveClick(dialog: DialogFragment?)
        fun onDialogNegativeClick(dialog: DialogFragment?)
    }

    private var mType: DialogType? = null
    private var mTitle: String? = null
    private var mMessage: String? = null
    private var mListener: MessageDialogListener? = null


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
        builder.setMessage(mMessage)
            .setTitle(mTitle)
        builder.setPositiveButton(getString(R.string.ok)) { _, _ ->
            if (mListener != null) {
                mListener!!.onDialogPositiveClick(this@MessageDialogFragment)
            }
        }
        if(mType == DialogType.OPTION){
            Log.d("type", "onCreateDialog: OPTION")
            builder.setNegativeButton(getString(R.string.dont_allow)) { _, _ ->
                if (mListener != null) {
                    mListener!!.onDialogNegativeClick(this@MessageDialogFragment)
                }
            }
        }
        return builder.create()
    }

    companion object {
        fun newInstance(
            dialogType: DialogType,
            title: String?,
            message: String?,
            listener: MessageDialogListener?
        ): MessageDialogFragment {
            val fragment = MessageDialogFragment()
            fragment.mTitle = title
            fragment.mMessage = message
            fragment.mListener = listener
            fragment.mType = dialogType
            return fragment
        }

        enum class DialogType {
            MESSAGE,
            OPTION
        }
    }

}