package com.aslaltuna.instafire.models

import com.google.firebase.firestore.PropertyName
import java.io.Serializable

data class User(
    var username: String = "",
    var age: Int = 0,
    var bio: String? = null,
    @get:PropertyName("image_url") @set:PropertyName("image_url") var imageUrl: String? = null
): Serializable