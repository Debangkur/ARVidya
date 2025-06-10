package com.example.learnui.DataClass


import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("localmodels")
data class LocalModels(
    val name: String,
    val location: String,
    val tts: String,
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0
)
