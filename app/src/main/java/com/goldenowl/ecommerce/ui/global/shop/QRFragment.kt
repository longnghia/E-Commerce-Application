package com.goldenowl.ecommerce.ui.global.shop

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Point
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.navigation.fragment.findNavController
import com.goldenowl.ecommerce.R
import com.goldenowl.ecommerce.databinding.FragmentQrBinding
import com.goldenowl.ecommerce.ui.customview.MessageDialogFragment
import com.google.zxing.Result
import me.dm7.barcodescanner.zxing.ZXingScannerView


class QRFragment : Fragment(), ZXingScannerView.ResultHandler {

    private lateinit var mScannerView: ZXingScannerView
    private lateinit var binding: FragmentQrBinding
    private var mFlash = false
    private var mAutoFocus = false
    private var permissionGranted = false
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                permissionGranted = true
            } else {
                findNavController().navigateUp()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        permissionGranted = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (!permissionGranted) {
            requestPermission()
        }
    }

    private fun requestPermission() {
        val fragment: DialogFragment = MessageDialogFragment.newInstance(
            MessageDialogFragment.Companion.DialogType.OPTION,
            getString(R.string.access_camera),
            getString(R.string.camera_qr),
            object : MessageDialogFragment.MessageDialogListener {
                override fun onDialogPositiveClick(dialog: DialogFragment?) {
                    requestPermissionLauncher.launch(Manifest.permission.CAMERA)
                    dialog?.dismiss()
                }

                override fun onDialogNegativeClick(dialog: DialogFragment?) {
                    findNavController().navigateUp()
                }
            }
        )
        fragment.show(activity!!.supportFragmentManager, "request_permission")
    }

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
            if (!permissionGranted) {
                requestPermission()
                return@setOnClickListener
            }
            mFlash = !mFlash
            it.isSelected = mFlash
            mScannerView.flash = mFlash
        }

        val params = CoordinatorLayout.LayoutParams(
            CoordinatorLayout.LayoutParams.WRAP_CONTENT,
            CoordinatorLayout.LayoutParams.WRAP_CONTENT,
        )
        val display: Display = requireActivity().windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        val width: Int = size.x
        val height: Int = size.y

        Log.d(TAG, "setViews: width =$width")
        params.setMargins(0, -width * 6 / 8, 0, 0)
        params.gravity = Gravity.CENTER
        binding.tvStatus.apply {
            elevation = 50f
            layoutParams = params
        }
    }

    override fun onResume() {
        super.onResume()
        mScannerView.setResultHandler(this);
        mScannerView.startCamera();
        mScannerView.flash = mFlash;
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
    }

    override fun handleResult(rawResult: Result) {
        try {
            val notification: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val r = RingtoneManager.getRingtone(context, notification)
            r.play()
            val res = rawResult.text
            if (validQR(res)) {
                val productId = res.split('=')[1]
                showMessageDialog("Found $productId")
            } else {
                showMessageDialog("Invalid QR code")
            }
        } catch (e: Exception) {
            showMessageDialog(getString(R.string.scan_error))
        }
    }

    private fun validQR(text: String?): Boolean {
        if (text.isNullOrBlank()) return false
        return text.indexOf("Product=") == 0
    }

    private fun showMessageDialog(message: String?) {
        val fragment: DialogFragment = MessageDialogFragment.newInstance(
            MessageDialogFragment.Companion.DialogType.MESSAGE,
            "Scan Results",
            message,
            object : MessageDialogFragment.MessageDialogListener {
                override fun onDialogPositiveClick(dialog: DialogFragment?) {
                    mScannerView.resumeCameraPreview(this@QRFragment)
                    findNavController().navigate(R.id.home_dest)
                }

                override fun onDialogNegativeClick(dialog: DialogFragment?) {
                }
            }
        )
        fragment.show(activity!!.supportFragmentManager, "scan_results")
    }

    private fun closeMessageDialog() {
        closeDialog("scan_results")
    }


    private fun closeDialog(dialogName: String?) {
        val fragmentManager: FragmentManager = activity!!.supportFragmentManager
        val fragment = fragmentManager.findFragmentByTag(dialogName) ?: return
        val diaglog = fragment as DialogFragment
        if (diaglog != null) {
            diaglog.dismiss()
        }
    }
}
