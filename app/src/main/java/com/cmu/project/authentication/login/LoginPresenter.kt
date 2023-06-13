package com.cmu.project.authentication.login

import com.cmu.project.core.Constants.EMPTY_STRING
import com.cmu.project.core.Utils.md5
import com.cmu.project.core.database.CacheDatabase
import com.cmu.project.core.database.entities.UserEntity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

class LoginPresenter(private val view: LoginContract.View) : LoginContract.Presenter {

    private val database = CacheDatabase.getInstance(view.provideContext())

    override fun isUserAuthenticated(): Boolean {
        return FirebaseAuth.getInstance().currentUser != null
    }

    override suspend fun signInWithEmailAndPassword(email: String, password: String) {
        if (isUserAuthenticated())
            view.startMainActivity()
        else {
            val ref = FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password).await()
            database.userDao().insert(UserEntity(id = ref.user!!.uid, "", email, md5(password), EMPTY_STRING, EMPTY_STRING))
            view.startMainActivity()
        }
    }

}