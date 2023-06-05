package com.cmu.project.core.models

import com.cmu.project.core.Constants.EMPTY_STRING

data class User(
    var uid: String = EMPTY_STRING,
    var email: String = EMPTY_STRING,
    var name: String = EMPTY_STRING,
    var password: String = EMPTY_STRING,
)
