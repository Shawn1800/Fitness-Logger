package com.ghostbug.heavyliftsapp.data.health

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContract
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.HealthConnectFeatures
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.ActiveCaloriesBurnedRecord
import androidx.health.connect.client.records.DistanceRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.records.WeightRecord
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus
import java.time.Instant
import java.time.ZonedDateTime
import kotlin.time.toJavaInstant

class HealthConnectManager(private val context: Context) {

    private val healthConnectClient by lazy {
        HealthConnectClient.getOrCreate(context)
    }

    val permissions = setOf(
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getReadPermission(ActiveCaloriesBurnedRecord::class),
        HealthPermission.getReadPermission(DistanceRecord::class),
        HealthPermission.getReadPermission(WeightRecord::class)
    )

    fun checkAvailability(): HealthConnectAvailability {
        return when (HealthConnectClient.getSdkStatus(context)) {
            HealthConnectClient.SDK_AVAILABLE -> HealthConnectAvailability.AVAILABLE
            HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED -> {
                promptInstall()
                HealthConnectAvailability.UPDATE_REQUIRED
            }
            else -> HealthConnectAvailability.NOT_SUPPORTED
        }
    }

    private fun promptInstall() {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(
                "market://details?id=com.google.android.apps.healthdata" +
                        "&url=healthconnect%3A%2F%2Fonboarding"
            )
            setPackage("com.android.vending")
            putExtra("overlay", true)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    fun requestPermissionsActivityContract(): ActivityResultContract<Set<String>, Set<String>> {
        return PermissionController.createRequestPermissionResultContract()
    }

    suspend fun hasAllPermissions(): Boolean {
        val granted = healthConnectClient.permissionController.getGrantedPermissions()
        val missing = permissions - granted
        println("DEBUG ── Missing permissions: $missing")
        return missing.isEmpty()
    }

    suspend fun revokeAllPermissions() {
        healthConnectClient.permissionController.revokeAllPermissions()
    }

    fun isFeatureAvailable(feature: Int): Boolean {
        return healthConnectClient
            .features
            .getFeatureStatus(feature) == HealthConnectFeatures.FEATURE_STATUS_AVAILABLE
    }

    // ─── Steps ────────────────────────────────────────────────────────────────

    suspend fun readStepsForDay(date: LocalDate): Long? {
        if (!hasAllPermissions()) return null
        return try {
            val tz = TimeZone.currentSystemDefault()
            val start = date.atStartOfDayIn(tz).toJavaInstant()
            val end = date.plus(1, DateTimeUnit.DAY).atStartOfDayIn(tz).toJavaInstant()

            val response = healthConnectClient.aggregate(
                AggregateRequest(
                    metrics = setOf(StepsRecord.COUNT_TOTAL),
                    timeRangeFilter = TimeRangeFilter.between(start, end)
                )
            )
            val result = response[StepsRecord.COUNT_TOTAL]
            println("DEBUG ── HC steps: $result")
            result
        } catch (e: Exception) {
            println("DEBUG ── steps exception: ${e.message}")
            null
        }
    }

    // ─── Active calories ──────────────────────────────────────────────────────

    suspend fun readCaloriesForDay(date: LocalDate): Double? {
        if (!hasAllPermissions()) {
            println("DEBUG CALORIES ── no permissions")
            return null
        }
        return try {
            val tz = TimeZone.currentSystemDefault()
            val start = date.atStartOfDayIn(tz).toJavaInstant()
            val end = date.plus(1, DateTimeUnit.DAY).atStartOfDayIn(tz).toJavaInstant()

            val response = healthConnectClient.aggregate(
                AggregateRequest(
                    metrics = setOf(ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL),
                    timeRangeFilter = TimeRangeFilter.between(start, end)
                )
            )
            val result = response[ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL]
                ?.inKilocalories
            println("DEBUG CALORIES ── HC active calories: $result")
            result
        } catch (e: Exception) {
            println("DEBUG CALORIES ── active exception: ${e.message}")
            null
        }
    }




    // ─── Distance ─────────────────────────────────────────────────────────────

    suspend fun readDistanceForDay(date: LocalDate): Double? {
        if (!hasAllPermissions()) return null
        return try {
            val tz = TimeZone.currentSystemDefault()
            val start = date.atStartOfDayIn(tz).toJavaInstant()
            val end = date.plus(1, DateTimeUnit.DAY).atStartOfDayIn(tz).toJavaInstant()

            val response = healthConnectClient.aggregate(
                AggregateRequest(
                    metrics = setOf(DistanceRecord.DISTANCE_TOTAL),
                    timeRangeFilter = TimeRangeFilter.between(start, end)
                )
            )
            val result = response[DistanceRecord.DISTANCE_TOTAL]?.inKilometers
            println("DEBUG ── HC distance: $result km")
            result
        } catch (e: Exception) {
            println("DEBUG ── distance exception: ${e.message}")
            null
        }
    }

    // ─── Weight ───────────────────────────────────────────────────────────────

    suspend fun readWeightInputs(start: Instant, end: Instant): List<WeightRecord> {
        return try {
            healthConnectClient.readRecords(
                ReadRecordsRequest(
                    recordType = WeightRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(start, end)
                )
            ).records
        } catch (e: Exception) {
            emptyList()
        }
    }
}

enum class HealthConnectAvailability {
    AVAILABLE,
    UPDATE_REQUIRED,
    NOT_SUPPORTED
}