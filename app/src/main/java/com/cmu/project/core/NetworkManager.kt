package com.cmu.project.core

import android.content.Context
import com.cmu.project.core.database.CacheDatabase
import com.cmu.project.core.models.Book
import com.cmu.project.core.models.Library
import com.cmu.project.core.models.User
import com.cmu.project.core.models.toBookEntity
import com.cmu.project.core.models.toLibraryEntity
import com.cmu.project.core.models.toUserEntity
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

object NetworkManager {

    suspend fun getRemoteCollection(context: Context, collection: Collection): QuerySnapshot? {
        return Firebase.firestore.collection(collection.name.lowercase()).get().await().also { querySnapshot ->
            cacheRemoteResults(context, querySnapshot, collection)
        }
    }

    private fun cacheRemoteResults(context: Context, querySnapshot: QuerySnapshot, collection: Collection) {
        val database = CacheDatabase.getInstance(context)
        querySnapshot.forEach { snapshot ->
            when (collection) {
                Collection.BOOKS -> database.bookDao().insert(snapshot.toObject(Book::class.java).toBookEntity())
                Collection.LIBRARIES -> database.libraryDao().insert(snapshot.toObject(Library::class.java).toLibraryEntity())
                else -> {}
            }
        }
    }

}