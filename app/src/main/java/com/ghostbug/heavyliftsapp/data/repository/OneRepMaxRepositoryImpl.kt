package com.ghostbug.heavyliftsapp.data.repository

import com.ghostbug.heavyliftsapp.data.domain.OneRepMaxEntity
import com.ghostbug.heavyliftsapp.data.remote.dto.OneRepMaxEntityDto
import com.ghostbug.heavyliftsapp.data.remote.dto.toDomain
import com.ghostbug.heavyliftsapp.data.remote.dto.toDto
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.ZoneId

class OneRepMaxRepositoryImpl(
    private val postgrest: Postgrest,
    private val auth: Auth
) : OneRepMaxRepository {

    private companion object {
        const val TABLE_ONE_REP_MAX = "one_rep_max"
    }

    private val _updates = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    override val updates: SharedFlow<Unit> = _updates.asSharedFlow()

    private val currentUserId: String?
        get() = auth.currentUserOrNull()?.id

    private suspend fun notifyUpdate() {
        _updates.emit(Unit)
    }

    override suspend fun insert(entity: OneRepMaxEntity) {
        val userId = currentUserId ?: return
        withContext(Dispatchers.IO) {
            val dto = entity.toDto().copy(userId = userId)
            postgrest.from(TABLE_ONE_REP_MAX).insert(dto)
            notifyUpdate()
        }
    }

    override suspend fun getChangePercentageForDate(exerciseId: Long, date: Long): Double? {
        val userId = currentUserId ?: return null
        return withContext(Dispatchers.IO) {
            val (startOfDay, endOfDay) = getDateRange(date)
            postgrest.from(TABLE_ONE_REP_MAX)
                .select(Columns.list("change_percent")) {
                    filter {
                        eq("user_id", userId)
                        eq("exercise_id", exerciseId)
                        gte("date", startOfDay)
                        lt("date", endOfDay)
                    }
                    limit(1)
                }
                .decodeSingleOrNull<OneRepMaxEntityDto>()
                ?.changePercent?.toDouble()
        }
    }

    override suspend fun getOneRepMaxForDate(date: Long): List<OneRepMaxEntity> {
        val userId = currentUserId ?: return emptyList()
        return withContext(Dispatchers.IO) {
            val (startOfDay, endOfDay) = getDateRange(date)
            postgrest.from(TABLE_ONE_REP_MAX)
                .select {
                    filter {
                        eq("user_id", userId)
                        and {
                            gte("date", startOfDay)
                            lt("date", endOfDay)
                        }
                    }
                }
                .decodeList<OneRepMaxEntityDto>()
                .map { it.toDomain() }
        }
    }

    override suspend fun getLatest(exerciseId: Long): OneRepMaxEntity? {
        val userId = currentUserId ?: return null
        return withContext(Dispatchers.IO) {
            postgrest.from(TABLE_ONE_REP_MAX)
                .select {
                    filter {
                        eq("user_id", userId)
                        eq("exercise_id", exerciseId)
                    }
                    order("date", Order.DESCENDING)
                    limit(1)
                }
                .decodeSingleOrNull<OneRepMaxEntityDto>()
                ?.toDomain()
        }
    }

    override suspend fun getPersonalBestBefore(exerciseId: Long, date: Long): OneRepMaxEntity? {
        val userId = currentUserId ?: return null
        return withContext(Dispatchers.IO) {
            val (startOfDay, _) = getDateRange(date)
            postgrest.from(TABLE_ONE_REP_MAX)
                .select {
                    filter {
                        eq("user_id", userId)
                        eq("exercise_id", exerciseId)
                        lt("date", startOfDay)
                    }
                    order("curr_1rm", Order.DESCENDING)
                    limit(1)
                }
                .decodeSingleOrNull<OneRepMaxEntityDto>()
                ?.toDomain()
        }
    }

    override suspend fun getNext(exerciseId: Long, date: Long): OneRepMaxEntity? {
        val userId = currentUserId ?: return null
        return withContext(Dispatchers.IO) {
            val (_, endOfDay) = getDateRange(date)
            postgrest.from(TABLE_ONE_REP_MAX)
                .select {
                    filter {
                        eq("user_id", userId)
                        eq("exercise_id", exerciseId)
                        gte("date", endOfDay)
                    }
                    order("date", Order.ASCENDING)
                    limit(1)
                }
                .decodeSingleOrNull<OneRepMaxEntityDto>()
                ?.toDomain()
        }
    }

    override suspend fun deleteOneRepMax(exerciseId: Long, date: Long) {
        val userId = currentUserId ?: return
        withContext(Dispatchers.IO) {
            val (startOfDay, endOfDay) = getDateRange(date)
            postgrest.from(TABLE_ONE_REP_MAX)
                .delete {
                    filter {
                        eq("user_id", userId)
                        eq("exercise_id", exerciseId)
                        and {
                            gte("date", startOfDay)
                            lt("date", endOfDay)
                        }
                    }
                }
            notifyUpdate()
        }
    }

    override suspend fun deleteByExercise(exerciseId: Long) {
        val userId = currentUserId ?: return
        withContext(Dispatchers.IO) {
            postgrest.from(TABLE_ONE_REP_MAX)
                .delete {
                    filter {
                        eq("user_id", userId)
                        eq("exercise_id", exerciseId)
                    }
                }
            notifyUpdate()
        }
    }

    override suspend fun update(entity: OneRepMaxEntity) {
        val userId = currentUserId ?: return
        withContext(Dispatchers.IO) {
            val dto = entity.toDto().copy(userId = userId)
            postgrest.from(TABLE_ONE_REP_MAX).update(dto) {
                filter {
                    eq("user_id", userId)
                    eq("id", entity.id)
                }
            }
            notifyUpdate()
        }
    }

    override suspend fun getByDate(exerciseId: Long, date: Long): OneRepMaxEntity? {
        val userId = currentUserId ?: return null
        return withContext(Dispatchers.IO) {
            val (startOfDay, endOfDay) = getDateRange(date)
            postgrest.from(TABLE_ONE_REP_MAX)
                .select {
                    filter {
                        eq("user_id", userId)
                        eq("exercise_id", exerciseId)
                        and {
                            gte("date", startOfDay)
                            lt("date", endOfDay)
                        }
                    }
                    limit(1)
                }
                .decodeSingleOrNull<OneRepMaxEntityDto>()
                ?.toDomain()
        }
    }

    private fun getDateRange(date: Long): Pair<String, String> {
        val localDate = Instant.ofEpochMilli(date).atZone(ZoneId.systemDefault()).toLocalDate()
        val startOfDay = localDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toString()
        val endOfDay = localDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toString()
        return Pair(startOfDay, endOfDay)
    }
}
