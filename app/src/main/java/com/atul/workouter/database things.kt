package com.atul.workouter

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Ignore
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Transaction
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import java.util.Date


class ExerciseListConverter {
    private val gson: Gson = Gson()

    @TypeConverter
    public fun fromExerciseList(exercises: List<Exercise>): String {
        return gson.toJson(exercises)
    }

    @TypeConverter
    public fun toExerciseList(data: String): List<Exercise> {
        return gson.fromJson(data, Array<Exercise>::class.java).toList()
    }

    @TypeConverter
    public fun fromStringList(strings: List<String>): String {
        return gson.toJson(strings)
    }

    @TypeConverter
    public fun toStringList(data: String): List<String> {
        return gson.fromJson(data, Array<String>::class.java).toList()
    }

    @TypeConverter
    public fun toDate(data: String): Date {
        return gson.fromJson(data, Date::class.java)
    }

    @TypeConverter
    public fun fromDate(date: Date): String {
        return gson.toJson(date)
    }

}


@Entity
data class Routine(
    @PrimaryKey
    var name: String="",
    var description: String="",
    var lastDoneDate: Date?=null,
    var lastDoneFrequency: Int=0,
    @Ignore var forceRun: Boolean=false
)



@Entity(foreignKeys = [ForeignKey(entity = Routine::class, parentColumns = ["name"], childColumns = ["routineName"], onDelete = ForeignKey.CASCADE)])
data class Exercise(
    @PrimaryKey
    var name: String="",
    var description: String="",
    var isTimed: Boolean=false,
    var time: Int=0,
    var reps: Int=0,
    var sets: Int=0,
    var rest: Float=30f,
    var steps: List<String> = listOf(""),
    var frequency: Int=0,
    var routineName: String="",
    @Ignore var setsDone: Int=0,
    )

data class ExerciseWithRoutine(
    @Relation(
        parentColumn = "name",
        entityColumn = "routineName"
    )
    val exercise: List<Exercise>,
    @Embedded
    val routine: Routine)

@Dao
interface ExerciseDao {

        @Insert
        abstract fun insertExercise(exercise: Exercise)

        @Query("SELECT * FROM Exercise WHERE name= :name")
        abstract fun getExercise(name: String): Exercise

        @Query("SELECT * FROM Exercise")
        abstract fun getAllExercises(): List<Exercise>

        //delete all
        @Query("DELETE FROM Exercise")
        abstract fun deleteAll()
}

@Dao
interface  RoutineDao {

    @Insert
    abstract fun insertRoutine(routine: Routine)

    @Query("SELECT * FROM Routine WHERE name= :name")
    abstract fun getRoutine(name: String): Routine

    @Query("SELECT * FROM Routine")
    abstract fun getAllRoutines(): List<Routine>

    //delete all
    @Query("DELETE FROM Routine")
    abstract fun deleteAll()

    @Transaction
    @Query("SELECT * FROM Routine WHERE name= :name")
    abstract fun getRoutineWithExercises(name: String): List<ExerciseWithRoutine>

    @Query("UPDATE Routine SET lastDoneDate= :date, lastDoneFrequency= :frequency WHERE name= :name")
    abstract fun updateRoutineLastDoneDateAndLastDoneFrequency(name: String, date: Date, frequency: Int)


}


@Database(entities = [Routine::class, Exercise::class], version = 7)
@TypeConverters(ExerciseListConverter::class)
abstract class AppDatabase: RoomDatabase() {
    abstract fun routineDao(): RoutineDao

    abstract fun exerciseDao(): ExerciseDao
}


object DatabaseProvider {
    private var instance: AppDatabase? = null
    fun getDatabase(context: Context): AppDatabase {
        return instance ?: synchronized(context) {
            val db=Room.databaseBuilder(context, AppDatabase::class.java, "workouter").fallbackToDestructiveMigration().build()
            instance =db
            db
        }
    }
}