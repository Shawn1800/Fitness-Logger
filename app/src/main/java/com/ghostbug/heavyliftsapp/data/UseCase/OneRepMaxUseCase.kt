package com.ghostbug.heavyliftsapp.data.UseCase

import com.ghostbug.heavyliftsapp.data.domain.OneRepMaxEntity
import com.ghostbug.heavyliftsapp.data.domain.WorkoutEntryEntity
import com.ghostbug.heavyliftsapp.data.repository.OneRepMaxRepository
import com.ghostbug.heavyliftsapp.data.repository.WorkoutRepository

class OneRepMaxUseCase(
    private val oneRepMaxRepository: OneRepMaxRepository,
    private val workoutRepository: WorkoutRepository
) {
    suspend operator fun invoke(
        exerciseId: Int,
        date: Long,
        sets: List<WorkoutEntryEntity>
    ) {
        // 1. Rebuild the entire timeline for this exercise
        recalculateTimeline(exerciseId)
    }

    private suspend fun recalculateTimeline(exerciseId: Int) {
        // Get ALL workout sessions for this exercise
        val allWorkouts = workoutRepository.getWorkoutsByExercise(exerciseId)
        
        // If NO workouts exist at all for this exercise, delete all 1RM history for it
        if (allWorkouts.isEmpty()) {
            oneRepMaxRepository.deleteByExercise(exerciseId)
            return
        }

        // Group by date
        val sessionsByDate = allWorkouts.groupBy { 
            val instant = java.time.Instant.ofEpochMilli(it.date)
            instant.atZone(java.time.ZoneId.systemDefault()).toLocalDate()
        }.toSortedMap()

        // Clear existing 1RM history for this exercise to start fresh
        oneRepMaxRepository.deleteByExercise(exerciseId)

        var runningPersonalBest = 0.0f
        
        sessionsByDate.forEach { (localDate, sessionSets) ->
            val sessionDateMillis = localDate.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
            val sessionMax1RM = sessionSets.maxOf { calculate1RM(it.weight, it.reps) }
            val prevBest = runningPersonalBest
            val changePercent = if (prevBest > 0) ((sessionMax1RM - prevBest) / prevBest) * 100 else 0.0f

            oneRepMaxRepository.insert(
                OneRepMaxEntity(
                    id = 0,
                    exerciseId = exerciseId,
                    curr1RM = sessionMax1RM,
                    prev1RM = prevBest,
                    changePercent = changePercent,
                    date = sessionDateMillis,
                    userId = ""
                )
            )

            if (sessionMax1RM > runningPersonalBest) {
                runningPersonalBest = sessionMax1RM
            }
        }
    }

    private fun calculate1RM(weight: Float, reps: Int): Float {
        if (reps == 1) return weight
        return if (reps > 0) weight * (1 + reps / 30f) else weight
    }
}
