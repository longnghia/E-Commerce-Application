package com.goldenowl.ecommerce.ui.global.bottomsheet

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.goldenowl.ecommerce.R
import com.goldenowl.ecommerce.adapter.BottomSheetWriteReviewAdapter
import com.goldenowl.ecommerce.databinding.ModalBottomSheetWriteReviewBinding
import com.goldenowl.ecommerce.models.data.ProductData
import com.goldenowl.ecommerce.utils.BaseLoadingStatus
import java.util.*

class BottomSheetWriteReview(private val productData: ProductData) :
    BaseBottomSheet<ModalBottomSheetWriteReviewBinding>() {

    private val listImages: MutableList<String> = mutableListOf()
    private val listUri: MutableList<Uri> = mutableListOf()

    private lateinit var imageActivityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var bottomSheetAdapter: BottomSheetWriteReviewAdapter


    companion object {
        const val TAG = "BottomSheetWriteReview"
    }

    private fun selectImage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        imageActivityResultLauncher.launch(intent)
    }

    override fun getViewBinding(): ModalBottomSheetWriteReviewBinding {
        return ModalBottomSheetWriteReviewBinding.inflate(layoutInflater)
    }

    override fun setViews() {
        bottomSheetAdapter = BottomSheetWriteReviewAdapter { selectImage() }
        binding.rcvImgs.adapter = bottomSheetAdapter

        binding.btnSendReview.setOnClickListener {
            val userId = viewModel.getUserId()
            val product = productData.product
            val rating = binding.ratingBar.rating.toInt()
            val review = binding.edtReview.text.toString()
            val date = Date()
            if (rating == 0) {
                Toast.makeText(context, getString(R.string.pls_select_rating), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            viewModel.sendReview(userId, product.id, rating, review, listUri, date)
        }
    }

    override fun setObservers() {
        viewModel.loadingStatus.observe(viewLifecycleOwner) {
            when (it) {
                BaseLoadingStatus.LOADING -> {
                    binding.layoutLoading.loadingFrameLayout.visibility = View.VISIBLE
                }
                BaseLoadingStatus.SUCCEEDED -> {
                    binding.layoutLoading.loadingFrameLayout.visibility = View.GONE
                    viewModel.loadingStatus.value = BaseLoadingStatus.NONE
                    dismiss()
                }
                else -> binding.layoutLoading.loadingFrameLayout.visibility = View.GONE
            }
        }
    }

    override fun initData() {
        super.initData()
        val callback = ActivityResultCallback<ActivityResult> { result ->
            if (result == null)
                return@ActivityResultCallback
            if (result.resultCode != Activity.RESULT_OK)
                return@ActivityResultCallback
            val data: Intent = result.data ?: return@ActivityResultCallback

            val uri: Uri? = data.data
            uri?.let {
                listUri.add(it)
                listImages.add(it.toString())
                bottomSheetAdapter.setData(listImages)
                binding.rcvImgs.scrollToPosition(listImages.size)
            }
        }
        imageActivityResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult(), callback)
    }
}