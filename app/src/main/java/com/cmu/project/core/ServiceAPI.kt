package com.cmu.project.core

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ServiceAPI {

    private const val baseUrl = "https://www.googleapis.com/books/v1/"

    fun getInstance(): Retrofit {
        return Retrofit.Builder().baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

}