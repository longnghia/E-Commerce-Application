package com.goldenowl.ecommerce.ui.global.shop

import android.R.attr.x
import android.R.attr.y
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.navigation.fragment.findNavController
import com.goldenowl.ecommerce.R
import com.goldenowl.ecommerce.databinding.FragmentQrBinding
import com.goldenowl.ecommerce.ui.customview.MessageDialogFragment
import com.goldenowl.ecommerce.utils.Utils
import com.google.zxing.Result
import me.dm7.barcodescanner.zxing.ZXingScannerView


class QRFragment : Fragment(), ZXingScannerView.ResultHandler, MessageDialogFragment.MessageDialogListener {

    private lateinit var mScannerView: ZXingScannerView
    private lateinit var binding: FragmentQrBinding
    private var mFlash = false
    private var mAutoFocus = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentQrBinding.inflate(inflater, container, false)
        mScannerView = binding.scannerView
        if (savedInstanceState != null) {
            mFlash = savedInstanceState.getBoolean(FLASH_STATE, false);
            mAutoFocus = savedInstanceState.getBoolean(AUTO_FOCUS_STATE, true);
        } else {
            mFlash = false;
            mAutoFocus = true;
        }
        setViews()
        return binding.root
    }

    private fun setViews() {
        mScannerView.setSquareViewFinder(true)
        binding.ivFlash.setOnClickListener {
            mFlash = !mFlash
            it.isSelected = mFlash
            mScannerView.flash = mFlash
        }

        val height = binding.root.height
        val width = binding.root.width
        val textView = TextView(context).apply {
            text = "TEST123"
            setTextColor(Utils.getColor(context, R.color.white) ?: 0xFFFFFF)
        }
        val params = CoordinatorLayout.LayoutParams(
            CoordinatorLayout.LayoutParams.WRAP_CONTENT,
            CoordinatorLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(x, width/2, x, y)
        params.gravity = Gravity.CENTER

        textView.layoutParams = params
        binding.root.addView(textView)
    }

    override fun onResume() {
        super.onResume()
        mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
        mScannerView.startCamera();          // Start camera on resume
        mScannerView.setFlash(mFlash);
        mScannerView.setAutoFocus(mAutoFocus);
    }

    override fun onPause() {
        super.onPause()
        mScannerView.stopCamera();
        closeMessageDialog();
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(FLASH_STATE, mFlash)
        outState.putBoolean(AUTO_FOCUS_STATE, mAutoFocus)
    }

    companion object {
        val TAG = "QRFragment"
        private const val FLASH_STATE = "FLASH_STATE"
        private const val AUTO_FOCUS_STATE = "AUTO_FOCUS_STATE"
        private const val SELECTED_FORMATS = "SELECTED_FORMATS"
        private const val CAMERA_ID = "CAMERA_ID"
    }

    override fun handleResult(rawResult: Result) {
        try {
            val notification: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val r = RingtoneManager.getRingtone(context, notification)
            r.play()
        } catch (e: Exception) {
        }
        showMessageDialog("Contents = " + rawResult.text + ", Format = " + rawResult.barcodeFormat.toString())
    }

    private fun showMessageDialog(message: String?) {
        val fragment: DialogFragment = MessageDialogFragment.newInstance("Scan Results", message, this)
        fragment.show(activity!!.supportFragmentManager, "scan_results")
    }

    private fun closeMessageDialog() {
        closeDialog("scan_results")
    }


    private fun closeDialog(dialogName: String?) {
        val fragmentManager: FragmentManager = activity!!.supportFragmentManager
        val fragment: DialogFragment = fragmentManager.findFragmentByTag(dialogName) as DialogFragment
        if (fragment != null) {
            fragment.dismiss()
        }
    }

    override fun onDialogPositiveClick(dialog: DialogFragment?) {
        // Resume the camera
        mScannerView.resumeCameraPreview(this)
        findNavController().navigate(R.id.home_dest)
    }

}
