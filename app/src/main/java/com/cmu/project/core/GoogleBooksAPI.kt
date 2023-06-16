package com.cmu.project.core

import com.cmu.project.core.models.dto.GoogleBooksDTO
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface GoogleBooksAPI {
    @GET("volumes")
    suspend fun getBookDetails(@Query("q") id: String): Response<GoogleBooksDTO>
}