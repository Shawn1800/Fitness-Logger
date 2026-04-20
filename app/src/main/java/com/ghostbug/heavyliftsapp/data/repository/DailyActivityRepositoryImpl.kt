package com.ghostbug.heavyliftsapp.data.repository

import com.ghostbug.heavyliftsapp.data.health.HealthConnectManager
import com.ghostbug.heavyliftsapp.data.health.RecordingApiManager
import com.ghostbug.heavyliftsapp.data.domain.ActivityGoals
import com.ghostbug.heavyliftsapp.data.domain.DailyActivity
import com.ghostbug.heavyliftsapp.data.remote.dto.ActivityGoalsDto
import com.ghostbug.heavyliftsapp.data.remote.dto.DailyActivityDto
import com.ghostbug.heavyliftsapp.data.remote.dto.toDomain
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.auth.Auth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.minus
import kotlinx.datetime.todayIn
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.time.toJavaInstant


class DailyActivityRepositoryImpl(
    private val postgrest: Postgrest,
    private val auth: Auth,
    private val healthConnectManager: HealthConnectManager,
    private val recordingApiManager: RecordingApiManager,
    private val userProfileRepository: UserProfileRepository
) : DailyActivityRepository {

    companion object {
        const val TABLE_DAILY_ACTIVITY = "daily_activity"
        const val TABLE_ACTIVITY_GOALS = "activity_goals"
    }

    private val currentUserId: String?
        get() = auth.currentUserOrNull()?.id

    private val today: LocalDate
        get() = Clock.System.todayIn(TimeZone.currentSystemDefault())

    // ─── Today's activity ────────────────────────────────────────────────────

    override suspend fun getTodayActivity(): DailyActivity {
        return withContext(Dispatchers.IO) {
            val uid = currentUserId ?: return@withContext emptyActivity()

            val hcSteps = healthConnectManager.readStepsForDay(today)
            val hcDistance = healthConnectManager.readDistanceForDay(today)
            val (calories, calorieSource) = getCaloriesForDay(today)

            if (hcSteps != null && hcSteps > 0) {
                return@withContext DailyActivity(
                    userId = uid,
                    date = today,
                    steps = hcSteps.toInt(),
                    stepSource = "health_connect",
                    caloriesBurned = calories,
                    calorieSource = calorieSource,
                    distanceKm = hcDistance?.toFloat() ?: 0f
                )
            }

            // fallback — Recording API
            val recordingSteps = recordingApiManager.readStepsForDay(today)
            val recordingDistance = recordingApiManager.readDistanceForDay(today)

            DailyActivity(
                userId = uid,
                date = today,
                steps = recordingSteps.toInt(),
                stepSource = "sensor",
                caloriesBurned = estimateCaloriesFromSteps(recordingSteps),
                calorieSource = "estimated",
                distanceKm = recordingDistance
            )
        }
    }

    // ─── History ──────────────────────────────────────────────────────────────

    override suspend fun getActivityHistory(days: Int): List<DailyActivity> {
        val uid = currentUserId ?: return emptyList()
        return withContext(Dispatchers.IO) {
            try {
                val fromDate = today.minus(days, DateTimeUnit.DAY)
                postgrest.from(TABLE_DAILY_ACTIVITY)
                    .select {
                        filter {
                            eq("user_id", uid)
                            gte("date", fromDate.toString())
                            lte("date", today.toString())
                        }
                        order(
                            "date",
                            io.github.jan.supabase.postgrest.query.Order.DESCENDING
                        )
                    }
                    .decodeList<DailyActivityDto>()
                    .map { it.toDomain() }
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    // ─── Sync to Supabase ─────────────────────────────────────────────────────

    override suspend fun syncToSupabase(date: LocalDate) {
        val uid = currentUserId ?: return
        withContext(Dispatchers.IO) {
            try {
                val hcSteps = healthConnectManager.readStepsForDay(date)
                val hcDistance = healthConnectManager.readDistanceForDay(date)
                val (calories, calorieSource) = getCaloriesForDay(date)

                val steps: Long
                val stepSource: String
                val distance: Float

                if (hcSteps != null && hcSteps > 0) {
                    steps = hcSteps
                    stepSource = "health_connect"
                    distance = hcDistance?.toFloat() ?: 0f
                } else {
                    steps = recordingApiManager.readStepsForDay(date)
                    stepSource = "sensor"
                    distance = recordingApiManager.readDistanceForDay(date)
                }

                val dto = DailyActivityDto(
                    userId = uid,
                    date = date,
                    steps = steps.toInt(),
                    stepSource = stepSource,
                    caloriesBurned = calories,
                    calorieSource = calorieSource,
                    distanceKm = distance
                )

                postgrest.from(TABLE_DAILY_ACTIVITY)
                    .upsert(dto) {
                        onConflict = "user_id,date"
                    }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // ─── Goals ────────────────────────────────────────────────────────────────

    override suspend fun getCurrentGoal(): ActivityGoals? {
        val uid = currentUserId ?: return null
        return withContext(Dispatchers.IO) {
            try {
                postgrest.from(TABLE_ACTIVITY_GOALS)
                    .select {
                        filter {
                            eq("user_id", uid)
                            lte("effective_from", today.toString())
                        }
                        order(
                            "effective_from",
                            io.github.jan.supabase.postgrest.query.Order.DESCENDING
                        )
                        limit(1)
                    }
                    .decodeSingleOrNull<ActivityGoalsDto>()
                    ?.toDomain()
            } catch (e: Exception) {
                null
            }
        }
    }

    override suspend fun saveGoal(stepGoal: Int, calorieGoal: Float) {
        val uid = currentUserId ?: return
        withContext(Dispatchers.IO) {
            try {
                val dto = ActivityGoalsDto(
                    userId = uid,
                    stepGoal = stepGoal,
                    calorieGoal = calorieGoal,
                    effectiveFrom = today
                )
                postgrest.from(TABLE_ACTIVITY_GOALS)
                    .upsert(dto) {
                        onConflict = "user_id,effective_from"
                    }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override suspend fun getStepsByDate(steps: Long,createdAt: Instant): DailyActivity {
       val userId = currentUserId?: return emptyActivity()

        return withContext(Dispatchers.IO){
            val result =postgrest.from(TABLE_DAILY_ACTIVITY)
                .select {
                    filter {
                        eq ("user_id",userId)
                        eq("steps",steps)
                        eq("created_at",createdAt)
                        }
                    }
                .decodeSingleOrNull<DailyActivityDto>()
                 result?.toDomain()?:emptyActivity()
                }
        }



    // ─── Calorie logic ────────────────────────────────────────────────────────

    private suspend fun getCaloriesForDay(date: LocalDate): Pair<Float, String> {
        // try active calories from HC
        val activeCalories = healthConnectManager.readCaloriesForDay(date)
        if (activeCalories != null && activeCalories > 0) {
            println("DEBUG CALORIES ── using active: $activeCalories")
            return Pair(activeCalories.toFloat(), "health_connect")
        }

        // fallback — estimate from steps
        val steps = healthConnectManager.readStepsForDay(date) ?: 0L
        println("DEBUG CALORIES ── estimating from steps: $steps")
        return Pair(estimateCaloriesFromSteps(steps), "estimated")
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private fun emptyActivity(): DailyActivity {
        return DailyActivity(
            userId = "",
            date = today,
            steps = 0,
            stepSource = "sensor",
            caloriesBurned = 0f,
            calorieSource = "estimated",
            distanceKm = 0f
        )
    }

    private suspend fun estimateCaloriesFromSteps(steps: Long): Float {
        if (steps == 0L) return 0f
        return try {
            val profile = userProfileRepository.getOwnProfile()
            val weightKg = profile?.userWeight ?: 70f
            val heightCm = profile?.height ?: 170f
            val strideLength = heightCm * 0.415f / 100f
            val distanceKm = (steps * strideLength) / 1000f
            val met = if (steps > 10000) 4.5f else 3.5f
            val timeHours = distanceKm / 5.0f
            met * weightKg * timeHours
        } catch (e: Exception) {
            steps * 0.04f
        }
    }



    private suspend fun getUserBmr(): Double {
        return try {
            val profile = userProfileRepository.getOwnProfile()

            val weightKg = profile?.userWeight?.toDouble()
                ?.takeIf { it > 0 } ?: 70.0
            val heightCm = profile?.height?.toDouble()
                ?.takeIf { it > 0 } ?: 170.0
            val age = profile?.age
                ?.takeIf { it > 0 } ?: 25

            println("DEBUG BMR ── weight: $weightKg height: $heightCm age: $age gender: ${profile?.gender}")

            val bmr = when (profile?.gender?.name?.lowercase()) {
                "male" -> (10 * weightKg) + (6.25 * heightCm) - (5 * age) + 5
                "female" -> (10 * weightKg) + (6.25 * heightCm) - (5 * age) - 161
                else -> (10 * weightKg) + (6.25 * heightCm) - (5 * age) - 78
            }

            // sanity check — BMR should always be between 1000 and 4000
            if (bmr < 1000 || bmr > 4000) {
                println("DEBUG BMR ── BMR out of range: $bmr, using default 1800")
                return 1800.0
            }

            println("DEBUG BMR ── calculated BMR: $bmr kcal/day")
            bmr
        } catch (e: Exception) {
            println("DEBUG BMR ── exception: ${e.message}, using default 1800")
            1800.0
        }
    }
}