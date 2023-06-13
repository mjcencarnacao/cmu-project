package com.cmu.project.authentication.register

import com.cmu.project.core.Utils.md5
import com.cmu.project.core.database.CacheDatabase
import com.cmu.project.core.models.User
import com.cmu.project.core.models.toUserEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class RegisterPresenter(private val view: RegisterContract.View) : RegisterContract.Presenter {

    private val database = CacheDatabase.getInstance(view.provideContext())

    override suspend fun saveUserToCollection(user: User) {
        Firebase.firestore.collection("users").add(user).await()
        database.userDao().delete()
        database.userDao().insert(user.toUserEntity())
    }

    override suspend fun signUpWithEmailAndPassword(name: String, email: String, password: String) {
        val ref = FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password).await()
        ref.user?.let {
            val user = User(it.uid, name, email, md5(password), emptyList(), emptyList())
            saveUserToCollection(user)
            view.startMainActivity()
        }
    }

}