package com.cmu.project.authentication.login

import com.cmu.project.core.mvp.BasePresenter
import com.cmu.project.core.mvp.BaseView

interface LoginContract {

    interface View : BaseView<LoginPresenter> {
        fun startMainActivity()
    }

    interface Presenter : BasePresenter {
        fun isUserAuthenticated(): Boolean
        suspend fun signInWithEmailAndPassword(email: String, password: String)
    }

}