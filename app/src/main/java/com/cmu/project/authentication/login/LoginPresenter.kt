package com.cmu.project.authentication.login

import com.cmu.project.authentication.login.LoginPresenter.HOLDER.TAG
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import kotlinx.coroutines.tasks.await

class LoginPresenter(private val view: LoginContract.View) : LoginContract.Presenter {

    object HOLDER {
        const val TAG = "LoginPresenter"
    }

    override fun isUserAuthenticated(): Boolean {
        return FirebaseAuth.getInstance().currentUser != null
    }

    override suspend fun signInWithEmailAndPassword(email: String, password: String) {
        FirebaseAuth.getInstance().currentUser?.let {
            view.startMainActivity()
        } ?: throw FirebaseAuthException(TAG, "Failed to Authenticate to Firebase.")
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password).await()
        view.startMainActivity()
    }

}