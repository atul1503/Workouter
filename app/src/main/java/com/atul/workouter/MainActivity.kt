package com.atul.workouter


import android.graphics.Paint.Align
import android.health.connect.datatypes.units.Percentage
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.compose.ui.unit.sp
import android.view.autofill.AutofillManager
import androidx.activity.compose.BackHandler
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
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
import androidx.compose.ui.layout.AlignmentLine
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import java.util.Date

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


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
                    primary = Color.Blue,
                    secondary = Color.Red,
                    background = Color.White
                )
            }
            else{
                lightColors(
                    primary = Color.Blue,
                    secondary = Color.Red,
                    background = Color.Black
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
                            if (it == "") return@TextField; ex.restTime.value = it.toInt();
                        },
                        label = {
                            Text(
                                "Enter rest in days. These many days will be skipped before this exercise is scheduled again.",
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
    if(vm.getThisExerciseFromETCBDT(exercise.value.name).sets>=exercise.value.setsDone){
        vm.saveRoutineAndExerciseLastDoneDate(exercise.value)
    }
    if(vm.currentExerciseIndex.value>=vm.exercisesThatCanBeDoneToday.value.size-1){
        vm.currentExerciseIndex.value=0
    }else {
        vm.incrementExerciseIndex()
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
            Column() {
                Text("Exercise Steps:",color = MaterialTheme.colors.onBackground )
                for (step in exercise.steps) {
                    Text(step,color = MaterialTheme.colors.onBackground )
                }

                if(!vm.getTextToSpeech().isSpeaking){
                    vm.getTextToSpeech().speak("start ${vm.getExercisesThatCanBeDoneToday()[vm.currentExerciseIndex.value].name}",TextToSpeech.QUEUE_FLUSH, null, null)
                }

                Text("Rest after each set: ${exercise.rest} seconds",color = MaterialTheme.colors.onBackground )
                Text("Exercise Reps: ${exercise.reps}",color = MaterialTheme.colors.onBackground )
                vm.isOnRest.value=false
                Button(modifier = Modifier.fillMaxWidth(), onClick = {
                    vm.changeNavigationString("get rest")
                }) {
                    Text("Get little rest!",color = Color.White )
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
        val exercises = vm.getExercisesThatCanBeDoneToday()
        val currentExerciseIndex = vm.currentExerciseIndex
        val currentExercise = exercises[currentExerciseIndex.value]
        Box(contentAlignment = Alignment.Center) {
            Column() {
                Text(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    text = "Exercise name: ${currentExercise.name}",
                    color = MaterialTheme.colors.onBackground
                )
                Text(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    text = "Exercise Description: ${currentExercise.description}",
                    color = MaterialTheme.colors.onBackground
                )
                Text(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    text = "Exercise steps:",
                    color = MaterialTheme.colors.onBackground
                )
                for (ex in currentExercise.steps) {
                    Text(
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        text = ex,
                        color = MaterialTheme.colors.onBackground
                    )
                }
                Text(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    text = "Sets done: ${currentExercise.setsDone}/${currentExercise.sets}",
                    color = MaterialTheme.colors.onBackground
                )
                Text(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    text = "Exercise Rest: ${currentExercise.rest}",
                    color = MaterialTheme.colors.onBackground
                )
                if (currentExercise.isTimed) {
                    Text(
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        text = "Exercise Time: ${currentExercise.time}",
                        color = MaterialTheme.colors.onBackground
                    )
                } else {
                    Text(
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        text = "Exercise Reps: ${currentExercise.reps}",
                        color = MaterialTheme.colors.onBackground
                    )
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
                if (lastCategory == null || currentRoutine.forceRun ) {
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
                    if(it.category==lastCategory){
                        return@filter false
                    }
                    return@filter true
                }).toMutableList()
                if(maxCategory!!>lastCategory){
                    //find just higher category
                    var nextCategory: Int?=null
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
                }
                else{
                    //find just higher category
                    var nextCategory: Int?=null
                    for(ex in exercises){
                        if(ex.category!!>lastCategory){
                            nextCategory=ex.category
                            break
                        }
                    }
                    exercisesThatCanBeDoneToday= exercisesThatCanBeDoneToday.filter { it.category==nextCategory }.toMutableList()
                }

                exercisesThatCanBeDoneToday=exercisesThatCanBeDoneToday.filter { System.currentTimeMillis()-it.lastDoneDate.time >= it.restTime*restDurationUnit }.toMutableList()

                Log.d("RoutineStarter", "These are the exercises that can be done today: ${exercisesThatCanBeDoneToday}")

                vm.changeExercisesThatCanBeDoneToday(exercisesThatCanBeDoneToday)
                vm.changeNavigationString("start exercises")
            }
        })
    }

    @Composable
    fun WorkoutApp(vm: viewModel) {
        Column(
            modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center
        ) {
            ClickableText(modifier = Modifier.padding(20.dp),
                text=buildAnnotatedString {
                    val text="Create routine"
                    append(text)
                    addStyle(
                        style = SpanStyle(color = MaterialTheme.colors.onBackground),
                        start = 0,
                        end = text.length
                    )
                                          },
                style = TextStyle(fontSize = 30.sp),
                onClick = { vm.changeNavigationString("create routine") })
            ClickableText(modifier = Modifier.padding(20.dp),
                text=buildAnnotatedString {
                    val text="View routines"
                    append(text)
                    addStyle(
                        style = SpanStyle(color = MaterialTheme.colors.onBackground),
                        start = 0,
                        end = text.length
                    )
                                          },
                style = TextStyle(fontSize = 30.sp),
                onClick = { vm.changeNavigationString("see all routines") })
            ClickableText(modifier = Modifier.padding(20.dp),
                text=buildAnnotatedString {
                    val text="Delete routines"
                    append(text)
                    addStyle(
                        style = SpanStyle(color = MaterialTheme.colors.onBackground),
                        start = 0,
                        end = text.length
                    )
                                          },
                style = TextStyle(fontSize = 30.sp),
                onClick = { vm.changeNavigationString("delete all routines") })
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
        var restInDays= remember { mutableStateOf(0) }
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
                onValueChange = {if(it=="") return@TextField;  restInDays.value = it.toInt();exercise.restTime = it.toInt() },
                label = { Text("Enter rest in days. These many days will be skipped before this exercise is scheduled again.",color = MaterialTheme.colors.onBackground ) })
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
            Text("Exercise name: ${exercise.name}",color = MaterialTheme.colors.onBackground )
            Text("Exercise Description: ${exercise.description}",color = MaterialTheme.colors.onBackground )
            Text("Exercise Steps: ${exercise.steps}",color = MaterialTheme.colors.onBackground )
            Text("Exercise Sets: ${exercise.sets}",color = MaterialTheme.colors.onBackground )
            Text("Exercise Rest: ${exercise.rest}",color = MaterialTheme.colors.onBackground )
            if (exercise.isTimed) {
                Text("Exercise Time: ${exercise.time}",color = MaterialTheme.colors.onBackground )
            } else {
                Text("Exercise Reps: ${exercise.reps}",color = MaterialTheme.colors.onBackground )
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

        Column {
            Text("Routine name: ${routine.name}",color = MaterialTheme.colors.onBackground )
            Text("Description: ${routine.description}",color = MaterialTheme.colors.onBackground )
            for (ex in exercises) {
                ExerciseViewForRoutineView(ex, vm)
            }
            Button(onClick = {

                vm.changeCurrentRoutine(routine)
                vm.changeNavigationString("start routine")
                Log.d("exercise","${vm.getCurrentRoutine()}, ${vm.getNavigationString()}")
            }) {
                Text("Start routine",color = Color.White )
            }
            Button(onClick = {
                vm.EditRoutine=routine
                scope.launch {
                    vm.EditExercises=vm.getExercisesOfRoutine(routine)
                }
                vm.changeNavigationString("edit routine")
            }) {
                Text("Edit routine",color = Color.White )
            }
            Checkbox(checked = forceRun.value, onCheckedChange = { forceRun.value=it; routine.forceRun = it })
            Text(text = "Force routine?",color = MaterialTheme.colors.onBackground )
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