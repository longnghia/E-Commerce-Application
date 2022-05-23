package com.goldenowl.ecommerce.ui.home

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.navigation.Navigation
import com.facebook.AccessToken
import com.facebook.GraphRequest
import com.facebook.HttpMethod
import com.facebook.login.LoginManager
import com.goldenowl.ecommerce.R
import com.goldenowl.ecommerce.databinding.FragmentProfileBinding
import com.goldenowl.ecommerce.models.data.SessionManager
import com.goldenowl.ecommerce.ui.auth.LoginSignupActivity
import com.goldenowl.ecommerce.ui.home.profile.BaseFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class ProfileFragment : BaseFragment<FragmentProfileBinding>() {
    private val TAG = "ProfileFragment"

    private lateinit var auth: FirebaseAuth


//    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
//        binding = FragmentProfileBinding.inflate(inflater)
//
//        auth = Firebase.auth
//
//        setViews()
//
//        return binding.root
//    }

    override fun init() {
        auth = Firebase.auth
    }

    override fun setViewBinding(): FragmentProfileBinding {
        return FragmentProfileBinding.inflate(layoutInflater)
    }

    override fun setViews() {
        setUpUI()

        with(binding) {
            btnLogOut.setOnClickListener {
                Log.d(TAG, "setViews: goto Login Signup Activity")
                val loginIntent = Intent(activity, MainActivity::class.java)
                logOut()
                context?.startActivity(loginIntent)
                activity?.finish()
            }

            topAppBar.toolbar.title = getString(R.string.my_profile)

            actionSettings.setOnClickListener (
                Navigation.createNavigateOnClickListener(R.id.next_action, null)
            )
        }
    }

    private fun setUpUI() {
        val sessionManager = SessionManager(requireActivity())
        if (sessionManager.isLoggedIn()) {
            setUpUserUI(sessionManager)

        } else {
            setUpGuessUI(requireActivity())

        }
    }

    override fun observeViews() {
//        with(binding) {
//            viewModel.currentUser.observe(viewLifecycleOwner) { user ->
//                if (user != null) {
//                    Log.d(TAG, "onCreateView: viewmodel.currentuser changed: $user ${user.email} $user.uid")
//
//                } else {
//                    Log.d(TAG, "onCreateView:  viewmodel.currentuser  null")
//                }
//            }
//        }

    }

    private fun setUpUserUI(sessionManager: SessionManager) {
        with(binding) {
            val userName = sessionManager.getUserDataFromSession()["userName"]
            Log.d(TAG, "setViews: logged in as $userName")
            tvName.text = userName
            val userEmail = sessionManager.getUserDataFromSession()["userEmail"]
            tvEmail.text = userEmail
//                userIcon = todo

            layoutUserActions.visibility = View.VISIBLE
            layoutGuessActions.visibility = View.GONE
        }
    }

    private fun setUpGuessUI(context: Context) {
        Log.d(TAG, "setUpGuessUI: start")
        with(binding) {
            setSpannableString(tvEmail)

            tvEmail.movementMethod = LinkMovementMethod.getInstance()
            tvEmail.highlightColor = Color.RED

//            tvEmail.text = getString(com.goldenowl.ecommerce.R.string.guest_email)
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
                ds.color= Color.RED
                ds.isUnderlineText = false
            }
        }
        ss.setSpan(clickableSpan, 0, 7, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        val boldSpan = StyleSpan(Typeface.BOLD)
        ss.setSpan(boldSpan, 0, 7, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        tvEmail.text = ss
    }


    private fun logOut() {
        val sessionManager = SessionManager(requireActivity())
        sessionManager.logoutFromSession()

        if (auth.currentUser != null)
            auth.signOut()

        if (AccessToken.getCurrentAccessToken() != null) {
            GraphRequest(
                AccessToken.getCurrentAccessToken(),
                "/me/permissions/",
                null,
                HttpMethod.DELETE,
                GraphRequest.Callback {
                    AccessToken.setCurrentAccessToken(null)
                    LoginManager.getInstance().logOut()

                }).executeAsync()
        }

    }

}