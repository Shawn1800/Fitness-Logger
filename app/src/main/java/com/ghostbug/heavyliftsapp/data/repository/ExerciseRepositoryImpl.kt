package com.ghostbug.heavyliftsapp.data.repository

import com.ghostbug.heavyliftsapp.data.domain.ExerciseEntity
import com.ghostbug.heavyliftsapp.data.remote.dto.ExerciseEntityDto
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ExerciseRepositoryImpl(
    private val postgrest: Postgrest,
    private val auth: Auth,
) : ExerciseRepository {

    private companion object {
        const val TABLE_EXERCISES = "exercises"
    }

    override suspend fun insertExercises(exercises: List<ExerciseEntity>) {
        withContext(Dispatchers.IO) {
            val dtos = exercises.map { it.toDto() }
            postgrest.from(TABLE_EXERCISES).insert(dtos)
        }
    }

    override suspend fun getAllExercises(): List<ExerciseEntity> {
        return withContext(Dispatchers.IO) {
            postgrest.from(TABLE_EXERCISES)
                .select()
                .decodeList<ExerciseEntityDto>()
                .map { it.toDomain() }
        }
    }

    override suspend fun searchExercises(query: String): List<ExerciseEntity> {
        return withContext(Dispatchers.IO) {
            postgrest.from(TABLE_EXERCISES)
                .select {
                    filter {
                        ilike("exercise_name", "%$query%")
                    }
                }
                .decodeList<ExerciseEntityDto>()
                .map { it.toDomain() }
        }
    }

    override suspend fun getExerciseByCategory(category: String): List<ExerciseEntity> {
        return withContext(Dispatchers.IO) {
            postgrest.from(TABLE_EXERCISES)
                .select {
                    filter {
                        eq("category", category)
                    }
                }
                .decodeList<ExerciseEntityDto>()
                .map { it.toDomain() }
        }
    }

    override suspend fun searchExerciseByCategory(
        query: String,
        category: String
    ): List<ExerciseEntity> {
        return withContext(Dispatchers.IO) {
            postgrest.from(TABLE_EXERCISES)
                .select {
                    filter {
                        eq("category", category)
                        ilike("exercise_name", "%$query%")
                    }
                }
                .decodeList<ExerciseEntityDto>()
                .map { it.toDomain() }
        }
    }

    private fun ExerciseEntityDto.toDomain(): ExerciseEntity {
        return ExerciseEntity(
            id = id,
            exerciseName = exerciseName,
            category = category
        )
    }

    private fun ExerciseEntity.toDto(): ExerciseEntityDto {
        return ExerciseEntityDto(
            id = id,
            exerciseName = exerciseName,
            category = category
        )
    }
}
