package com.example.heavyLifts.`data`.db

import androidx.room.InvalidationTracker
import androidx.room.RoomOpenDelegate
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.room.util.TableInfo
import androidx.room.util.TableInfo.Companion.read
import androidx.room.util.dropFtsSyncTriggers
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import com.example.heavyLifts.`data`.dao.ExerciseDao
import com.example.heavyLifts.`data`.dao.ExerciseDao_Impl
import com.example.heavyLifts.`data`.dao.OneRepMaxEntityDao
import com.example.heavyLifts.`data`.dao.OneRepMaxEntityDao_Impl
import com.example.heavyLifts.`data`.dao.WorkoutEntryEntityDao
import com.example.heavyLifts.`data`.dao.WorkoutEntryEntityDao_Impl
import javax.`annotation`.processing.Generated
import kotlin.Lazy
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.MutableList
import kotlin.collections.MutableMap
import kotlin.collections.MutableSet
import kotlin.collections.Set
import kotlin.collections.mutableListOf
import kotlin.collections.mutableMapOf
import kotlin.collections.mutableSetOf
import kotlin.reflect.KClass

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class ExerciseDatabase_Impl : ExerciseDatabase() {
  private val _exerciseDao: Lazy<ExerciseDao> = lazy {
    ExerciseDao_Impl(this)
  }

  private val _workoutEntryEntityDao: Lazy<WorkoutEntryEntityDao> = lazy {
    WorkoutEntryEntityDao_Impl(this)
  }

  private val _oneRepMaxEntityDao: Lazy<OneRepMaxEntityDao> = lazy {
    OneRepMaxEntityDao_Impl(this)
  }

  protected override fun createOpenDelegate(): RoomOpenDelegate {
    val _openDelegate: RoomOpenDelegate = object : RoomOpenDelegate(4, "7e82d3e40a19dfb52505eb5d2607fcd7", "e0225fadb1f624a69b8768acd1481c23") {
      public override fun createAllTables(connection: SQLiteConnection) {
        connection.execSQL("CREATE TABLE IF NOT EXISTS `exercises` (`exercise_id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `exercise_name` TEXT NOT NULL, `category` TEXT NOT NULL)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `workout_entry` (`entryId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `exercise_id` INTEGER NOT NULL, `weight` REAL NOT NULL, `reps` INTEGER NOT NULL, `date` INTEGER NOT NULL, `sets` INTEGER NOT NULL, FOREIGN KEY(`exercise_id`) REFERENCES `exercises`(`exercise_id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_workout_entry_exercise_id` ON `workout_entry` (`exercise_id`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `one_rep_max` (`oneRMId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `exercise_id` INTEGER NOT NULL, `curr_1rm` REAL NOT NULL, `prev_1rm` REAL NOT NULL, `change_percent` REAL NOT NULL, `date` INTEGER NOT NULL, FOREIGN KEY(`exercise_id`) REFERENCES `exercises`(`exercise_id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_one_rep_max_exercise_id` ON `one_rep_max` (`exercise_id`)")
        connection.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_one_rep_max_exercise_id_date` ON `one_rep_max` (`exercise_id`, `date`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)")
        connection.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '7e82d3e40a19dfb52505eb5d2607fcd7')")
      }

      public override fun dropAllTables(connection: SQLiteConnection) {
        connection.execSQL("DROP TABLE IF EXISTS `exercises`")
        connection.execSQL("DROP TABLE IF EXISTS `workout_entry`")
        connection.execSQL("DROP TABLE IF EXISTS `one_rep_max`")
      }

      public override fun onCreate(connection: SQLiteConnection) {
      }

      public override fun onOpen(connection: SQLiteConnection) {
        connection.execSQL("PRAGMA foreign_keys = ON")
        internalInitInvalidationTracker(connection)
      }

      public override fun onPreMigrate(connection: SQLiteConnection) {
        dropFtsSyncTriggers(connection)
      }

      public override fun onPostMigrate(connection: SQLiteConnection) {
      }

      public override fun onValidateSchema(connection: SQLiteConnection): RoomOpenDelegate.ValidationResult {
        val _columnsExercises: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsExercises.put("exercise_id", TableInfo.Column("exercise_id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsExercises.put("exercise_name", TableInfo.Column("exercise_name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsExercises.put("category", TableInfo.Column("category", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysExercises: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesExercises: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoExercises: TableInfo = TableInfo("exercises", _columnsExercises, _foreignKeysExercises, _indicesExercises)
        val _existingExercises: TableInfo = read(connection, "exercises")
        if (!_infoExercises.equals(_existingExercises)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |exercises(com.example.heavyLifts.data.entity.ExerciseEntity).
              | Expected:
              |""".trimMargin() + _infoExercises + """
              |
              | Found:
              |""".trimMargin() + _existingExercises)
        }
        val _columnsWorkoutEntry: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsWorkoutEntry.put("entryId", TableInfo.Column("entryId", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsWorkoutEntry.put("exercise_id", TableInfo.Column("exercise_id", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsWorkoutEntry.put("weight", TableInfo.Column("weight", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsWorkoutEntry.put("reps", TableInfo.Column("reps", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsWorkoutEntry.put("date", TableInfo.Column("date", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsWorkoutEntry.put("sets", TableInfo.Column("sets", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysWorkoutEntry: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        _foreignKeysWorkoutEntry.add(TableInfo.ForeignKey("exercises", "CASCADE", "NO ACTION", listOf("exercise_id"), listOf("exercise_id")))
        val _indicesWorkoutEntry: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesWorkoutEntry.add(TableInfo.Index("index_workout_entry_exercise_id", false, listOf("exercise_id"), listOf("ASC")))
        val _infoWorkoutEntry: TableInfo = TableInfo("workout_entry", _columnsWorkoutEntry, _foreignKeysWorkoutEntry, _indicesWorkoutEntry)
        val _existingWorkoutEntry: TableInfo = read(connection, "workout_entry")
        if (!_infoWorkoutEntry.equals(_existingWorkoutEntry)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |workout_entry(com.example.heavyLifts.data.entity.WorkoutEntryEntity).
              | Expected:
              |""".trimMargin() + _infoWorkoutEntry + """
              |
              | Found:
              |""".trimMargin() + _existingWorkoutEntry)
        }
        val _columnsOneRepMax: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsOneRepMax.put("oneRMId", TableInfo.Column("oneRMId", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsOneRepMax.put("exercise_id", TableInfo.Column("exercise_id", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsOneRepMax.put("curr_1rm", TableInfo.Column("curr_1rm", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsOneRepMax.put("prev_1rm", TableInfo.Column("prev_1rm", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsOneRepMax.put("change_percent", TableInfo.Column("change_percent", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsOneRepMax.put("date", TableInfo.Column("date", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysOneRepMax: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        _foreignKeysOneRepMax.add(TableInfo.ForeignKey("exercises", "CASCADE", "NO ACTION", listOf("exercise_id"), listOf("exercise_id")))
        val _indicesOneRepMax: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesOneRepMax.add(TableInfo.Index("index_one_rep_max_exercise_id", false, listOf("exercise_id"), listOf("ASC")))
        _indicesOneRepMax.add(TableInfo.Index("index_one_rep_max_exercise_id_date", true, listOf("exercise_id", "date"), listOf("ASC", "ASC")))
        val _infoOneRepMax: TableInfo = TableInfo("one_rep_max", _columnsOneRepMax, _foreignKeysOneRepMax, _indicesOneRepMax)
        val _existingOneRepMax: TableInfo = read(connection, "one_rep_max")
        if (!_infoOneRepMax.equals(_existingOneRepMax)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |one_rep_max(com.example.heavyLifts.data.entity.OneRepMaxEntity).
              | Expected:
              |""".trimMargin() + _infoOneRepMax + """
              |
              | Found:
              |""".trimMargin() + _existingOneRepMax)
        }
        return RoomOpenDelegate.ValidationResult(true, null)
      }
    }
    return _openDelegate
  }

  protected override fun createInvalidationTracker(): InvalidationTracker {
    val _shadowTablesMap: MutableMap<String, String> = mutableMapOf()
    val _viewTables: MutableMap<String, Set<String>> = mutableMapOf()
    return InvalidationTracker(this, _shadowTablesMap, _viewTables, "exercises", "workout_entry", "one_rep_max")
  }

  public override fun clearAllTables() {
    super.performClear(true, "exercises", "workout_entry", "one_rep_max")
  }

  protected override fun getRequiredTypeConverterClasses(): Map<KClass<*>, List<KClass<*>>> {
    val _typeConvertersMap: MutableMap<KClass<*>, List<KClass<*>>> = mutableMapOf()
    _typeConvertersMap.put(ExerciseDao::class, ExerciseDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(WorkoutEntryEntityDao::class, WorkoutEntryEntityDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(OneRepMaxEntityDao::class, OneRepMaxEntityDao_Impl.getRequiredConverters())
    return _typeConvertersMap
  }

  public override fun getRequiredAutoMigrationSpecClasses(): Set<KClass<out AutoMigrationSpec>> {
    val _autoMigrationSpecsSet: MutableSet<KClass<out AutoMigrationSpec>> = mutableSetOf()
    return _autoMigrationSpecsSet
  }

  public override fun createAutoMigrations(autoMigrationSpecs: Map<KClass<out AutoMigrationSpec>, AutoMigrationSpec>): List<Migration> {
    val _autoMigrations: MutableList<Migration> = mutableListOf()
    return _autoMigrations
  }

  public override fun exerciseDao(): ExerciseDao = _exerciseDao.value

  public override fun workoutEntryEntityDao(): WorkoutEntryEntityDao = _workoutEntryEntityDao.value

  public override fun oneRepMaxEntityDao(): OneRepMaxEntityDao = _oneRepMaxEntityDao.value
}
