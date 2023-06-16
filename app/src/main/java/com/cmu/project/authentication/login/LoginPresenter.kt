package com.cmu.project.authentication.login

import android.util.Log
import android.widget.Toast
import com.cmu.project.authentication.login.LoginPresenter.HOLDER.TAG
import com.cmu.project.core.Collection
import com.cmu.project.core.Constants.EMPTY_STRING
import com.cmu.project.core.NetworkManager.getRemoteCollection
import com.cmu.project.core.Utils.md5
import com.cmu.project.core.database.CacheDatabase
import com.cmu.project.core.database.entities.UserEntity
import com.cmu.project.core.models.User
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class LoginPresenter(private val view: LoginContract.View) : LoginContract.Presenter {

    object HOLDER {
        const val TAG = "LoginPresenter"
    }

    override fun isUserAuthenticated(): Boolean {
        return FirebaseAuth.getInstance().currentUser != null
    }

    override suspend fun signInWithEmailAndPassword(email: String, password: String) {
        try {
            val ref = FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password).await()
            view.startMainActivity()
            Log.i(TAG, "Logged user successfully.")
        } catch (e: Exception) {
            Log.e(TAG, "Error occurred while logging user in. Error: ${e.message}")
            withContext(Dispatchers.Main) { Toast.makeText(view.provideContext(), "Error occurred while logging user in.", Toast.LENGTH_LONG).show()}
        }
    }

    private suspend fun retrieveUserFromRemoteCollection(uid: String): String {
        return getRemoteCollection(view.provideContext(), Collection.USERS)?.let { snapshot ->
            snapshot.documents.filter { it.getString("id") == uid }
        }?.get(0)?.toObject(User::class.java)?.name ?: ""
    }

}