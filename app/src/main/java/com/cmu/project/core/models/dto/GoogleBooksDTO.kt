package com.cmu.project.core.models.dto

data class GoogleBooksDTO(
    val items: List<Item>,
    val kind: String,
    val totalItems: Int
)