package com.goldenowl.ecommerce.ui.customview

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.goldenowl.ecommerce.R


class MessageDialogFragment : DialogFragment() {
    interface MessageDialogListener {
        fun onDialogPositiveClick(dialog: DialogFragment?)
    }

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
        return builder.create()
    }

    companion object {
        fun newInstance(title: String?, message: String?, listener: MessageDialogListener?): MessageDialogFragment {
            val fragment = MessageDialogFragment()
            fragment.mTitle = title
            fragment.mMessage = message
            fragment.mListener = listener
            return fragment
        }
    }
}