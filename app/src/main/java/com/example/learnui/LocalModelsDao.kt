package com.example.learnui

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.example.learnui.DataClass.LocalModels
import kotlinx.coroutines.flow.Flow

@Dao
interface LocalModelsDao {

    @Upsert
    suspend fun upsertModel(models: LocalModels)

    @Delete
    suspend fun deleteModel(models: LocalModels)

    @Query("SELECT * FROM localmodels")
    fun getModelList(): Flow<List<LocalModels>>

}