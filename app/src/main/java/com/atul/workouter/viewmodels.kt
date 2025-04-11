package com.atul.workouter

import android.content.Context
import android.speech.tts.TextToSpeech
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.Date

class viewModel: ViewModel() {
    var navigationString= mutableStateOf("home")
    var db: AppDatabase?= null;
    var currentRoutine=mutableStateOf(Routine())
    var exercisesThatCanBeDoneToday=mutableStateOf(listOf<Exercise>())
    var currentExerciseIndex=mutableStateOf(0)


    var exercisesGettingCreatedNow=mutableStateOf(listOf<Exercise>(Exercise()))
    var isOnRest=mutableStateOf(false)
    var EditRoutine= Routine()
    var EditExercises= listOf<MutableStateExercise>()

    var NearestNextRoutineExercise: Exercise?=null


    var tts : TextToSpeech?=null
    var ctx: Context?=null



    fun deleteRoutine(name: String){
        val scope=CoroutineScope(Dispatchers.IO)

        scope.launch {
            db!!.routineDao().deleteRoutine(name);
                scope.cancel()
        }


    }


    fun getTextToSpeech(): TextToSpeech {
        if(tts==null){
            tts=TextToSpeech(ctx, TextToSpeech.OnInitListener { status ->
                if (status != TextToSpeech.ERROR) {
                    tts!!.language = ctx!!.resources.configuration.locales[0]
                }
            })
            return tts!!
        }
        else{
            return tts!!
        }
    }

    fun setContext(ctx: Context){
        this.ctx=ctx
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

    fun getExercisesOfRoutine(routine: Routine): List<MutableStateExercise>{
        // get mutable exercises of routine
        val exs = db!!.routineDao().getRoutineWithExercises(routine.name)[0].exercises.map map@{
            val mut=MutableStateExercise(
                name=mutableStateOf(it.name),
                description = mutableStateOf(it.description),
                isTimed = mutableStateOf(it.isTimed),
                time = mutableStateOf(it.time),
                reps = mutableStateOf(it.reps),
                sets = mutableStateOf(it.sets),
                rest = mutableStateOf(it.rest.toInt()),
                steps = mutableStateOf(it.steps),
                restTime = mutableStateOf(it.restTime.toInt()),
                category = mutableStateOf(it.category!!),
                routineName = it.routineName,
                lastDoneDate = it.lastDoneDate,
                setsDone = it.setsDone
            )
            return@map mut

        }
        return exs
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
        return db!!.routineDao().getRoutineWithExercises(EditRoutine.name)[0].exercises
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
        return db!!.routineDao().getRoutineWithExercises(currentRoutine.value.name)[0].exercises
    }

    fun changeExercisesThatCanBeDoneToday(exercises: List<Exercise>){
        exercisesThatCanBeDoneToday.value=exercises
    }

    fun getExercisesThatCanBeDoneToday(): List<Exercise>{
        return exercisesThatCanBeDoneToday.value
    }

    fun saveRoutineAndExerciseLastDoneDate(exercise: Exercise){
        db!!.exerciseDao().updateExerciseLastDoneDate(exercise.name, Date())
        db!!.routineDao().updateLastDoneCategory(exercise.category!!,exercise.routineName)
    }


}


class MutableStateExercise(
    var name: MutableState<String>,
    var description: MutableState<String>,
    var isTimed: MutableState<Boolean>,
    var time: MutableState<Int>,
    var reps: MutableState<Int>,
    var sets: MutableState<Int>,
    var rest:  MutableState<Int>,
    var steps:  MutableState<List<String>>,
    var restTime: MutableState<Int>,
    var category: MutableState<Int>,
    var routineName: String,
    var lastDoneDate: Date,
    var setsDone: Int,
) {

    fun toExercise(): Exercise {
        return Exercise(
            name = name.value,
            description = description.value,
            isTimed = isTimed.value,
            time = time.value,
            reps = reps.value,
            sets = sets.value,
            rest = rest.value.toFloat(),
            steps = steps.value,
            restTime = restTime.value,
            routineName = routineName,
            lastDoneDate = lastDoneDate,
            category = category.value,
            setsDone = setsDone,

        )
    }

}


