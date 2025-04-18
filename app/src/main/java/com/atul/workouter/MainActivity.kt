package com.atul.workouter


import android.app.Activity
import android.graphics.Paint.Align
import android.health.connect.datatypes.units.Percentage
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.WindowManager
import androidx.compose.ui.unit.sp
import android.view.autofill.AutofillManager
import androidx.activity.compose.BackHandler
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.work.WorkManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.Checkbox
import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Shapes
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.Typography
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkQuery
import com.atul.workouter.ui.Boxer
import kotlinx.coroutines.delay
import java.util.Date
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {


    private fun scheduleNotificationWorker(){

        val constraints= Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()

        val workerRequest= PeriodicWorkRequestBuilder<NotificationWorker>(
            15, TimeUnit.MINUTES,
            5, TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .addTag("RoutineNotificationWorkerTag")
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork("RoutineNotificationWorker",ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,workerRequest)
    }




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        scheduleNotificationWorker()

        val viewmodel= viewModel()
        val db= DatabaseProvider.getDatabase(this)
        viewmodel.setAppDatabase(db)

        if(viewmodel.ctx==null){
            viewmodel.setContext(this)
        }

        viewmodel.getTextToSpeech()


        setContent {

            val myColor=if(isSystemInDarkTheme()){
                darkColors(
                    primary = Color(0xFF4DB6AC),
                    secondary = Color(0xFFFFF176),
                    background = Color(0xFF1B1B1B),
                    onPrimary = Color.Black,
                )
            }
            else{
                lightColors(
                    primary = Color(0xFF00897B) ,
                    secondary = Color(0xFFFFD54F) ,
                    background = Color(0xFFF9FBE7) ,
                    onPrimary = Color.White,
                )
            }

            MaterialTheme(
                colors = myColor,
                typography = Typography(),
                shapes = Shapes(),
                content = { Navigator(viewmodel) }
            )
        }
    }
}


@Composable
fun Navigator(vm: viewModel) {
    Log.d("Navigator",vm.getNavigationString())
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
        "edit routine" -> {
            EditRoutine(vm = vm)
        }
        "how much time left?" -> {
            WaitDialog(vm = vm)
        }
        "get rest" -> {
            RestScreen(vm)
        }
    }
}



@Composable
fun WaitDialog(vm: viewModel) {

    val currentTime= remember {
        mutableStateOf(System.currentTimeMillis())
    }


    var leftTime=((vm.NearestNextRoutineExercise!!.lastDoneDate.time+(vm.NearestNextRoutineExercise!!.restTime*86_400_000L).toLong())-currentTime.value)/1000


    val seconds=leftTime % 60
    leftTime /= 60

    val minutes=leftTime % 60
    leftTime /= 60

    val hours=leftTime % 24
    leftTime /= 24

    val days=leftTime

    LaunchedEffect(key1 = Unit, block = {
        while (true) {
            delay(1000)
            currentTime.value = System.currentTimeMillis()
            Log.d("timer","is running")
        }
    })
    BackHandler() {
        vm.changeNavigationString("home")
    }

    Row(modifier = Modifier.fillMaxHeight(), verticalAlignment = Alignment.CenterVertically,) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            KeyValueRow(key = "Time left for routine: ", value = "$days days $hours hours, $minutes minutes and $seconds seconds")

            Button(onClick = {
                vm.changeNavigationString("home")
            }) {
                Text("Close")
            }
        }
    }
}


@Composable
fun KeyValueRow(key: String, value: Any) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp), horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(key, color = MaterialTheme.colors.onBackground)
        if (value is String) {
            Text(value, color = MaterialTheme.colors.onBackground)
        } else if (value is Iterable<*>) {
            Text(value.joinToString(", "), color = MaterialTheme.colors.onBackground)
        } else if (value is Int) {
            Text(value.toString(), color = MaterialTheme.colors.onBackground)
        } else if (value is Float) {
            Text(value.toString(), color = MaterialTheme.colors.onBackground)
        } else if (value is Boolean) {
            Text(value.toString(), color = MaterialTheme.colors.onBackground)
        } else if (value is Date) {
            Text(value.toString(), color = MaterialTheme.colors.onBackground)
        } else {
            Text("Unknown type", color = MaterialTheme.colors.onBackground)
        }
    }
}


@Composable
fun EditRoutine(vm: viewModel){
    val routine= vm.EditRoutine


    val name= routine.name
    var description= remember {
        mutableStateOf(routine.description)
    }
    var forceRun= remember {
        mutableStateOf(routine.forceRun)
    }
    var exercises=vm.EditExercises
    val db= vm.getAppDatabase()
    val scope= CoroutineScope(Dispatchers.IO)

    BackHandler() {
        vm.changeNavigationString("see all routines")
    }


    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
        TextField(modifier=Modifier.fillMaxWidth(),value = description.value, onValueChange = { description.value=it; routine.description=it }, label = { Text("Enter routine description") })
        Text("Exercises",color = MaterialTheme.colors.onBackground )
        for(ex in exercises){

            // key to identify exercise
            key(ex.name.value) {

                Column {
                    TextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = ex.name.value,
                        onValueChange = { ex.name.value = it },
                        label = {
                            Text(
                                "Enter exercise name",
                                color = MaterialTheme.colors.onBackground
                            )
                        })
                    TextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = ex.description.value,
                        onValueChange = { ex.description.value = it },
                        label = { Text("Enter exercise description") })
                    TextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = ex.steps.value.joinToString("\n"),
                        onValueChange = { ex.steps.value = it.split("\n") },
                        label = { Text("Enter exercise steps") })
                    TextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = ex.sets.value.toString(),
                        onValueChange = {
                            if (it == "") return@TextField; ex.sets.value = it.toInt()
                        },
                        label = { Text("Enter number of sets") })
                    TextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = ex.rest.value.toString(),
                        onValueChange = {
                            if (it == "") return@TextField; ex.rest.value = it.toInt()
                        },
                        label = { Text("Enter rest between sets") })
                    TextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = ex.restTime.value.toString(),
                        onValueChange = {
                            if (it == "") return@TextField; ex.restTime.value = it.toFloat();
                        },
                        label = {
                            Text(
                                "Enter rest in days. These many days will be skipped before this exercise is scheduled again. Make sure that exercises in same category have same value for this field. ",
                                color = MaterialTheme.colors.onBackground
                            )
                        })
                    TextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = if (ex.category.value != null) ex.category.value.toString() else "1",
                        onValueChange = {
                            if (it == "") return@TextField; ex.category.value = it.toInt()
                        },
                        label = {
                            Text(
                                "Enter exercise category. Exercises in different category will never be allowed in the same day.",
                                color = MaterialTheme.colors.onBackground
                            )
                        })
                    Row {
                        Text("Is exercise timed?", color = MaterialTheme.colors.onBackground)
                        Checkbox(
                            modifier = Modifier.fillMaxWidth(),
                            checked = ex.isTimed.value,
                            onCheckedChange = { ex!!.isTimed.value = it })
                    }

                    if (ex.isTimed.value) {
                        TextField(
                            modifier = Modifier.fillMaxWidth(),
                            value = ex.time.value.toString(),
                            onValueChange = {
                                if (it == "") {
                                    return@TextField
                                }
                                ex.time.value = it.toInt()
                            },
                            label = {
                                Text(
                                    "Enter time for exercise",
                                    color = MaterialTheme.colors.onBackground
                                )
                            })
                    } else {
                        TextField(
                            modifier = Modifier.fillMaxWidth(),
                            value = ex.reps.value.toString(),
                            onValueChange = {
                                if (it == "") {
                                    return@TextField
                                }

                                ex.reps.value = it.toInt()
                            },
                            label = {
                                Text(
                                    "Enter number of reps",
                                    color = MaterialTheme.colors.onBackground
                                )
                            })
                    }
                }
            }
        }
        Button(onClick = {
            scope.launch {
                for(ex in exercises){
                    db.exerciseDao().deleteThisExercise(ex.name.value)
                    db.exerciseDao().insertExercise(ex.toExercise())
                }
                vm.changeNavigationString("home")
            }
        }) {
            Text("Save routine",color = Color.White )
        }
    }
}

fun RestCoroutineWorker(vm:viewModel,exercise: State<Exercise>): Unit {
    vm.incrementSetDoneForExercise(exercise.value)
    Log.d("RestCoroutineWorker","This was the exercise: ${vm.getExercisesThatCanBeDoneToday()[vm.currentExerciseIndex.value]} ")
    if(vm.getThisExerciseFromETCBDT(exercise.value.name).setsDone>=exercise.value.sets){
        vm.saveRoutineAndExerciseLastDoneDate(exercise.value)
    }

    var DoneCount=0
    while(true){
        if (vm.currentExerciseIndex.value >= vm.exercisesThatCanBeDoneToday.value.size - 1) {
            vm.currentExerciseIndex.value = 0
        } else {
            vm.incrementExerciseIndex()
        }
        if(vm.exercisesThatCanBeDoneToday.value[vm.currentExerciseIndex.value].setsDone>=vm.exercisesThatCanBeDoneToday.value[vm.currentExerciseIndex.value].sets && DoneCount<vm.getExercisesThatCanBeDoneToday().size-1){
            DoneCount++
        }
        else{
            break
        }
    }


    Log.d("RestCoroutineWorker","This is the next exercise: ${vm.getExercisesThatCanBeDoneToday()[vm.currentExerciseIndex.value]} ")

    var isRoutineDone=true
    for(ex in vm.exercisesThatCanBeDoneToday.value){
        if(ex.setsDone<ex.sets){
            isRoutineDone=false
        }
    }
    if(isRoutineDone){
        vm.saveRoutineAndExerciseLastDoneDate(exercise.value)
        vm.currentExerciseIndex.value=0
        vm.isOnRest.value=false
        vm.changeNavigationString("home")
    }else {
        vm.changeNavigationString("start exercises")
    }
}



@Composable
fun RestScreen(vm: viewModel) {
    if(vm.currentExerciseIndex.value>=vm.getExercisesThatCanBeDoneToday().size){
        vm.changeNavigationString("home")
        Log.d("RestScreen","No exercise to be done")
        return
    }
    val exercise= rememberUpdatedState(newValue =vm.getExercisesThatCanBeDoneToday()[vm.currentExerciseIndex.value])
    val rest=exercise.value.rest
    vm.isOnRest.value=true

    ExerciseTimer(vm = vm, time = rest.toInt(), {
        RestCoroutineWorker(vm,exercise)
    })
    Box(modifier = Modifier.fillMaxWidth()) {
        Text(modifier = Modifier
            .align(Alignment.Center)
            .offset(0.dp, 200.dp), text = "Relax! Take some rest",color = MaterialTheme.colors.onBackground )
    }

}




    @Composable
    fun ExerciseTimer(vm: viewModel,time: Int?,callback: ()->Unit){
        var curr = remember {
            mutableStateOf(0f)
        };
        var animTargetState= remember {
            mutableStateOf(0f)
        }


        var animatedCurr= animateFloatAsState(targetValue = animTargetState.value, animationSpec = tween(time!!*1000, easing = LinearEasing))

        LaunchedEffect(key1 = Unit, block = {
            launch(Dispatchers.IO) {
                animTargetState.value=100f
                var saidSpeech=false
                //write log statements to know when this is happening
                Log.d("ExerciseTimer","The timer has started. This is the current exercise index: ${vm.currentExerciseIndex.value},Whether app is on rest stage: ${vm.isOnRest.value}")
                while (curr.value < time!!) {
                    curr.value += 1
                    Thread.sleep(1000)
                    if(!vm.getTextToSpeech().isSpeaking && !saidSpeech){
                        if(vm.isOnRest.value){
                            vm.getTextToSpeech().speak("take rest",TextToSpeech.QUEUE_FLUSH, null, null)
                            saidSpeech=true
                        }else{
                            vm.getTextToSpeech().speak("start ${vm.getExercisesThatCanBeDoneToday()[vm.currentExerciseIndex.value].name}",TextToSpeech.QUEUE_FLUSH, null, null)
                            saidSpeech=true
                        }
                    }
                    Log.d("ExerciseTimer","This is the time of timer: ${curr.value}")
                }
                if(vm.isOnRest.value){
                    vm.getTextToSpeech().speak("Rest is over.",TextToSpeech.QUEUE_FLUSH, null, null)
                }
                callback()

            }
        })

        Box(modifier = Modifier.fillMaxSize()) {
            val arcSize= LocalConfiguration.current.screenWidthDp
            Canvas(modifier = Modifier
                .size(arcSize.dp)
                .align(Alignment.Center)) {
                //Log.d("ExerciseTimer", "This is the sweep angle: ${360f * (1-(curr.value.toFloat() / time!!))}")
                drawArc(
                    color=Color.Cyan,
                    size = Size(arcSize.toFloat(), arcSize.toFloat()),
                    topLeft = Offset((size.width-arcSize)/2, (size.height-arcSize)/2),
                    startAngle = 90f,
                    sweepAngle = 360f * (1-(animatedCurr.value / 100f)),
                    useCenter = true,
                )
            }
            Text(modifier = Modifier.align(Alignment.Center),color = MaterialTheme.colors.onBackground ,text="${time!!-curr.value.toInt()}")
        }

    }

    @Composable
    fun StartCurrentExercise(vm: viewModel) {
        val exercise = vm.getExercisesThatCanBeDoneToday()[vm.currentExerciseIndex.value]
        if (exercise.isTimed) {
            vm.isOnRest.value=false
            ExerciseTimer(vm,exercise.time,{
                vm.changeNavigationString("get rest")
            })
        } else {
            Box(modifier=Modifier.fillMaxHeight(), contentAlignment = Alignment.Center) {
                Column() {
                    KeyValueRow(key = "Exercise Steps", value = exercise.steps)

                    if (!vm.getTextToSpeech().isSpeaking) {
                        vm.getTextToSpeech().speak(
                            "start ${vm.getExercisesThatCanBeDoneToday()[vm.currentExerciseIndex.value].name}",
                            TextToSpeech.QUEUE_FLUSH,
                            null,
                            null
                        )
                    }

                    KeyValueRow(key = "Rest after each set", value = exercise.rest)
                    KeyValueRow(key = "Exercise Reps", value = exercise.reps)
                    vm.isOnRest.value = false
                    Button(modifier = Modifier.fillMaxWidth(), onClick = {
                        vm.changeNavigationString("get rest")
                    }) {
                        Text("Get little rest!", color = Color.White)
                    }
                }
            }
        }
    }



    @Composable
    fun StartExercisesFromRoutine(vm: viewModel) {
        if(vm.currentExerciseIndex.value >= vm.getExercisesThatCanBeDoneToday().size){
            vm.currentExerciseIndex.value=0
            vm.changeNavigationString("home")
            return
        }
        (LocalContext.current as Activity).window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        val exercises = vm.getExercisesThatCanBeDoneToday()
        val currentExerciseIndex = vm.currentExerciseIndex
        val currentExercise = exercises[currentExerciseIndex.value]
        Box(modifier = Modifier.fillMaxHeight(), contentAlignment = Alignment.Center) {
            Column() {

                KeyValueRow(key = "Exercise name", value = currentExercise.name)
                KeyValueRow(key = "Exercise Description", value = currentExercise.description)
                KeyValueRow(key = "Exercise Steps", value = currentExercise.steps)
                KeyValueRow(
                    key = "Sets done",
                    value = "${currentExercise.setsDone} / ${currentExercise.sets}"
                )
                KeyValueRow(key = "Exercise Rest", value = currentExercise.rest)
                if (currentExercise.isTimed) {
                    KeyValueRow(key = "Exercise Time", value = currentExercise.time)
                } else {
                    KeyValueRow(key = "Exercise Reps", value = currentExercise.reps)
                }

                BackHandler() {
                    vm.changeNavigationString("see all routines")
                }
                Button(modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.End), onClick = {
                    vm.changeNavigationString("start current exercise")
                }) {
                    Text("Start exercise", color = Color.White)
                }
            }
        }

    }


    @Composable
    fun RoutineStarter(vm: viewModel) {
        val currentRoutine = vm.getCurrentRoutine()

        LaunchedEffect(currentRoutine.name, block = {
            // check what exercises can be done today
            // check if all higher priority exercises have been done or not
            launch(Dispatchers.IO) checker@{
                var exercises = vm.getCurrentRoutineExercises()
                var exercisesThatCanBeDoneToday: MutableList<Exercise> = mutableListOf()
                val lastCategory=currentRoutine.lastDoneCategory
                val restDurationUnit=86_400_000L // 86_400_000L milliseconds = 1 day
                exercises = exercises.sortedBy { it.category }
                Log.d("RoutineStarter", "This is the routine exercises: ${exercises}")
                if (lastCategory == null  ) {
                    //filter out exercises which have frequency more than the lowest freuqnecy exercise
                    val lowestCategory = exercises[0].category
                    exercisesThatCanBeDoneToday.addAll(exercises.filter { it.category == lowestCategory })
                    vm.exercisesThatCanBeDoneToday.value = exercisesThatCanBeDoneToday
                    vm.changeNavigationString("start exercises")

                    return@checker
                }


                //remove all those exercises with last frequency less than the routine last frequency and also remove those exercises whose frequency if you add with last routine date it is after today
                //Log.d("RoutineStarter", "Get current routine: ${currentRoutine}")
                val maxCategory=exercises.maxBy { it.category!! }.category
                exercisesThatCanBeDoneToday=(exercises.filter {
                    return@filter true
                }).toMutableList()
                var nextCategory: Int?=null
                if(maxCategory!!>lastCategory){
                    //find just higher category
                    nextCategory=null
                    for(ex in exercises){
                        if(ex.category!!>lastCategory){
                            nextCategory=ex.category
                            break
                        }
                    }
                    exercisesThatCanBeDoneToday= exercisesThatCanBeDoneToday.filter { it.category==nextCategory }.toMutableList()
                }
                else if(lastCategory==maxCategory){
                    val firstCategory=exercises[0].category
                    exercisesThatCanBeDoneToday= exercises.filter { it.category==firstCategory }.toMutableList()
                    nextCategory=firstCategory!!
                }
                else{
                    //find just higher category
                    for(ex in exercises){
                        if(ex.category!!>lastCategory){
                            nextCategory=ex.category
                            break
                        }
                    }
                    exercisesThatCanBeDoneToday= exercisesThatCanBeDoneToday.filter { it.category==nextCategory }.toMutableList()
                }
                if(currentRoutine.forceRun) {
                    vm.changeExercisesThatCanBeDoneToday(exercisesThatCanBeDoneToday)
                    vm.changeNavigationString("start exercises")
                    return@checker
                }

                exercisesThatCanBeDoneToday=exercisesThatCanBeDoneToday.filter { System.currentTimeMillis()-it.lastDoneDate.time >= it.restTime*restDurationUnit }.toMutableList()

                Log.d("RoutineStarter", "These are the exercises that can be done today: ${exercisesThatCanBeDoneToday}")
                Log.d("RoutineStarter",exercisesThatCanBeDoneToday.isEmpty().toString())

                if(exercisesThatCanBeDoneToday.isEmpty()){
                    vm.NearestNextRoutineExercise=exercises.filter { it.category==nextCategory }.sortedByDescending { it.lastDoneDate }[0]
                    vm.changeNavigationString("how much time left?")
                    return@checker
                }

                vm.changeExercisesThatCanBeDoneToday(exercisesThatCanBeDoneToday)
                vm.changeNavigationString("start exercises")
            }
        })
    }

    @Composable
    fun WorkoutApp(vm: viewModel) {

        if((LocalContext.current as Activity).window.attributes.flags.and(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)!=0){
            (LocalContext.current as Activity).window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }


            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(LocalConfiguration.current.screenHeightDp.dp/25,
                    Alignment.CenterVertically
                ),
            ) {
                Boxer {
                    ClickableText(modifier = Modifier.padding(LocalConfiguration.current.screenHeightDp.dp / 60),
                        text = buildAnnotatedString {
                            val text = "Create routine"
                            append(text)
                            addStyle(
                                style = SpanStyle(color = MaterialTheme.colors.onBackground),
                                start = 0,
                                end = text.length
                            )
                        },
                        style = TextStyle(fontSize = 30.sp),
                        onClick = { vm.changeNavigationString("create routine") })
                }
                Boxer {
                    ClickableText(modifier = Modifier.padding(LocalConfiguration.current.screenHeightDp.dp / 50),
                        text = buildAnnotatedString {
                            val text = "View routines"
                            append(text)
                            addStyle(
                                style = SpanStyle(color = MaterialTheme.colors.onBackground),
                                start = 0,
                                end = text.length
                            )
                        },
                        style = TextStyle(fontSize = 30.sp),
                        onClick = { vm.changeNavigationString("see all routines") })
                }

            }
    }

    @Composable
    fun ViewRoutines() {
        Column(modifier = Modifier.fillMaxSize()) {
            Text("Routines",color = MaterialTheme.colors.onBackground )

        }
    }


    @Composable
    fun CreateRoutine(vm: viewModel) {
        var name = remember { mutableStateOf("") }
        var description = remember { mutableStateOf("") }
        var exerciseCount = remember { mutableStateOf(1) }
        val scope = CoroutineScope(Dispatchers.IO)
        val db = vm.getAppDatabase()

        Column(modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())) {
            TextField( modifier=Modifier.fillMaxWidth(),
                value = "${name.value}",
                onValueChange = { name.value = it },
                label = { Text("Enter routine name",color = MaterialTheme.colors.onBackground ) })
            TextField(modifier=Modifier.fillMaxWidth(),
                value = "${description.value}",
                onValueChange = { description.value = it },
                label = { Text("Enter description",color = MaterialTheme.colors.onBackground ) })
            for (i in 1..exerciseCount.value) {
                ExerciseInput(vm)
            }
            Button(onClick = { exerciseCount.value += 1
                vm.exercisesGettingCreatedNow.value= vm.exercisesGettingCreatedNow.value.plus(Exercise())

            }) {
                Text("Add exercise",color = Color.White )
            }

            Button(onClick = {
                scope.launch {
                    Log.d("exercise", "Creating routine")
                    val routine = Routine(name.value, description.value, null, false)
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
                Text("Looks good?",color = Color.White )
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
        var category = remember { mutableStateOf(0) }
        var restInDays= remember { mutableStateOf(0f) }
        val showHints=remember {
            mutableStateOf(false)
        }



        Column() {
            TextField(modifier=Modifier.fillMaxWidth(),
                value = "${name.value}",
                onValueChange = { name.value = it;exercise.name = it
                                Log.d("ExerciseInput","${exercise.name}, ${vm.exercisesGettingCreatedNow.value[vm.exercisesGettingCreatedNow.value.size - 1].name}")
                                },
                label = { Text("Enter exercise name",color = MaterialTheme.colors.onBackground ) })
            TextField(modifier=Modifier.fillMaxWidth(),
                value = "${description.value}",
                onValueChange = { description.value = it;exercise.description = it },
                label = { Text("Enter exercise description",color = MaterialTheme.colors.onBackground ) })
            TextField(modifier=Modifier.fillMaxWidth(),
                value = "${steps.value}",
                onValueChange = { steps.value = it;exercise.steps = it.split("\n") },
                label = { Text("Enter exercise steps",color = MaterialTheme.colors.onBackground ) })
            TextField(modifier=Modifier.fillMaxWidth(),
                value = "${restInDays.value}",
                onValueChange = {if(it=="") return@TextField;  restInDays.value = it.toFloat();exercise.restTime = it.toFloat() },
                label = { Text("Enter rest in days. These many days will be skipped before this exercise is scheduled again.  Make sure that exercises in same category have same value for this field. ",color = MaterialTheme.colors.onBackground ) })
            TextField(modifier=Modifier.fillMaxWidth(),
                value = "${sets.value}",
                onValueChange = {
                    if (it == "") {
                        return@TextField
                    }
                    sets.value = it.toInt();exercise.sets = it.toInt();

                },
                label = { Text("Enter number of sets",color = MaterialTheme.colors.onBackground ) })
            TextField(modifier=Modifier.fillMaxWidth(),
                value = "${rest.value}",
                onValueChange = {
                    if (it == "") {
                        return@TextField
                    }
                    rest.value = it.toFloat();exercise.rest = it.toFloat();

                },
                label = { Text("Enter rest between sets",color = MaterialTheme.colors.onBackground ) })
            TextField(modifier=Modifier.fillMaxWidth(),value = "${category.value}", onValueChange = {
                if (it == "") {
                    return@TextField
                }
                category.value = it.toInt();exercise.category = it.toInt()

            }, label = {
                Text("Enter exercise category. Exercises in different category will never be allowed in the same day.",color = MaterialTheme.colors.onBackground )
            })
            Row {
                Text("Is exercise timed?",color = MaterialTheme.colors.onBackground )
                Checkbox(modifier=Modifier.fillMaxWidth(),
                    checked = isTimed.value,
                    onCheckedChange = { isTimed.value = it;exercise.isTimed = it

                    })
            }

            if (isTimed.value == true) {
                TextField(modifier=Modifier.fillMaxWidth(),
                    value = "${time.value}",
                    onValueChange = {
                        if (it == "") {
                            return@TextField
                        }
                        time.value = it.toInt();
                        exercise.time = it.toInt()
                    },
                    label = { Text("Enter time for exercise",color = MaterialTheme.colors.onBackground ) })
            } else {
                TextField(modifier=Modifier.fillMaxWidth(),
                    value = "${reps.value}",
                    onValueChange = {
                        if (it == "") {
                            return@TextField
                        }
                        ;reps.value = it.toInt();exercise.reps = it.toInt()
                    },
                    label = { Text("Enter number of reps",color = MaterialTheme.colors.onBackground ) })
            }
        }
    }

    @Composable
    fun ExerciseViewForRoutineView(exercise: Exercise, vm: viewModel) {

            Column() {
                KeyValueRow("Exercise", exercise.name)
                KeyValueRow("Exercise Description", exercise.description)
                KeyValueRow("Exercise Steps", exercise.steps)
                KeyValueRow("Exercise Sets", exercise.sets)
                KeyValueRow("Exercise Rest", exercise.rest)

                if (exercise.isTimed) {
                    KeyValueRow("Exercise Time", exercise.time)
                } else {
                    KeyValueRow("Exercise Reps", exercise.reps.toString())
                }
            }
    }

    @Composable
    fun RoutineView(routine: Routine, vm: viewModel) {
        var exercises = remember { mutableStateListOf<Exercise>() }
        val forceRun= remember {
            mutableStateOf(false)
        }

        val scope= CoroutineScope(Dispatchers.IO)

        BackHandler() {
            vm.changeNavigationString("home")
        }


        LaunchedEffect(key1 = routine.name, block = {
            launch(Dispatchers.IO) {
                vm.changeCurrentRoutine(routine)
                Log.d("exercise", Thread.currentThread().name.toString())
                exercises.addAll(vm.getCurrentRoutineExercises())
            }
        })

        Column(modifier=Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            KeyValueRow("Routine",routine.name)
            KeyValueRow("Description",routine.description)
            for (ex in exercises) {
                ExerciseViewForRoutineView(ex, vm)
            }
            Row(horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically){
                Button(onClick = {
                    vm.changeCurrentRoutine(routine)
                    vm.changeNavigationString("start routine")
                    Log.d("exercise", "${vm.getCurrentRoutine()}, ${vm.getNavigationString()}")
                }) {
                    Text("Start routine", color = Color.White)
                }
                Button(onClick = {
                    vm.EditRoutine = routine
                    scope.launch {
                        vm.EditExercises = vm.getExercisesOfRoutine(routine)
                    }
                    vm.changeNavigationString("edit routine")
                }) {
                    Text("Edit routine", color = Color.White)
                }
                val screenwidth = LocalConfiguration.current.screenWidthDp
                val screenheight = LocalConfiguration.current.screenHeightDp
                Box(
                    modifier = Modifier.width((screenwidth * 0.3f).dp)
                        .clip(RoundedCornerShape((screenwidth * 0.02).dp))
                        .background(color = MaterialTheme.colors.primary).pointerInput(Unit) {
                        detectTapGestures(onLongPress = {
                            scope.launch {
                                vm.deleteRoutine(routine.name)
                                vm.changeNavigationString("home")
                            }
                        })
                    }) {
                    Text(
                        text = "Delete routine",
                        color = Color.White,
                        modifier = Modifier.padding(screenwidth.dp / 65).width((screenwidth.dp / 3))
                    )
                }
            }

            Checkbox(checked = forceRun.value, onCheckedChange = { forceRun.value=it; routine.forceRun = it })
            Text(text = "Force start routine today?",color = MaterialTheme.colors.onBackground )
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

        BackHandler() {
            vm.changeNavigationString("home")
        }

        Log.d("routine", "All routine view recomposed")
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            for (r in routines.value) {
                key(r.name) {
                    RoutineView(r, vm)
                }
            }
        }
    }