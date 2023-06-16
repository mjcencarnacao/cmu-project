package com.cmu.project.authentication.register

import android.content.Context
import com.cmu.project.core.database.entities.UserEntity
import com.cmu.project.core.models.User
import com.cmu.project.core.mvp.BasePresenter
import com.cmu.project.core.mvp.BaseView

interface RegisterContract {

    interface View : BaseView<RegisterPresenter> {
        fun startMainActivity()
        fun provideContext(): Context
    }

    interface Presenter : BasePresenter {
        suspend fun saveUserToCollection(user: User)
        suspend fun signUpWithEmailAndPassword(name:String, email: String, password: String)
    }

}