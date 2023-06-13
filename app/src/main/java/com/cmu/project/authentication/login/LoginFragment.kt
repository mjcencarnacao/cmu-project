package com.cmu.project.authentication.login

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.cmu.project.R
import com.cmu.project.core.activities.MainActivity
import com.cmu.project.databinding.FragmentLoginBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class LoginFragment : Fragment(R.layout.fragment_login), LoginContract.View {

    private val permissions = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    private val PERMISSION_REQUEST_CODE : Int = 1234
    override lateinit var presenter: LoginPresenter
    private lateinit var binding: FragmentLoginBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentLoginBinding.bind(view)
        presenter = LoginPresenter(this)
        setOnClickListeners()
        checkAndRequestPermission()
        if (presenter.isUserAuthenticated()) startMainActivity()
    }

    private fun setOnClickListeners() {
        binding.btnSignIn.setOnClickListener { signInWithEmailAndPassword() }
        binding.tvSignUp.setOnClickListener { findNavController().navigate(R.id.action_loginFragment_to_registerFragment) }
    }

    private fun signInWithEmailAndPassword() = lifecycleScope.launch(Dispatchers.IO) {
        val email: String = binding.etEmail.text.toString()
        val password: String = binding.etPassword.text.toString()
        if (email.isNotBlank() && password.isNotBlank()) presenter.signInWithEmailAndPassword(email, password)
    }

    override fun provideContext(): Context {
        return requireContext()
    }

    override fun startMainActivity() {
        startActivity(Intent(context, MainActivity::class.java)).also { activity?.finish() }
    }

    private fun checkAndRequestPermission() {
        ActivityCompat.requestPermissions(requireActivity(), permissions, PERMISSION_REQUEST_CODE)
    }

}