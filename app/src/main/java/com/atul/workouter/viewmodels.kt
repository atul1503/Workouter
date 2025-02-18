package com.atul.workouter

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import java.util.Date

class viewModel: ViewModel() {
    var navigationString= mutableStateOf("home")
    var db: AppDatabase?= null;
    var currentRoutine=mutableStateOf(Routine().apply {
        lastDoneDate=null
    })
    var exercisesThatCanBeDoneToday=mutableStateOf(listOf<Exercise>())
    var currentExerciseIndex=mutableStateOf(0)
    var currentExerciseTimerStatus=mutableStateOf(TimerStatus.NOT_STARTED)
    var exercisesGettingCreatedNow=mutableStateOf(listOf<Exercise>(Exercise()))
    var isOnRest=mutableStateOf(false)

    enum class TimerStatus{
        STARTED,
        STOPPED,
        NOT_STARTED,
    }


    fun changeNavigationString(string: String){
        navigationString.value=string
    }

    fun getAppDatabase(): AppDatabase{
        return db!!
    }

    fun setAppDatabase(database: AppDatabase){
        db=database
    }

    fun getNavigationString(): String{
        return navigationString.value
    }

    fun changeCurrentRoutine(routine: Routine){
        currentRoutine.value=routine
    }

    fun getCurrentRoutine(): Routine{
        return currentRoutine.value
    }

    fun getCurrentRoutineExercises(): List<Exercise>{
        return db!!.routineDao().getRoutineWithExercises(currentRoutine.value.name)[0].exercise
    }

    fun changeExercisesThatCanBeDoneToday(exercises: List<Exercise>){
        exercisesThatCanBeDoneToday.value=exercises
    }

    fun getExercisesThatCanBeDoneToday(): List<Exercise>{
        return exercisesThatCanBeDoneToday.value
    }

    fun setRoutineLastDoneDateAndLastDoneFrequency(routine: Routine,exercise: Exercise){
        routine.lastDoneDate= Date()
        routine.lastDoneFrequency=exercise.frequency
        db!!.routineDao().updateRoutineLastDoneDateAndLastDoneFrequency(routine.name,
            routine.lastDoneDate!!, routine.lastDoneFrequency)
    }

}


