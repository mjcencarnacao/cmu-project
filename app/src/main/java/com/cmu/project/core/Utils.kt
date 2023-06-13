package com.cmu.project.core

import android.graphics.Bitmap
import com.cmu.project.core.database.CacheDatabase
import com.cmu.project.core.database.entities.LibraryEntity
import com.cmu.project.core.database.entities.toLibrary
import com.cmu.project.core.models.Library
import com.cmu.project.core.models.toLibraryEntity
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import java.io.ByteArrayOutputStream
import java.math.BigInteger
import java.security.MessageDigest

object Utils {
    fun convertBitmapToByteArray(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        return stream.toByteArray()
    }

    fun libraryEntityToLibraryList(list: List<LibraryEntity>): List<Library> {
        val libraries = mutableListOf<Library>()
        list.forEach { entity -> libraries.add(entity.toLibrary()) }
        return libraries
    }

    fun libraryListFromSnapshot(snapshot: QuerySnapshot, database: CacheDatabase) : List<Library> {
        val libraries = mutableListOf<Library>()
        snapshot.forEach { document ->
            val library = document.toObject(Library::class.java)
            libraries.add(library)
            database.libraryDao().insert(library.toLibraryEntity())
        }
        return libraries
    }

    fun md5(input:String): String {
        val md = MessageDigest.getInstance("MD5")
        return BigInteger(1, md.digest(input.toByteArray())).toString(16).padStart(32, '0')
    }
}