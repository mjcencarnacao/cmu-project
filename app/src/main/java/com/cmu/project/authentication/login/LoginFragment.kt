package com.cmu.project.authentication.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.cmu.project.R
import com.cmu.project.core.activities.MainActivity
import com.cmu.project.databinding.FragmentLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LoginFragment : Fragment(R.layout.fragment_login), LoginContract.View {

    override lateinit var presenter: LoginPresenter
    private lateinit var binding: FragmentLoginBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentLoginBinding.bind(view)
        presenter = LoginPresenter(this)
        setOnClickListeners()
        FirebaseAuth.getInstance().currentUser?.let {
            startMainActivity()
        } ?: throw FirebaseAuthException(LoginPresenter.HOLDER.TAG, "Failed to Authenticate to Firebase.")
    }

    private fun setOnClickListeners() {
        binding.btnSignIn.setOnClickListener { signInWithEmailAndPassword() }
        binding.tvSignUp.setOnClickListener { findNavController().navigate(R.id.action_loginFragment_to_registerFragment) }
    }

    private fun signInWithEmailAndPassword() = lifecycleScope.launch(Dispatchers.IO) {
        val email: String = binding.etEmail.text.toString()
        val password: String = binding.etPassword.text.toString()
        if(email.isNotBlank() && password.isNotBlank()) presenter.signInWithEmailAndPassword(email, password)
    }

    override fun startMainActivity() {
        startActivity(Intent(context, MainActivity::class.java)).also { activity?.finish() }
    }

}