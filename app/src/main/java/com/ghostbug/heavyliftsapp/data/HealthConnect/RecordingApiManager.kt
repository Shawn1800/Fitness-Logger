package com.ghostbug.heavyliftsapp.data.health  // fix package casing

import android.Manifest
import android.content.Context
import androidx.annotation.RequiresPermission
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.fitness.FitnessLocal
import com.google.android.gms.fitness.LocalRecordingClient
import com.google.android.gms.fitness.data.LocalDataSet
import com.google.android.gms.fitness.data.LocalDataType
import com.google.android.gms.fitness.request.LocalDataReadRequest
import kotlinx.coroutines.tasks.await
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus
import java.util.concurrent.TimeUnit

class RecordingApiManager(private val context: Context) {

    private val client: LocalRecordingClient by lazy {
        FitnessLocal.getLocalRecordingClient(context)
    }

    fun isAvailable(): Boolean {
        val result = GoogleApiAvailability.getInstance()
            .isGooglePlayServicesAvailable(
                context,
                LocalRecordingClient.LOCAL_RECORDING_CLIENT_MIN_VERSION_CODE
            )
        return result == ConnectionResult.SUCCESS
    }

    @RequiresPermission(Manifest.permission.ACTIVITY_RECOGNITION)
    suspend fun subscribe() {
        if (!isAvailable()) return
        try {
            client.subscribe(LocalDataType.TYPE_STEP_COUNT_DELTA).await()
            client.subscribe(LocalDataType.TYPE_DISTANCE_DELTA).await()
            // no TYPE_CALORIES_EXPENDED — includes BMR, handled in repository via HC
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun unsubscribe() {
        if (!isAvailable()) return
        try {
            client.unsubscribe(LocalDataType.TYPE_STEP_COUNT_DELTA).await()
            client.unsubscribe(LocalDataType.TYPE_DISTANCE_DELTA).await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun readStepsForDay(date: LocalDate): Long {
        if (!isAvailable()) return 0L
        return try {
            val tz = TimeZone.currentSystemDefault()
            val startSecs = date.atStartOfDayIn(tz).epochSeconds
            val endSecs = date.plus(1, DateTimeUnit.DAY).atStartOfDayIn(tz).epochSeconds

            val request = LocalDataReadRequest.Builder()
                .aggregate(LocalDataType.TYPE_STEP_COUNT_DELTA)
                .bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(startSecs, endSecs, TimeUnit.SECONDS)
                .build()

            var totalSteps = 0L
            val response = client.readData(request).await()
            for (dataSet in response.buckets.flatMap { it.dataSets }) {
                totalSteps += extractSteps(dataSet)
            }
            println("DEBUG RECORDING ── steps: $totalSteps")
            totalSteps
        } catch (e: Exception) {
            println("DEBUG RECORDING ── steps exception: ${e.message}")
            0L
        }
    }

    suspend fun readDistanceForDay(date: LocalDate): Float {
        if (!isAvailable()) return 0f
        return try {
            val tz = TimeZone.currentSystemDefault()
            val startSecs = date.atStartOfDayIn(tz).epochSeconds
            val endSecs = date.plus(1, DateTimeUnit.DAY).atStartOfDayIn(tz).epochSeconds

            val request = LocalDataReadRequest.Builder()
                .aggregate(LocalDataType.TYPE_DISTANCE_DELTA)
                .bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(startSecs, endSecs, TimeUnit.SECONDS)
                .build()

            var totalDistance = 0f
            val response = client.readData(request).await()
            for (dataSet in response.buckets.flatMap { it.dataSets }) {
                for (dp in dataSet.dataPoints) {
                    for (field in dp.dataType.fields) {
                        totalDistance += dp.getValue(field).asFloat()
                    }
                }
            }
            val distanceKm = totalDistance / 1000f
            println("DEBUG RECORDING ── distance: $distanceKm km")
            distanceKm
        } catch (e: Exception) {
            println("DEBUG RECORDING ── distance exception: ${e.message}")
            0f
        }
    }

    // ─── Private helpers ──────────────────────────────────────────────────────

    private fun extractSteps(dataSet: LocalDataSet): Long {
        var steps = 0L
        for (dp in dataSet.dataPoints) {
            for (field in dp.dataType.fields) {
                steps += dp.getValue(field).asInt()
            }
        }
        return steps
    }
}