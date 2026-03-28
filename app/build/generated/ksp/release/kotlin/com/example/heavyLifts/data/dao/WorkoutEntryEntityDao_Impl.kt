package com.example.heavyLifts.`data`.dao

import androidx.collection.LongSparseArray
import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.appendPlaceholders
import androidx.room.util.getColumnIndex
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.room.util.recursiveFetchLongSparseArray
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.SQLiteStatement
import com.example.heavyLifts.`data`.entity.ExerciseEntity
import com.example.heavyLifts.`data`.entity.WorkoutEntryEntity
import com.example.heavyLifts.`data`.entity.WorkoutWithExercise
import javax.`annotation`.processing.Generated
import kotlin.Double
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.mutableListOf
import kotlin.reflect.KClass
import kotlin.text.StringBuilder
import kotlinx.coroutines.flow.Flow

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class WorkoutEntryEntityDao_Impl(
  __db: RoomDatabase,
) : WorkoutEntryEntityDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfWorkoutEntryEntity: EntityInsertAdapter<WorkoutEntryEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfWorkoutEntryEntity = object : EntityInsertAdapter<WorkoutEntryEntity>() {
      protected override fun createQuery(): String = "INSERT OR REPLACE INTO `workout_entry` (`entryId`,`exercise_id`,`weight`,`reps`,`date`,`sets`) VALUES (nullif(?, 0),?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: WorkoutEntryEntity) {
        statement.bindLong(1, entity.entryId.toLong())
        statement.bindLong(2, entity.exerciseId.toLong())
        statement.bindDouble(3, entity.weight)
        statement.bindLong(4, entity.reps.toLong())
        statement.bindLong(5, entity.date)
        statement.bindLong(6, entity.sets.toLong())
      }
    }
  }

  public override suspend fun insertWorkoutEntry(entry: List<WorkoutEntryEntity>): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfWorkoutEntryEntity.insert(_connection, entry)
  }

  public override fun getWorkoutByDate(date: Long): Flow<List<WorkoutWithExercise>> {
    val _sql: String = "SELECT * FROM workout_entry WHERE date = ?"
    return createFlow(__db, true, arrayOf("exercises", "workout_entry")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, date)
        val _columnIndexOfEntryId: Int = getColumnIndexOrThrow(_stmt, "entryId")
        val _columnIndexOfExerciseId: Int = getColumnIndexOrThrow(_stmt, "exercise_id")
        val _columnIndexOfWeight: Int = getColumnIndexOrThrow(_stmt, "weight")
        val _columnIndexOfReps: Int = getColumnIndexOrThrow(_stmt, "reps")
        val _columnIndexOfDate: Int = getColumnIndexOrThrow(_stmt, "date")
        val _columnIndexOfSets: Int = getColumnIndexOrThrow(_stmt, "sets")
        val _collectionExercise: LongSparseArray<ExerciseEntity?> = LongSparseArray<ExerciseEntity?>()
        while (_stmt.step()) {
          val _tmpKey: Long
          _tmpKey = _stmt.getLong(_columnIndexOfExerciseId)
          _collectionExercise.put(_tmpKey, null)
        }
        _stmt.reset()
        __fetchRelationshipexercisesAscomExampleHeavyLiftsDataEntityExerciseEntity(_connection, _collectionExercise)
        val _result: MutableList<WorkoutWithExercise> = mutableListOf()
        while (_stmt.step()) {
          val _item: WorkoutWithExercise
          val _tmpWorkoutEntry: WorkoutEntryEntity
          val _tmpEntryId: Int
          _tmpEntryId = _stmt.getLong(_columnIndexOfEntryId).toInt()
          val _tmpExerciseId: Int
          _tmpExerciseId = _stmt.getLong(_columnIndexOfExerciseId).toInt()
          val _tmpWeight: Double
          _tmpWeight = _stmt.getDouble(_columnIndexOfWeight)
          val _tmpReps: Int
          _tmpReps = _stmt.getLong(_columnIndexOfReps).toInt()
          val _tmpDate: Long
          _tmpDate = _stmt.getLong(_columnIndexOfDate)
          val _tmpSets: Int
          _tmpSets = _stmt.getLong(_columnIndexOfSets).toInt()
          _tmpWorkoutEntry = WorkoutEntryEntity(_tmpEntryId,_tmpExerciseId,_tmpWeight,_tmpReps,_tmpDate,_tmpSets)
          val _tmpExercise: ExerciseEntity?
          val _tmpKey_1: Long
          _tmpKey_1 = _stmt.getLong(_columnIndexOfExerciseId)
          _tmpExercise = _collectionExercise.get(_tmpKey_1)
          if (_tmpExercise == null) {
            error("Relationship item 'exercise' was expected to be NON-NULL but is NULL in @Relation involving a parent column named 'exercise_id' and entityColumn named 'exercise_id'.")
          }
          _item = WorkoutWithExercise(_tmpWorkoutEntry,_tmpExercise)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getWorkoutByExercise(exerciseId: Int): Flow<List<WorkoutEntryEntity>> {
    val _sql: String = "SELECT * FROM workout_entry WHERE exercise_id = ?"
    return createFlow(__db, false, arrayOf("workout_entry")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, exerciseId.toLong())
        val _columnIndexOfEntryId: Int = getColumnIndexOrThrow(_stmt, "entryId")
        val _columnIndexOfExerciseId: Int = getColumnIndexOrThrow(_stmt, "exercise_id")
        val _columnIndexOfWeight: Int = getColumnIndexOrThrow(_stmt, "weight")
        val _columnIndexOfReps: Int = getColumnIndexOrThrow(_stmt, "reps")
        val _columnIndexOfDate: Int = getColumnIndexOrThrow(_stmt, "date")
        val _columnIndexOfSets: Int = getColumnIndexOrThrow(_stmt, "sets")
        val _result: MutableList<WorkoutEntryEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: WorkoutEntryEntity
          val _tmpEntryId: Int
          _tmpEntryId = _stmt.getLong(_columnIndexOfEntryId).toInt()
          val _tmpExerciseId: Int
          _tmpExerciseId = _stmt.getLong(_columnIndexOfExerciseId).toInt()
          val _tmpWeight: Double
          _tmpWeight = _stmt.getDouble(_columnIndexOfWeight)
          val _tmpReps: Int
          _tmpReps = _stmt.getLong(_columnIndexOfReps).toInt()
          val _tmpDate: Long
          _tmpDate = _stmt.getLong(_columnIndexOfDate)
          val _tmpSets: Int
          _tmpSets = _stmt.getLong(_columnIndexOfSets).toInt()
          _item = WorkoutEntryEntity(_tmpEntryId,_tmpExerciseId,_tmpWeight,_tmpReps,_tmpDate,_tmpSets)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getWorkoutByExerciseAndDate(exerciseId: Int, date: Long): List<WorkoutEntryEntity> {
    val _sql: String = "SELECT * FROM workout_entry WHERE exercise_id = ? AND date = ?  "
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, exerciseId.toLong())
        _argIndex = 2
        _stmt.bindLong(_argIndex, date)
        val _columnIndexOfEntryId: Int = getColumnIndexOrThrow(_stmt, "entryId")
        val _columnIndexOfExerciseId: Int = getColumnIndexOrThrow(_stmt, "exercise_id")
        val _columnIndexOfWeight: Int = getColumnIndexOrThrow(_stmt, "weight")
        val _columnIndexOfReps: Int = getColumnIndexOrThrow(_stmt, "reps")
        val _columnIndexOfDate: Int = getColumnIndexOrThrow(_stmt, "date")
        val _columnIndexOfSets: Int = getColumnIndexOrThrow(_stmt, "sets")
        val _result: MutableList<WorkoutEntryEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: WorkoutEntryEntity
          val _tmpEntryId: Int
          _tmpEntryId = _stmt.getLong(_columnIndexOfEntryId).toInt()
          val _tmpExerciseId: Int
          _tmpExerciseId = _stmt.getLong(_columnIndexOfExerciseId).toInt()
          val _tmpWeight: Double
          _tmpWeight = _stmt.getDouble(_columnIndexOfWeight)
          val _tmpReps: Int
          _tmpReps = _stmt.getLong(_columnIndexOfReps).toInt()
          val _tmpDate: Long
          _tmpDate = _stmt.getLong(_columnIndexOfDate)
          val _tmpSets: Int
          _tmpSets = _stmt.getLong(_columnIndexOfSets).toInt()
          _item = WorkoutEntryEntity(_tmpEntryId,_tmpExerciseId,_tmpWeight,_tmpReps,_tmpDate,_tmpSets)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteSetById(entryId: Int) {
    val _sql: String = "DELETE  FROM workout_entry WHERE entryId = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, entryId.toLong())
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteWorkoutByExerciseAndDate(exerciseId: Int, date: Long) {
    val _sql: String = "DELETE FROM workout_entry WHERE exercise_id = ? AND date =? "
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, exerciseId.toLong())
        _argIndex = 2
        _stmt.bindLong(_argIndex, date)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  private fun __fetchRelationshipexercisesAscomExampleHeavyLiftsDataEntityExerciseEntity(_connection: SQLiteConnection, _map: LongSparseArray<ExerciseEntity?>) {
    if (_map.isEmpty()) {
      return
    }
    if (_map.size() > 999) {
      recursiveFetchLongSparseArray(_map, false) { _tmpMap ->
        __fetchRelationshipexercisesAscomExampleHeavyLiftsDataEntityExerciseEntity(_connection, _tmpMap)
      }
      return
    }
    val _stringBuilder: StringBuilder = StringBuilder()
    _stringBuilder.append("SELECT `exercise_id`,`exercise_name`,`category` FROM `exercises` WHERE `exercise_id` IN (")
    val _inputSize: Int = _map.size()
    appendPlaceholders(_stringBuilder, _inputSize)
    _stringBuilder.append(")")
    val _sql: String = _stringBuilder.toString()
    val _stmt: SQLiteStatement = _connection.prepare(_sql)
    var _argIndex: Int = 1
    for (i in 0 until _map.size()) {
      val _item: Long = _map.keyAt(i)
      _stmt.bindLong(_argIndex, _item)
      _argIndex++
    }
    try {
      val _itemKeyIndex: Int = getColumnIndex(_stmt, "exercise_id")
      if (_itemKeyIndex == -1) {
        return
      }
      val _columnIndexOfExerciseId: Int = 0
      val _columnIndexOfExerciseName: Int = 1
      val _columnIndexOfCategory: Int = 2
      while (_stmt.step()) {
        val _tmpKey: Long
        _tmpKey = _stmt.getLong(_itemKeyIndex)
        if (_map.containsKey(_tmpKey)) {
          val _item_1: ExerciseEntity
          val _tmpExerciseId: Int
          _tmpExerciseId = _stmt.getLong(_columnIndexOfExerciseId).toInt()
          val _tmpExerciseName: String
          _tmpExerciseName = _stmt.getText(_columnIndexOfExerciseName)
          val _tmpCategory: String
          _tmpCategory = _stmt.getText(_columnIndexOfCategory)
          _item_1 = ExerciseEntity(_tmpExerciseId,_tmpExerciseName,_tmpCategory)
          _map.put(_tmpKey, _item_1)
        }
      }
    } finally {
      _stmt.close()
    }
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
