package com.cmu.project.core.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    @Provides
    @Singleton
    fun providesFirebaseAuth() : FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun providesFirebaseStorage() : FirebaseStorage = FirebaseStorage.getInstance()

    @Provides
    @Singleton
    @Named("books")
    fun providesBookCollection() : CollectionReference = Firebase.firestore.collection("books")

    @Provides
    @Singleton
    @Named("libraries")
    fun providesLibraryCollection() : CollectionReference = Firebase.firestore.collection("libraries")

}