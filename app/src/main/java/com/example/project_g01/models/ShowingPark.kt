package com.example.project_g01.models

import java.io.Serializable

data class ShowingPark(
    val parkName: String,
    val parkImage: String,
    val parkAddress: String,
    val parkUrl: String,
    val parkDescription: String
) : Serializable {
}
