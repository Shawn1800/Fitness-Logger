package com.ghostbug.heavyLifts.data.UseCase

import com.ghostbug.heavyLifts.data.domain.OneRepMaxEntity
import com.ghostbug.heavyLifts.data.domain.WorkoutEntryEntity
import com.ghostbug.heavyLifts.data.repository.OneRepMaxRepository

class OneRepMaxUseCase (
    private val oneRepMaxRepository: OneRepMaxRepository,
) {
    suspend operator fun invoke(
        exerciseId: Int,
        date: Long,
        sets: List<WorkoutEntryEntity>
    ) {
        // 1. If no sets, delete the 1RM entry for this exercise/date
        if (sets.isEmpty()) {
            oneRepMaxRepository.deleteOneRepMax(exerciseId, date)
            return
        }

        // 2. Calculate the highest 1RM from the current session sets
        val new1RM = sets.maxOf { set ->
            calculate1RM(set.weight, set.reps)
        }

        // 3. Get the previous session's 1RM (before this date) to calculate change percentage
        // IMPORTANT: Do this BEFORE deleting today's record
        val previous = oneRepMaxRepository.getPrevious(exerciseId, date)
        // Use curr1RM from previous record as the baseline for comparison
        val prev1RM = previous?.curr1RM ?: 0.0f
        val changePercentage = if (prev1RM > 0.0f) ((new1RM - prev1RM) / prev1RM) * 100 else 0.0f

        // 4. Delete the existing record for today to avoid duplicates before inserting the new calculation
        oneRepMaxRepository.deleteOneRepMax(exerciseId, date)

        // 5. Insert the new snapshot with:
        // - curr1RM: The 1RM calculated from today's sets
        // - prev1RM: The 1RM from the previous session (before today)
        // - changePercent: Percentage change from previous to current
        oneRepMaxRepository.insert(
            OneRepMaxEntity(
                exerciseId = exerciseId,
                curr1RM = new1RM,           // Today's calculated 1RM
                prev1RM = prev1RM,          // Previous session's 1RM (from database)
                changePercent = changePercentage,
                date = date,
                userId = "",                // Set by repository
                id = 0
            )
        )
    }

    private fun calculate1RM(weight: Float, reps: Int): Float {
        // Brzycki Formula: 1RM = weight * (1 + reps / 30)
        return if (reps > 0) weight * (1 + reps / 30f) else weight
    }
}


