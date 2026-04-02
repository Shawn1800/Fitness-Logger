package com.ghostbug.heavyLifts.data.repository

import com.ghostbug.heavyLifts.data.domain.WorkoutEntryEntity
import com.ghostbug.heavyLifts.data.domain.WorkoutWithExercise
import com.ghostbug.heavyLifts.data.remote.dto.WorkoutEntryEntityDto
import com.ghostbug.heavyLifts.data.remote.dto.WorkoutWithExerciseDto
import com.ghostbug.heavyLifts.data.remote.dto.toDomain
import com.ghostbug.heavyLifts.data.remote.dto.toDto
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class WorkoutRepositoryImpl (
   private val postgrest: Postgrest,
   private val auth: Auth
): WorkoutRepository {

    private val currentUserId: String?
        get() = auth.currentUserOrNull()?.id

    override suspend fun insertWorkoutEntry(entry: List<WorkoutEntryEntity>) {
        val userId = currentUserId ?: return
        withContext(Dispatchers.IO){
            val dtos = entry.map { it.toDto().copy(userId = userId) }
            postgrest.from("workout_entries")
                .insert(dtos)
        }
    }

    override suspend fun getWorkoutByDate(date: Long): List<WorkoutWithExercise> {
        val userId = currentUserId ?: return emptyList()

        return withContext(Dispatchers.IO) {
            val localDate = Instant.ofEpochMilli(date).atZone(ZoneId.systemDefault()).toLocalDate()
            val startOfDay = localDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toString()
            val endOfDay = localDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toString()

            android.util.Log.d("WORKOUT_DEBUG", "QUERY → startOfDay=$startOfDay, endOfDay=$endOfDay")

            val result = postgrest.from("workout_entries")
                .select(Columns.raw("*, exercises(*)")) {
                    filter {
                        eq("user_id", userId)
                        and {                     // ✅ wrap date range in and {}
                            gte("date", startOfDay)
                            lt("date", endOfDay)
                        }
                    }
                }
                .decodeList<WorkoutWithExerciseDto>()

            android.util.Log.d("WORKOUT_DEBUG", "RESULTS → ${result.map { it.date }}")

            result.map { it.toDomain() }
        }
    }

    override suspend fun deleteSetById(id: Int) {
        val userId = currentUserId ?: return
        withContext(Dispatchers.IO) {
            postgrest.from("workout_entries")
                .delete {
                    filter {
                        eq("user_id", userId)
                        eq("id", id)
                    }
                }
        }
    }

    override suspend fun getWorkoutByExerciseAndDate(exerciseId: Int, date: Long): List<WorkoutEntryEntity> {
        val userId = currentUserId ?: return emptyList()

        return withContext(Dispatchers.IO) {
            val localDate = Instant.ofEpochMilli(date).atZone(ZoneId.systemDefault()).toLocalDate()
            val startOfDay = localDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toString()
            val endOfDay = localDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toString()

            val result = postgrest.from("workout_entries")
                .select {
                    filter {
                        eq("user_id", userId)
                        eq("exercise_id", exerciseId)
                        and {                     // ✅ wrap date range in and {}
                            gte("date", startOfDay)
                            lt("date", endOfDay)
                        }
                    }
                }
                .decodeList<WorkoutEntryEntityDto>()

            result.map { it.toDomain() }
        }
    }
}
