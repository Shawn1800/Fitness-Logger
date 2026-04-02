package com.ghostbug.heavyLifts.data.repository


import com.ghostbug.heavyLifts.data.domain.ExerciseEntity
import com.ghostbug.heavyLifts.data.remote.dto.ExerciseEntityDto
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ExerciseRepositoryImpl(
    private val postgrest: Postgrest,
    private val auth: Auth,
) : ExerciseRepository {

    override suspend fun insertExercises(exercises: List<ExerciseEntity>) {
        withContext(Dispatchers.IO) {
            val dto = exercises.map {
                ExerciseEntityDto(
                    id = it.id,
                    exerciseName = it.exerciseName,
                    category = it.category
                )
            }
            postgrest.from("exercises")
                .insert(dto)
        }
    }


    override suspend fun getAllExercises(): List<ExerciseEntity> {
        return withContext(Dispatchers.IO) {
            val result = postgrest.from("exercises")
                .select()
                .decodeList<ExerciseEntityDto>()
            result.map { it.toDomain() }
        }
    }

    override suspend fun searchExercises(query: String): List<ExerciseEntity> {
        return withContext(Dispatchers.IO) {
            val result = postgrest.from("exercises")
                .select {
                    filter {
                        ilike("exercise_name", "%$query%")
                    }
                }
                .decodeList<ExerciseEntityDto>()
            result.map { it.toDomain() }
        }
    }

    override suspend fun getExerciseByCategory(category: String): List<ExerciseEntity> {
        return withContext(Dispatchers.IO) {
            val result = postgrest.from("exercises")
                .select{
                    filter {
                        eq("category", category)
                    }
                }
                .decodeList<ExerciseEntityDto>()
             result.map { it.toDomain() }
        }
    }

    override suspend fun searchExerciseByCategory(
        query: String,
        category: String
    ): List<ExerciseEntity> {
       return withContext(Dispatchers.IO) {
            val result = postgrest.from("exercises")
                .select {
                    filter {
                        eq("category", category)
                        ilike("exercise_name", "%$query%")
                    }
                }
                .decodeList<ExerciseEntityDto>()
           result.map { it.toDomain() }
        }
    }

    private fun ExerciseEntityDto.toDomain(): ExerciseEntity {
        return ExerciseEntity(
            id = id,
            exerciseName = exerciseName,
            category = category
        )
    }
}