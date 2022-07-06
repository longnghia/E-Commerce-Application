package com.goldenowl.ecommerce.ui.global.profile

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.StyleSpan
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.goldenowl.ecommerce.R
import com.goldenowl.ecommerce.databinding.FragmentProfileBinding
import com.goldenowl.ecommerce.models.auth.UserManager
import com.goldenowl.ecommerce.ui.auth.LoginSignupActivity
import com.goldenowl.ecommerce.ui.global.BaseFragment
import com.goldenowl.ecommerce.ui.global.MainActivity
import com.goldenowl.ecommerce.viewmodels.AuthViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class ProfileFragment : BaseFragment<FragmentProfileBinding>() {
    private val TAG = "ProfileFragment"

    private lateinit var auth: FirebaseAuth
    private lateinit var userManager: UserManager
    private val viewModel: AuthViewModel by activityViewModels()

    override fun init() {
        userManager = UserManager.getInstance(requireContext())
        auth = Firebase.auth
    }

    override fun getViewBinding(): FragmentProfileBinding {
        return FragmentProfileBinding.inflate(layoutInflater)
    }

    override fun setViews() {
        setUpUI()

        with(binding) {
            btnLogOut.setOnClickListener {
                val loginIntent = Intent(activity, MainActivity::class.java)
                logOut()
                context?.startActivity(loginIntent)
                activity?.finish()
            }

            actionSettings.setOnClickListener(
                Navigation.createNavigateOnClickListener(com.goldenowl.ecommerce.R.id.next_action, null)
            )
        }
    }

    private fun setUpUI() {

        if (userManager.isLoggedIn()) {
            setUpUserUI()
        } else {
            setUpGuessUI()
        }
    }

    override fun setObservers() {
        viewModel.toastMessage.observe(viewLifecycleOwner) {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
    }

    private fun setUpUserUI() {
        with(binding) {
            val userName = userManager.name
            tvName.text = userName
            val userEmail = userManager.email
            tvEmail.text = userEmail
            userManager.avatar.let {
                val uri = Uri.parse(it)
                if (!it.isNullOrBlank()) {
                    Glide.with(this@ProfileFragment)
                        .load(uri)
                        .placeholder(R.drawable.ic_user)
                        .apply(options).into(userIcon)
                }
            }
            layoutUserActions.visibility = View.VISIBLE
            layoutGuessActions.visibility = View.GONE
        }
    }

    private fun setUpGuessUI() {
        with(binding) {
            setSpannableString(tvEmail)

            tvEmail.movementMethod = LinkMovementMethod.getInstance()
            tvEmail.highlightColor = Color.RED

            layoutUserActions.visibility = View.GONE
            layoutGuessActions.visibility = View.VISIBLE
        }
    }

    private fun setSpannableString(tvEmail: TextView) {
        val ss = SpannableString(getString(com.goldenowl.ecommerce.R.string.guest_email))
        val clickableSpan: ClickableSpan = object : ClickableSpan() {

            override fun onClick(textView: View) {

                startActivity(Intent(requireActivity(), LoginSignupActivity::class.java))
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.color = Color.RED
                ds.isUnderlineText = false
            }
        }
        ss.setSpan(clickableSpan, 0, 7, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        val boldSpan = StyleSpan(Typeface.BOLD)
        ss.setSpan(boldSpan, 0, 7, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        tvEmail.text = ss
    }


    private fun logOut() {
        viewModel.logOut()
    }

    override fun setAppbar() {
        binding.topAppBar.collapsingToolbarLayout.title = getString(com.goldenowl.ecommerce.R.string.profile)
    }

    companion object {
        var options: RequestOptions = RequestOptions()
            .centerCrop()
            .error(com.goldenowl.ecommerce.R.drawable.img_broken)

    }
}