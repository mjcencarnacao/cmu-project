package com.cmu.project.authentication.register

import android.util.Log
import android.widget.Toast
import com.cmu.project.authentication.register.RegisterPresenter.HOLDER.TAG
import com.cmu.project.core.Utils.md5
import com.cmu.project.core.database.CacheDatabase
import com.cmu.project.core.models.User
import com.cmu.project.core.models.toUserEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class RegisterPresenter(private val view: RegisterContract.View) : RegisterContract.Presenter {

    object HOLDER {
        const val TAG = "RegisterPresenter"
    }

    override suspend fun saveUserToCollection(user: User) {
        Firebase.firestore.collection("users").add(user).await()
    }

    override suspend fun signUpWithEmailAndPassword(name: String, email: String, password: String) {
        try {
            val ref = FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password).await()
            ref.user?.let {
                val user = User(it.uid, name, email, md5(password), emptyList(), emptyList())
                saveUserToCollection(user)
                view.startMainActivity()
            }
            Log.i(TAG, "Registered user successfully.")
        } catch (e: Exception) {
            Log.e(TAG, "Error occurred while registering user. Error: ${e.message}")
            withContext(Dispatchers.Main) { Toast.makeText(view.provideContext(), "Error occurred while registering user.", Toast.LENGTH_LONG).show() }
        }
    }

}