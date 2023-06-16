package com.cmu.project.authentication.register

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.cmu.project.R
import com.cmu.project.authentication.login.LoginPresenter
import com.cmu.project.authentication.register.RegisterFragment.HOLDER.TAG
import com.cmu.project.core.activities.MainActivity
import com.cmu.project.databinding.FragmentRegisterBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RegisterFragment : Fragment(R.layout.fragment_register), RegisterContract.View {

    object HOLDER {
        const val TAG = "RegisterFragment"
    }

    override lateinit var presenter: RegisterPresenter
    private lateinit var binding: FragmentRegisterBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentRegisterBinding.bind(view)
        presenter = RegisterPresenter(this)
        setOnClickListeners()
    }

    private fun setOnClickListeners() {
        binding.btnSignIn.setOnClickListener { signUpWithEmailAndPassword() }
        binding.tvSignUp.setOnClickListener { findNavController().navigate(R.id.action_registerFragment_to_loginFragment) }
    }

    private fun signUpWithEmailAndPassword() = lifecycleScope.launch(Dispatchers.IO) {
        val name: String = binding.etName.text.toString()
        val email: String = binding.etEmail.text.toString()
        val password: String = binding.etPassword.text.toString()
        val confirmPassword : String = binding.etConfirmPassword.text.toString()
        if (email.isNotBlank() && password.isNotBlank() && confirmPassword == password) presenter.signUpWithEmailAndPassword(name, email, password)
    }

    override fun provideContext(): Context {
        return requireContext()
    }

    override fun startMainActivity() {
        Log.i(TAG, "Starting Main Activity.")
        startActivity(Intent(context, MainActivity::class.java)).also { activity?.finish() }
    }

}