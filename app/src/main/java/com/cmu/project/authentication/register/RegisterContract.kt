package com.cmu.project.authentication.register

import com.cmu.project.authentication.login.LoginPresenter
import com.cmu.project.core.mvp.BasePresenter
import com.cmu.project.core.mvp.BaseView

interface RegisterContract {

    interface View : BaseView<LoginPresenter> {
        fun startMainActivity()
    }

    interface Presenter : BasePresenter {
        suspend fun signUpWithEmailAndPassword(email: String, password: String)
    }

}