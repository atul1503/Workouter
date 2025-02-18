package com.atul.workouter


import android.health.connect.datatypes.units.Percentage
import android.os.Bundle
import android.util.Log
import android.view.autofill.AutofillManager
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.Checkbox
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawStyle
import androidx.compose.ui.graphics.drawscope.clipPath
import java.util.Date

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        val viewmodel= viewModel()
        val db= DatabaseProvider.getDatabase(this)
        viewmodel.setAppDatabase(db)

        /*
        CoroutineScope(Dispatchers.IO).launch {
         db.routineDao().deleteAll()
         db.exerciseDao().deleteAll()
        }
         */

        setContent {
                Navigator(viewmodel)
        }
    }
}

@Composable
fun Navigator(vm: viewModel) {
    val value = vm.getNavigationString()
    val db= vm.getAppDatabase()
    when (value) {
        "home" -> WorkoutApp(vm)
        "create routine" -> CreateRoutine( vm)
        "see all routines" -> AllRoutineView(vm)
        "start routine" -> {
            RoutineStarter(vm = vm)
        }
        "start exercises" -> {
            StartExercisesFromRoutine(vm = vm)
        }
        "start current exercise" -> {
            StartCurrentExercise(vm = vm)
        }
        "get rest" -> {
            RestScreen(vm)
        }
        "delete all routines" -> {
            val scope = CoroutineScope(Dispatchers.IO)
            LaunchedEffect(key1 = Unit, block = {
                scope.launch {
                    db.routineDao().deleteAll()
                    db.exerciseDao().deleteAll()
                    vm.changeNavigationString("home")
                }
            })
        }
    }
}


@Composable
fun RestScreen(vm: viewModel) {
    val exercise=vm.getExercisesThatCanBeDoneToday()[vm.currentExerciseIndex.value]
    val rest=exercise.rest
    ExerciseTimer(vm = vm, time = rest.toInt())
    Text(modifier =Modifier.offset(50.dp,50.dp), text = "Relax take some rest")
    LaunchedEffect(key1 = Unit, block = {
        launch(Dispatchers.IO) coroutine@{
            Log.d("RestScreen", "This is the current exercise index: ${vm.currentExerciseIndex.value}, ${vm.getExercisesThatCanBeDoneToday().size}, ${vm.isOnRest.value}")
            if(vm.isOnRest.value){
                vm.isOnRest.value=false
                vm.currentExerciseIndex.value += 1
                vm.changeNavigationString("start exercises")
                return@coroutine
            }
            vm.isOnRest.value=true
            if (vm.getExercisesThatCanBeDoneToday().size-1 == vm.currentExerciseIndex.value) {
                vm.changeNavigationString("home")
                vm.setRoutineLastDoneDateAndLastDoneFrequency(vm.getCurrentRoutine(), exercise)
                vm.currentExerciseIndex.value=0
                return@coroutine
            }
            while (vm.currentExerciseTimerStatus.value != viewModel.TimerStatus.STOPPED) {
               //Log.d("RestScreen","Waiting for timer to stop")
                Thread.sleep(500)
            }
            vm.currentExerciseIndex.value += 1
            vm.changeNavigationString("start exercises")
        }
    })
}




    @Composable
    fun ExerciseTimer(vm: viewModel,time: Int?) {
        var curr = remember {
            mutableStateOf(0)
        };

        LaunchedEffect(key1 = Unit, block = {
            launch(Dispatchers.IO) {
                vm.currentExerciseTimerStatus.value = viewModel.TimerStatus.STARTED
                while (curr.value < time!!) {
                    curr.value += 1
                    Thread.sleep(1000)
                }
                vm.currentExerciseTimerStatus.value = viewModel.TimerStatus.STOPPED
                vm.changeNavigationString("get rest")
            }
        })

        Box() {
            Canvas(modifier = Modifier.size(200.dp)) {
                //Log.d("ExerciseTimer", "This is the sweep angle: ${360f * (1-(curr.value.toFloat() / time!!))}")
                drawArc(
                    color=Color.Cyan,
                    size = Size(200f, 200f),
                    startAngle = 90f,
                    sweepAngle = 360f * (1-(curr.value.toFloat() / time!!)),
                    useCenter = true,
                )
            }
            Text(modifier = Modifier.offset(95.dp,95.dp),text="${time!!-curr.value}")
        }

    }

    @Composable
    fun StartCurrentExercise(vm: viewModel) {
        val exercise = vm.getExercisesThatCanBeDoneToday()[vm.currentExerciseIndex.value]
        if (exercise.isTimed) {
            ExerciseTimer(vm,exercise.time)
            Text(modifier = Modifier.offset(50.dp,50.dp), text = "Exercise name: ${exercise.name}")
        } else {
            Column() {
                Text("Exercise Steps: ${exercise.steps}")
                Text("Exercise Sets: ${exercise.sets}")
                Text("Exercise Rest: ${exercise.rest}")
                Text("Exercise Reps: ${exercise.reps}")
                vm.isOnRest.value=false
                Button(onClick = {
                    vm.changeNavigationString("get rest")
                }) {
                    Text("Get little rest!")
                }
            }
        }
    }


    @Composable
    fun StartExercisesFromRoutine(vm: viewModel) {
        if(vm.currentExerciseIndex.value >= vm.getExercisesThatCanBeDoneToday().size){
            Log.d("RestScreen","Sending back to home")
            vm.changeNavigationString("home")
            vm.currentExerciseIndex.value=0
            return
        }
        val exercises = vm.getExercisesThatCanBeDoneToday()
        val currentExerciseIndex = vm.currentExerciseIndex
        val currentExercise = exercises[currentExerciseIndex.value]
        Column() {
            Text("Exercise name: ${currentExercise.name}")
            Text("Exercise Description: ${currentExercise.description}")
            Text("Exercise Steps: ${currentExercise.steps}")
            Text("Exercise Sets: ${currentExercise.sets}")
            Text("Exercise Rest: ${currentExercise.rest}")
            if (currentExercise.isTimed) {
                Text("Exercise Time: ${currentExercise.time}")
            } else {
                Text("Exercise Reps: ${currentExercise.reps}")
            }
            Button(onClick = {
                vm.changeNavigationString("start current exercise")
            }) {
                Text("Start exercise")
            }
        }
    }


    @Composable
    fun RoutineStarter(vm: viewModel) {
        val currentRoutine = vm.getCurrentRoutine()

        LaunchedEffect(key1 = currentRoutine.name, block = {
            // check what exercises can be done today
            // check if all higher priority exercises have been done or not
            launch(Dispatchers.IO) checker@{
                var exercises = vm.getCurrentRoutineExercises()
                var exercisesThatCanBeDoneToday: MutableList<Exercise> = mutableListOf()
                val routineLastDay = currentRoutine.lastDoneDate
                val routineLastFreq = currentRoutine.lastDoneFrequency
                exercises = exercises.sortedBy { it.frequency }
                Log.d("RoutineStarter", "This is the routine exercises: ${exercises}")
                if (routineLastDay == null) {
                    //filter out exercises which have frequency more than the lowest freuqnecy exercise
                    val lowestFrequency = exercises[0].frequency
                    exercisesThatCanBeDoneToday.addAll(exercises.filter { it.frequency == lowestFrequency })
                    vm.exercisesThatCanBeDoneToday.value = exercisesThatCanBeDoneToday
                    vm.changeNavigationString("start exercises")

                    return@checker
                }
                //remove all those exercises with last frequency less than the routine last frequency and also remove those exercises whose frequency if you add with last routine date it is after today
                Log.d("RoutineStarter", "Get current routine: ${currentRoutine}")
                exercisesThatCanBeDoneToday.addAll(exercises.filter { it.frequency >= routineLastFreq && it.frequency * 84_400_000L + routineLastDay.time <= System.currentTimeMillis() })
                Log.d("RoutineStarter", "These are the exercises that can be done today: ${exercisesThatCanBeDoneToday}")

                vm.changeExercisesThatCanBeDoneToday(exercisesThatCanBeDoneToday)
                vm.changeNavigationString("start exercises")
            }
        })
    }

    @Composable
    fun WorkoutApp(vm: viewModel) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            ClickableText(
                buildAnnotatedString { append("Create routine") },
                onClick = { vm.changeNavigationString("create routine") })
            ClickableText(
                buildAnnotatedString { append("View routines") },
                onClick = { vm.changeNavigationString("see all routines") })
            ClickableText(
                buildAnnotatedString { append("Delete all routines") },
                onClick = { vm.changeNavigationString("delete all routines") })
        }
    }

    @Composable
    fun ViewRoutines() {
        Column(modifier = Modifier.fillMaxSize()) {
            Text("Routines")

        }
    }


    @Composable
    fun CreateRoutine(vm: viewModel) {
        var name = remember { mutableStateOf("") }
        var description = remember { mutableStateOf("") }
        var exerciseCount = remember { mutableStateOf(1) }
        val scope = CoroutineScope(Dispatchers.IO)
        val db = vm.getAppDatabase()

        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            TextField(
                value = "${name.value}",
                onValueChange = { name.value = it },
                label = { Text("Enter routine name") })
            TextField(
                value = "${description.value}",
                onValueChange = { description.value = it },
                label = { Text("Enter description") })
            for (i in 1..exerciseCount.value) {
                ExerciseInput(vm)
            }
            Button(onClick = { exerciseCount.value += 1
                vm.exercisesGettingCreatedNow.value= vm.exercisesGettingCreatedNow.value.plus(Exercise())

            }) {
                Text("Add exercise")
            }

            Button(onClick = {
                scope.launch {
                    Log.d("exercise", "Creating routine")
                    val routine = Routine(name.value, description.value, Date(), 0)
                    db.routineDao().insertRoutine(routine)
                    val exercises=vm.exercisesGettingCreatedNow.value
                    Log.d("CreateRoutine","These are the exercises: ${exercises}")
                    for (ex in exercises) {
                        //print exercise
                        Log.d(
                            "exercise",
                            "name: ${ex.name} description: ${ex.description} ${ex.isTimed} ${ex.time} ${ex.reps} ${ex.sets} ${ex.rest} ${ex.steps}"
                        )
                        ex.routineName = routine.name
                        db.exerciseDao().insertExercise(ex)
                    }

                    vm.changeNavigationString("home")
                }


            }) {
                Text("Looks good?")
            }
        }

    }



    @Composable
    fun ExerciseInput(vm: viewModel) {
        val exercise = vm.exercisesGettingCreatedNow.value[vm.exercisesGettingCreatedNow.value.size - 1]
        var name = remember { mutableStateOf("") }
        var description = remember { mutableStateOf("") }
        var steps = remember { mutableStateOf("") }
        var isTimed = remember { mutableStateOf(false) }
        var time = remember { mutableStateOf(0) }
        var reps = remember { mutableStateOf(0) }
        var sets = remember { mutableStateOf(0) }
        var rest = remember { mutableStateOf(0f) }
        var frequency = remember { mutableStateOf(0) }



        Column() {
            TextField(
                value = "${name.value}",
                onValueChange = { name.value = it;exercise.name = it
                                Log.d("ExerciseInput","${exercise.name}, ${vm.exercisesGettingCreatedNow.value[vm.exercisesGettingCreatedNow.value.size - 1].name}")
                                },
                label = { Text("Enter exercise name") })
            TextField(
                value = "${description.value}",
                onValueChange = { description.value = it;exercise.description = it },
                label = { Text("Enter exercise description") })
            TextField(
                value = "${steps.value}",
                onValueChange = { steps.value = it;exercise.steps = it.split("\n") },
                label = { Text("Enter exercise steps") })
            TextField(
                value = "${sets.value}",
                onValueChange = {
                    if (it == "") {
                        return@TextField
                    }
                    sets.value = it.toInt();exercise.sets = it.toInt();

                },
                label = { Text("Enter number of sets") })
            TextField(
                value = "${rest.value}",
                onValueChange = {
                    if (it == "") {
                        return@TextField
                    }
                    rest.value = it.toFloat();exercise.rest = it.toFloat();

                },
                label = { Text("Enter rest between sets") })
            TextField(value = "${frequency.value}", onValueChange = {
                if (it == "") {
                    return@TextField
                }
                frequency.value = it.toInt();exercise.frequency = it.toInt()

            }, label = {
                Text("Enter exercise frequency in days")
            })
            Row {
                Text("Is exercise timed?")
                Checkbox(
                    checked = isTimed.value,
                    onCheckedChange = { isTimed.value = it;exercise.isTimed = it

                    })
            }

            if (isTimed.value == true) {
                TextField(
                    value = "${time.value}",
                    onValueChange = {
                        if (it == "") {
                            return@TextField
                        }
                        time.value = it.toInt();
                        exercise.time = it.toInt()
                    },
                    label = { Text("Enter time for exercise") })
            } else {
                TextField(
                    value = "${reps.value}",
                    onValueChange = {
                        if (it == "") {
                            return@TextField
                        }
                        ;reps.value = it.toInt();exercise.reps = it.toInt()
                    },
                    label = { Text("Enter number of reps") })
            }
        }
    }

    @Composable
    fun ExerciseViewForRoutineView(exercise: Exercise, vm: viewModel) {
        Column() {
            Text("Exercise name: ${exercise.name}")
            Text("Exercise Description: ${exercise.description}")
            Text("Exercise Steps: ${exercise.steps}")
            Text("Exercise Sets: ${exercise.sets}")
            Text("Exercise Rest: ${exercise.rest}")
            if (exercise.isTimed) {
                Text("Exercise Time: ${exercise.time}")
            } else {
                Text("Exercise Reps: ${exercise.reps}")
            }

        }
    }

    @Composable
    fun RoutineView(routine: Routine, vm: viewModel) {

        var exercises = remember { mutableStateListOf<Exercise>() }
        LaunchedEffect(key1 = routine.name, block = {
            launch(Dispatchers.IO) {
                vm.changeCurrentRoutine(routine)
                Log.d("exercise", Thread.currentThread().name.toString())
                exercises.addAll(vm.getCurrentRoutineExercises())
            }
        })

        Column() {
            Text("Routine name: ${routine.name}")
            Text("Description: ${routine.description}")
            for (ex in exercises) {
                ExerciseViewForRoutineView(ex, vm)
            }
            Button(onClick = {

                vm.changeCurrentRoutine(routine)
                vm.changeNavigationString("start routine")
                Log.d("exercise","${vm.getCurrentRoutine()}, ${vm.getNavigationString()}")
            }) {
                Text("Start routine")
            }
        }
    }


    @Composable
    fun AllRoutineView(vm: viewModel) {
        val db = vm.getAppDatabase()
        val scope = CoroutineScope(Dispatchers.IO)
        val routines = remember {
            mutableStateOf(mutableStateListOf<Routine>())
        }

        LaunchedEffect(key1 = Unit, block = {
            scope.launch {
                routines.value.addAll(db.routineDao().getAllRoutines())

            }
        })

        Log.d("routine", "All routine view recomposed")
        Column() {
            for (r in routines.value) {
                RoutineView(r, vm)
            }
        }
    }