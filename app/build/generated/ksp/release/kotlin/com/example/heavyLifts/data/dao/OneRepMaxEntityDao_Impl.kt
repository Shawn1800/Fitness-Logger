package com.example.heavyLifts.`data`.dao

import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import com.example.heavyLifts.`data`.entity.OneRepMaxEntity
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
import kotlinx.coroutines.flow.Flow

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class OneRepMaxEntityDao_Impl(
  __db: RoomDatabase,
) : OneRepMaxEntityDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfOneRepMaxEntity: EntityInsertAdapter<OneRepMaxEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfOneRepMaxEntity = object : EntityInsertAdapter<OneRepMaxEntity>() {
      protected override fun createQuery(): String = "INSERT OR REPLACE INTO `one_rep_max` (`oneRMId`,`exercise_id`,`curr_1rm`,`prev_1rm`,`change_percent`,`date`) VALUES (nullif(?, 0),?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: OneRepMaxEntity) {
        statement.bindLong(1, entity.oneRMId.toLong())
        statement.bindLong(2, entity.exerciseId.toLong())
        statement.bindDouble(3, entity.curr1RM)
        statement.bindDouble(4, entity.prev1RM)
        statement.bindDouble(5, entity.changePercent)
        statement.bindLong(6, entity.date)
      }
    }
  }

  public override suspend fun insert(oneRepMaxEntity: OneRepMaxEntity): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfOneRepMaxEntity.insert(_connection, oneRepMaxEntity)
  }

  public override suspend fun getChangePercentageForDate(exerciseId: Int, date: Long): Double? {
    val _sql: String = "SELECT change_percent FROM  one_rep_max WHERE exercise_id = ? AND date = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, exerciseId.toLong())
        _argIndex = 2
        _stmt.bindLong(_argIndex, date)
        val _result: Double?
        if (_stmt.step()) {
          if (_stmt.isNull(0)) {
            _result = null
          } else {
            _result = _stmt.getDouble(0)
          }
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getOneRepMaxForDate(date: Long): Flow<List<OneRepMaxEntity>> {
    val _sql: String = "SELECT * FROM one_rep_max WHERE date = ?"
    return createFlow(__db, false, arrayOf("one_rep_max")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, date)
        val _columnIndexOfOneRMId: Int = getColumnIndexOrThrow(_stmt, "oneRMId")
        val _columnIndexOfExerciseId: Int = getColumnIndexOrThrow(_stmt, "exercise_id")
        val _columnIndexOfCurr1RM: Int = getColumnIndexOrThrow(_stmt, "curr_1rm")
        val _columnIndexOfPrev1RM: Int = getColumnIndexOrThrow(_stmt, "prev_1rm")
        val _columnIndexOfChangePercent: Int = getColumnIndexOrThrow(_stmt, "change_percent")
        val _columnIndexOfDate: Int = getColumnIndexOrThrow(_stmt, "date")
        val _result: MutableList<OneRepMaxEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: OneRepMaxEntity
          val _tmpOneRMId: Int
          _tmpOneRMId = _stmt.getLong(_columnIndexOfOneRMId).toInt()
          val _tmpExerciseId: Int
          _tmpExerciseId = _stmt.getLong(_columnIndexOfExerciseId).toInt()
          val _tmpCurr1RM: Double
          _tmpCurr1RM = _stmt.getDouble(_columnIndexOfCurr1RM)
          val _tmpPrev1RM: Double
          _tmpPrev1RM = _stmt.getDouble(_columnIndexOfPrev1RM)
          val _tmpChangePercent: Double
          _tmpChangePercent = _stmt.getDouble(_columnIndexOfChangePercent)
          val _tmpDate: Long
          _tmpDate = _stmt.getLong(_columnIndexOfDate)
          _item = OneRepMaxEntity(_tmpOneRMId,_tmpExerciseId,_tmpCurr1RM,_tmpPrev1RM,_tmpChangePercent,_tmpDate)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getPrevious(exerciseId: Int, date: Long): OneRepMaxEntity? {
    val _sql: String = "SELECT * FROM one_rep_max WHERE exercise_id = ? AND date < ? ORDER BY date DESC LIMIT 1"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, exerciseId.toLong())
        _argIndex = 2
        _stmt.bindLong(_argIndex, date)
        val _columnIndexOfOneRMId: Int = getColumnIndexOrThrow(_stmt, "oneRMId")
        val _columnIndexOfExerciseId: Int = getColumnIndexOrThrow(_stmt, "exercise_id")
        val _columnIndexOfCurr1RM: Int = getColumnIndexOrThrow(_stmt, "curr_1rm")
        val _columnIndexOfPrev1RM: Int = getColumnIndexOrThrow(_stmt, "prev_1rm")
        val _columnIndexOfChangePercent: Int = getColumnIndexOrThrow(_stmt, "change_percent")
        val _columnIndexOfDate: Int = getColumnIndexOrThrow(_stmt, "date")
        val _result: OneRepMaxEntity?
        if (_stmt.step()) {
          val _tmpOneRMId: Int
          _tmpOneRMId = _stmt.getLong(_columnIndexOfOneRMId).toInt()
          val _tmpExerciseId: Int
          _tmpExerciseId = _stmt.getLong(_columnIndexOfExerciseId).toInt()
          val _tmpCurr1RM: Double
          _tmpCurr1RM = _stmt.getDouble(_columnIndexOfCurr1RM)
          val _tmpPrev1RM: Double
          _tmpPrev1RM = _stmt.getDouble(_columnIndexOfPrev1RM)
          val _tmpChangePercent: Double
          _tmpChangePercent = _stmt.getDouble(_columnIndexOfChangePercent)
          val _tmpDate: Long
          _tmpDate = _stmt.getLong(_columnIndexOfDate)
          _result = OneRepMaxEntity(_tmpOneRMId,_tmpExerciseId,_tmpCurr1RM,_tmpPrev1RM,_tmpChangePercent,_tmpDate)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getLatest(exerciseId: Int): OneRepMaxEntity? {
    val _sql: String = "SELECT * FROM one_rep_max WHERE exercise_id = ? ORDER BY date DESC LIMIT 1 "
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, exerciseId.toLong())
        val _columnIndexOfOneRMId: Int = getColumnIndexOrThrow(_stmt, "oneRMId")
        val _columnIndexOfExerciseId: Int = getColumnIndexOrThrow(_stmt, "exercise_id")
        val _columnIndexOfCurr1RM: Int = getColumnIndexOrThrow(_stmt, "curr_1rm")
        val _columnIndexOfPrev1RM: Int = getColumnIndexOrThrow(_stmt, "prev_1rm")
        val _columnIndexOfChangePercent: Int = getColumnIndexOrThrow(_stmt, "change_percent")
        val _columnIndexOfDate: Int = getColumnIndexOrThrow(_stmt, "date")
        val _result: OneRepMaxEntity?
        if (_stmt.step()) {
          val _tmpOneRMId: Int
          _tmpOneRMId = _stmt.getLong(_columnIndexOfOneRMId).toInt()
          val _tmpExerciseId: Int
          _tmpExerciseId = _stmt.getLong(_columnIndexOfExerciseId).toInt()
          val _tmpCurr1RM: Double
          _tmpCurr1RM = _stmt.getDouble(_columnIndexOfCurr1RM)
          val _tmpPrev1RM: Double
          _tmpPrev1RM = _stmt.getDouble(_columnIndexOfPrev1RM)
          val _tmpChangePercent: Double
          _tmpChangePercent = _stmt.getDouble(_columnIndexOfChangePercent)
          val _tmpDate: Long
          _tmpDate = _stmt.getLong(_columnIndexOfDate)
          _result = OneRepMaxEntity(_tmpOneRMId,_tmpExerciseId,_tmpCurr1RM,_tmpPrev1RM,_tmpChangePercent,_tmpDate)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteOneRepMax(exerciseId: Int, date: Long) {
    val _sql: String = "DELETE FROM one_rep_max WHERE exercise_id = ? AND date = ?  "
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

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
