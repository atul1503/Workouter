package com.atul.workouter

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
    var EditRoutine= mutableStateOf(Routine())
    var EditExercises= mutableStateListOf<Exercise>()

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

    fun incrementSetDoneForExercise(exercise: Exercise){
        var copy=exercise.copy(setsDone = exercise.setsDone+1)
        copy.steps=exercise.steps.toList()
        exercisesThatCanBeDoneToday.value= exercisesThatCanBeDoneToday.value.map {
            if(it.name==exercise.name){
                copy
            }else{
                it
            }
        }
    }

    fun getThisExerciseFromETCBDT(name: String): Exercise{
        return exercisesThatCanBeDoneToday.value.filter { it.name==name }[0]
    }

    fun editExerciseByName(name: String,ex: Exercise){

       CoroutineScope(Dispatchers.IO).launch{
           db!!.exerciseDao().deleteThisExercise(name)
           db!!.exerciseDao().insertExercise(ex)
       }
    }

    fun getExercisesOfRoutine(routine: Routine): List<Exercise>{
        return db!!.routineDao().getRoutineWithExercises(routine.name)[0].exercise
    }

    fun getExerciseByName(name: String): Exercise{
        return db!!.exerciseDao().getExercise(name)
    }

    fun setSetsDoneForExercise(exercise: Exercise, setsDone: Int){
        var copy=exercise.copy(setsDone = setsDone)
        copy.steps=exercise.steps.toList()
        exercisesThatCanBeDoneToday.value= exercisesThatCanBeDoneToday.value.map {
            if(it.name==exercise.name){
                copy
            }else{
                it
            }
        }
    }


    fun getEditRoutineExercises(): List<Exercise>{
        return db!!.routineDao().getRoutineWithExercises(EditRoutine.value.name)[0].exercise
    }

    fun incrementExerciseIndex(){
        currentExerciseIndex.value+=1
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


