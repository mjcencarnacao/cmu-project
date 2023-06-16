package com.cmu.project.main.search

import android.content.Context
import android.net.Uri
import com.cmu.project.core.mvp.BasePresenter
import com.cmu.project.core.mvp.BaseView
import com.google.firebase.firestore.DocumentReference

interface BookSearchContract {

    interface BookView {
        fun setBookTitle(title: String)
        fun setBookAuthor(author: String)
        fun setBookRating(rating: Float)
        fun setBookCover(url: Uri?, cached: Boolean)
        fun setImageListener(url: Uri?)
        fun setBookNotification(documentReference : DocumentReference)
    }

    interface Presenter : BasePresenter

    interface View {
        fun provideContext() : Context
    }

}