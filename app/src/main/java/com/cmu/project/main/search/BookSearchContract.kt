package com.cmu.project.main.search

import android.net.Uri

interface BookSearchContract {

    interface BookView {
        fun setBookTitle(title: String)
        fun setBookAuthor(author: String)
        fun setBookRating(rating: Float)
        fun setBookCover(url: Uri?)
    }

}