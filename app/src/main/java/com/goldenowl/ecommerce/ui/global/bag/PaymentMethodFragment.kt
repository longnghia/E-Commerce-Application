package com.goldenowl.ecommerce.ui.global.bag

import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.goldenowl.ecommerce.R
import com.goldenowl.ecommerce.adapter.PaymentMethodAdapter
import com.goldenowl.ecommerce.databinding.FragmentPaymentMethodBinding
import com.goldenowl.ecommerce.models.data.Card
import com.goldenowl.ecommerce.ui.global.BaseHomeFragment
import com.goldenowl.ecommerce.ui.global.bottomsheet.BottomSheetInsertCard

class PaymentMethodFragment : BaseHomeFragment<FragmentPaymentMethodBinding>(), PaymentMethodAdapter.ICheckListener {

    private var listCard: List<Card> = emptyList()
    private var defaultCard: Int = 0
    private lateinit var adapter: PaymentMethodAdapter
    override fun getViewBinding(): FragmentPaymentMethodBinding {
        return FragmentPaymentMethodBinding.inflate(layoutInflater)
    }

    override fun setObservers() {
        viewModel.toastMessage.observe(viewLifecycleOwner) {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }

        viewModel.listCard.observe(viewLifecycleOwner) {
            listCard = it
            adapter.setData(listCard, defaultCard)
            if (it.isEmpty()) {
                binding.tvPaymentCards.text = getString(R.string.no_card)
            } else {
                binding.tvPaymentCards.text = getString(R.string.your_payment_cards)
            }
        }

        viewModel.defaultCardIndex.observe(viewLifecycleOwner) {
            Log.d(TAG, "setObservers: defaultCard=$it")
            if (it != null) {
                defaultCard = it
                adapter.setData(listCard, defaultCard)
            }
        }
    }

    override fun setViews() {
        adapter = PaymentMethodAdapter(this)
        adapter.setData(listCard, defaultCard)
        binding.rcvCards.adapter = adapter

        /*add card*/
        binding.ivAddCard.setOnClickListener {
            toggleBottomSheetInsertCard()
        }
    }

    private fun toggleBottomSheetInsertCard() {
        val modalBottomSheet = BottomSheetInsertCard(viewModel)
        modalBottomSheet.enterTransition = View.GONE
        modalBottomSheet.show(parentFragmentManager, TAG)
    }


    companion object {
        const val TAG = "PaymentMethodFragment"
    }

    override fun setAppbar() {
        binding.topAppBar.collapsingToolbarLayout.title = getString(R.string.payment_method)

        binding.topAppBar.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    override fun selectCard(position: Int, card: Card) {
        viewModel.setDefaultCard(position, card)
    }

    override fun removeCard(position: Int) {
        viewModel.removeCard(position)
    }

    override fun insertCard(card: Card) {
        Log.d(TAG, "insertCard: $card")
        viewModel.insertCard(card)
    }
}