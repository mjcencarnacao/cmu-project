package com.cmu.project.authentication.login

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.Manifest.permission.ACCESS_NOTIFICATION_POLICY
import android.Manifest.permission.CAMERA
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.cmu.project.R
import com.cmu.project.authentication.login.LoginFragment.HOLDER.PERMISSION_REQUEST_CODE
import com.cmu.project.authentication.login.LoginFragment.HOLDER.TAG
import com.cmu.project.authentication.login.LoginFragment.HOLDER.permissions
import com.cmu.project.core.activities.MainActivity
import com.cmu.project.databinding.FragmentLoginBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class LoginFragment : Fragment(R.layout.fragment_login), LoginContract.View {

    object HOLDER {
        const val TAG = "LoginFragment"
        const val PERMISSION_REQUEST_CODE: Int = 1234
        val permissions = arrayOf(CAMERA, ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION, ACCESS_NOTIFICATION_POLICY)
    }

    override lateinit var presenter: LoginPresenter
    private lateinit var binding: FragmentLoginBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.i(TAG, "onViewCreated: Started LoginFragment.")
        binding = FragmentLoginBinding.bind(view)
        presenter = LoginPresenter(this)
        setupFragment()
    }

    private fun setupFragment() {
        if (presenter.isUserAuthenticated()) startMainActivity()
        setOnClickListeners()
        checkAndRequestPermissions()
    }

    private fun setOnClickListeners() {
        binding.btnSignIn.setOnClickListener { signInWithEmailAndPassword() }
        binding.tvSignUp.setOnClickListener { findNavController().navigate(R.id.action_loginFragment_to_registerFragment) }
    }

    private fun signInWithEmailAndPassword() = lifecycleScope.launch(Dispatchers.IO) {
        val email: String = binding.etEmail.text.toString()
        val password: String = binding.etPassword.text.toString()
        if (email.isNotBlank() && password.isNotBlank()) presenter.signInWithEmailAndPassword(
            email,
            password
        )
    }

    override fun provideContext(): Context {
        return requireContext()
    }

    override fun startMainActivity() {
        Log.i(TAG, "Starting Main Activity.")
        startActivity(Intent(context, MainActivity::class.java)).also { activity?.finish() }
    }

    override fun checkAndRequestPermissions() {
        Log.i(TAG, "Checking permissions.")
        ActivityCompat.requestPermissions(requireActivity(), permissions, PERMISSION_REQUEST_CODE)
    }

}