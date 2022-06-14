package com.goldenowl.ecommerce.ui.global.bottomsheet

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.goldenowl.ecommerce.adapter.ModalAdapter
import com.goldenowl.ecommerce.databinding.ModalBottomSheetAddToFavoriteBinding
import com.goldenowl.ecommerce.models.data.Favorite
import com.goldenowl.ecommerce.models.data.Product
import com.goldenowl.ecommerce.viewmodels.ShopViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class BottomSheetInsertFavorite(private val product: Product, private val viewModel: ShopViewModel) :
    BottomSheetDialogFragment() {

    private lateinit var binding: ModalBottomSheetAddToFavoriteBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View =
        ModalBottomSheetAddToFavoriteBinding.inflate(layoutInflater, container, false).apply {
            binding = this
        }.root

    @SuppressLint("LongLogTag")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bottomSheetAdapter = ModalAdapter()
        // todo check if size remain
        binding.rcvSizes.adapter = bottomSheetAdapter
        binding.rcvSizes.layoutManager = GridLayoutManager(context, 3)


        binding.btnAddToFavorites.setOnClickListener {
            viewModel.insertFavorite(
                Favorite(
                    product.id,
                    bottomSheetAdapter.getCheckedSize(),
                    product.getFirstColor()
                )
            )
            dismiss()
        }

    }

    companion object {
        const val TAG = "BottomSheetInsertFavorite"
    }
}