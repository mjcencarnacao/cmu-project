package com.cmu.project.core

import android.content.Context
import android.location.Location
import com.cmu.project.core.database.CacheDatabase
import com.cmu.project.core.models.Book
import com.cmu.project.core.models.Library
import com.cmu.project.core.models.toBookEntity
import com.cmu.project.core.models.toLibraryEntity
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

object NetworkManager {

    private const val MAX_DISTANCE = 0.1f

    suspend fun getRemoteCollection(context: Context, collection: Collection, location: Location? = null): QuerySnapshot? {
        val result = /*if(collection == Collection.LIBRARIES)
            return getLibrariesWithinDistance(location, MAX_DISTANCE)
        else*/
            Firebase.firestore.collection(collection.name.lowercase()).get().await()
        return result.also { querySnapshot -> cacheRemoteResults(context, querySnapshot, collection) }
    }

    private fun cacheRemoteResults(context: Context, querySnapshot: QuerySnapshot, collection: Collection) {
        val database = CacheDatabase.getInstance(context)
        database.libraryDao().deleteAll()
        querySnapshot.forEach { snapshot ->
            when (collection) {
                Collection.BOOKS -> database.bookDao().insert(snapshot.toObject(Book::class.java).toBookEntity())
                Collection.LIBRARIES -> database.libraryDao().insert(snapshot.toObject(Library::class.java).toLibraryEntity())
                else -> {}
            }
        }
    }

    private suspend fun getLibrariesWithinDistance(location: Location?, maxDistance: Float): QuerySnapshot {

        if (location == null)
            return Firebase.firestore.collection(Collection.LIBRARIES.name.lowercase()).get().await()

        val southPoint = GeoPoint(location.latitude - maxDistance, location.longitude - maxDistance)
        val northPoint = GeoPoint(location.latitude + maxDistance, location.longitude + maxDistance)

        return Firebase.firestore.collection(Collection.LIBRARIES.name.lowercase())
            .whereGreaterThanOrEqualTo("location", southPoint)
            .whereLessThanOrEqualTo("location", northPoint)
            .get()
            .await()
    }

}