package com.ghostbug.heavyLifts.data.repository

import com.ghostbug.heavyLifts.data.domain.OneRepMaxEntity
import com.ghostbug.heavyLifts.data.remote.dto.OneRepMaxEntityDto
import com.ghostbug.heavyLifts.data.remote.dto.toDomain
import com.ghostbug.heavyLifts.data.remote.dto.toDto
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate

class OneRepMaxRepositoryImpl(
   private val postgrest: Postgrest,
   private val auth: Auth
) : OneRepMaxRepository {

    private val currentUserId: String?
        get() = auth.currentUserOrNull()?.id

    override suspend fun insert(entity: OneRepMaxEntity) {
        val userId = currentUserId ?: return
        withContext(Dispatchers.IO){
            val dto = entity.toDto().copy(userId = userId)
            postgrest.from("one_rep_max")
                .insert(dto)
        }
    }

    override suspend fun getChangePercentageForDate(exerciseId: Int, date: Long): Double? {
        val userId = currentUserId ?: return null
        return withContext(Dispatchers.IO){
            // Use YYYY-MM-DD format for consistent filtering
            val dateString = LocalDate.ofEpochDay(date / 86400000).toString()

            val result = postgrest.from("one_rep_max")
                .select(Columns.list("change_percent")) {
                    filter {
                        eq("user_id", userId)
                        eq("exercise_id", exerciseId)
                        eq("date", dateString)
                    }
                    limit(1)
                }
                .decodeSingleOrNull<OneRepMaxEntityDto>()
            result?.changePercent?.toDouble()
        }
    }

    override suspend fun getOneRepMaxForDate(date: Long): List<OneRepMaxEntity> {
        val userId = currentUserId ?: return emptyList()
        return withContext(Dispatchers.IO){
            // Use YYYY-MM-DD format for consistent filtering
            val dateString = LocalDate.ofEpochDay(date / 86400000).toString()

            val result = postgrest.from("one_rep_max")
                .select {
                    filter {
                        eq("user_id", userId)
                        eq("date", dateString)
                    }
                }
                .decodeList<OneRepMaxEntityDto>()
            result.map { it.toDomain() }
        }
    }

    override suspend fun getLatest(exerciseId: Int): OneRepMaxEntity? {
        val userId = currentUserId ?: return null
        return withContext(Dispatchers.IO) {
            val result = postgrest.from("one_rep_max")
                .select {
                    filter {
                        eq("user_id", userId)
                        eq("exercise_id", exerciseId)
                    }
                    order("date", Order.DESCENDING)
                    limit(1)
                }
                .decodeSingleOrNull<OneRepMaxEntityDto>()
            result?.toDomain()
        }
    }

    override suspend fun getPrevious(exerciseId: Int, date: Long): OneRepMaxEntity? {
        val userId = currentUserId ?: return null
        return withContext(Dispatchers.IO) {
            // Use YYYY-MM-DD format for consistent filtering
            val dateString = LocalDate.ofEpochDay(date / 86400000).toString()

            val result = postgrest.from("one_rep_max")
                .select {
                    filter {
                        eq("user_id", userId)
                        eq("exercise_id", exerciseId)
                        lt("date", dateString)
                    }
                    order("date", Order.DESCENDING)
                    limit(1)
                }
                .decodeSingleOrNull<OneRepMaxEntityDto>()
            result?.toDomain()
        }
    }

    override suspend fun deleteOneRepMax(exerciseId: Int, date: Long) {
        val userId = currentUserId ?: return
        withContext(Dispatchers.IO) {
            // Use YYYY-MM-DD format for consistent filtering
            val dateString = LocalDate.ofEpochDay(date / 86400000).toString()

            postgrest.from("one_rep_max")
                .delete {
                    filter {
                        eq("user_id", userId)
                        eq("exercise_id", exerciseId)
                        eq("date", dateString)
                    }
                }
        }
    }
}
