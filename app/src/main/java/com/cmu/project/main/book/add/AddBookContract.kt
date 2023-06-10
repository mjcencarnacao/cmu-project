package com.cmu.project.main.book.add

import android.graphics.Bitmap
import com.cmu.project.core.mvp.BasePresenter
import com.cmu.project.core.mvp.BaseView
import com.google.firebase.firestore.DocumentReference

interface AddBookContract {

    interface View : BaseView<AddBookPresenter> {
        fun getBookImage() : Bitmap?
    }

    interface Presenter : BasePresenter {
        fun addBookToFirebaseWithCode(id: String)
        fun addBookToLibrary(documentReference: DocumentReference)
        fun addBookToFirebaseWithoutCode(name: String, bitmap: Bitmap)
    }

}