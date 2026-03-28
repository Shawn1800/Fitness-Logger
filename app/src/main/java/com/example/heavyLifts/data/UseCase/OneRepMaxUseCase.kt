package com.example.heavyLifts.data.UseCase

import com.example.heavyLifts.data.entity.OneRepMaxEntity
import com.example.heavyLifts.data.entity.WorkoutEntryEntity
import com.example.heavyLifts.data.repository.OneRepMaxRepository
import com.example.heavyLifts.data.repository.WorkoutRepository

class OneRepMaxUseCase  (
    private  val oneRepMaxRepository: OneRepMaxRepository,
    private val workoutRepository: WorkoutRepository
) {
    suspend operator fun invoke(
        exerciseId:Int ,
        date: Long,
        sets: List<WorkoutEntryEntity>
    ) {

        if (sets.isEmpty()) {
            oneRepMaxRepository.deleteOneRepMax(exerciseId, date)
            return
        }

        val new1RM = sets.maxOf { set ->
            calculate1RM(set.weight, set.reps)
        }

        // Use getPrevious to find the 1RM from the session BEFORE this date
        val previous = oneRepMaxRepository.getPrevious(exerciseId, date)
        val prev1RM = previous?.curr1RM ?: 0.0
        val changePercentage = if (prev1RM > 0.0) ((new1RM - prev1RM) / prev1RM) * 100 else 0.0

        // 4. insert snapshot (Room will REPLACE if entry for same exercise+date exists)
        oneRepMaxRepository.insert(
            OneRepMaxEntity(
                exerciseId = exerciseId,
                curr1RM = new1RM,
                prev1RM = prev1RM,
                changePercent = changePercentage,
                date = date
            )
        )

    }
    private fun calculate1RM(weight: Double, reps: Int): Double {
        return weight * (1 + reps / 30.0)
    }

}