package com.mrspd.letschat.fragments.fbLoginFragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginBehavior
import com.facebook.login.LoginResult
import com.mrspd.letschat.R
import com.mrspd.letschat.databinding.FacebookLoginFragmentBinding
import com.mrspd.letschat.ui.mainActivity.SharedViewModel
import com.mrspd.letschat.util.AuthUtil
import com.mrspd.letschat.util.ErrorMessage
import com.mrspd.letschat.util.eventbus_events.CallbackManagerEvent
import org.greenrobot.eventbus.EventBus


class FacebookLoginFragment : Fragment() {


    private lateinit var callbackManager: CallbackManager


    private lateinit var binding: FacebookLoginFragmentBinding


    companion object {
        fun newInstance() =
            FacebookLoginFragment()
    }

    private lateinit var viewModel: FacebookLoginViewModel
    private lateinit var sharedViewModel: SharedViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.facebook_login_fragment, container, false)

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(FacebookLoginViewModel::class.java)
        sharedViewModel = ViewModelProviders.of(this.activity!!).get(SharedViewModel::class.java)


        // Initialize Facebook callbackManager used in Login button
        callbackManager = CallbackManager.Factory.create()
        //pass callback manager to activity to continue FB login
        EventBus.getDefault().post(CallbackManagerEvent(callbackManager))


        binding.FBloginButton.loginBehavior = LoginBehavior.WEB_ONLY
        binding.FBloginButton.setPermissions("email", "public_profile")
        binding.FBloginButton.registerCallback(callbackManager, object :
            FacebookCallback<LoginResult> {
            @SuppressLint("FragmentLiveDataObserve")
            override fun onSuccess(loginResult: LoginResult) {
                viewModel.handleFacebookAccessToken(
                    AuthUtil.firebaseAuthInstance,
                    loginResult.accessToken
                ).observe(this@FacebookLoginFragment, Observer { firebaseUser ->
                            //login with facebook successful
                    viewModel.isUserAlreadyStoredInFirestore(firebaseUser.uid)
                        .observe(this@FacebookLoginFragment, Observer { isUserStoredInFirestore ->
                                    //if user doesn't exist in firestore store him
                            if (!isUserStoredInFirestore) {
                                viewModel.storeFacebookUserInFirebase().observe(
                                    this@FacebookLoginFragment,
                                    Observer { isStoredSuccessfully ->
                                                    //if true facebook user been stored in firebase successfully
                                                    if (isStoredSuccessfully) {
                                                        navigateToHome()
                                                    }
                                                })
                                    } else {
                                        //fb user already stored in firestore just navigate to home
                                        navigateToHome()
                                    }
                                })
                        })

            }

            override fun onCancel() {

            }

            override fun onError(error: FacebookException) {
                ErrorMessage.errorMessage = error.message
            }
        })


        //pass loading state to shared viewmodel to show proper layout
        viewModel.loadState.observe(this, Observer {
        })

    }

    private fun navigateToHome() {

        try {
            if (parentFragment?.javaClass.toString() == "class com.mrspd.letschat.fragments.login.LoginFragment") {

                this@FacebookLoginFragment.findNavController()
                    .navigate(R.id.action_loginFragment_to_homeFragment)
            } else if (parentFragment?.javaClass.toString() == "class com.mrspd.letschat.fragments.register.SignupFragment") {
                this@FacebookLoginFragment.findNavController()
                    .navigate(R.id.action_signupFragment_to_homeFragment)
            }

        } catch (e: Exception) {
            println("FacebookLoginFragment.navigateToHome:${e.message}")
        }
    }


}
