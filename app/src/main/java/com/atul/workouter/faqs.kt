package com.atul.workouter

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun FaqPage(vm: viewModel) {

    BackHandler() {
        vm.changeNavigationString("home")
    }

    val screenwidth= LocalConfiguration.current.screenWidthDp
    Log.d("faq",(screenwidth*0.005*4).toString())

    Column(
        Modifier
            .verticalScroll(rememberScrollState())
            .background(color = MaterialTheme.colors.background),
        verticalArrangement = Arrangement.spacedBy((screenwidth*0.05).dp)
    , horizontalAlignment = Alignment.CenterHorizontally
    ){


            Box(modifier = Modifier
                .width(width = (screenwidth * 0.95).dp)
                .clip(
                    RoundedCornerShape(
                        topStart = (screenwidth * 0.0005).dp,
                        topEnd = (screenwidth * 0.0005).dp,
                        bottomEnd = (screenwidth * 0.0005).dp,
                        bottomStart = (screenwidth * 0.0005).dp
                    )
                )
                .border(
                    width = (screenwidth * 0.0005).dp,
                    color = MaterialTheme.colors.primary
                )
                .padding(all = (screenwidth * 0.06).dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy((screenwidth*0.07).dp)) {

                    Text(
                        "Q. What is this app for?",
                        color = MaterialTheme.colors.primary,
                        fontWeight = MaterialTheme.typography.h6.fontWeight,
                        style = TextStyle(fontSize = (screenwidth*0.06).sp)
                    )
                    Text(
                        text = """A. This app is for tracking your workouts and routines. This can be used either for working out or creating study routines or any routine of any kind.
                However, initially it was made keeping in mind to schedule workouts as you can see from the terms like sets,reps, etc. But sets and reps can be used for any kind of task.
                Let's say you have to study a subject everyday for 1 hr, then you create an exercise with rest period as 1 day and no. of sets as 1 and set the exercise as timed exercise with 1 hr as the time.
                So, every day it will remind you to study that subject for 1 hr. So, here set is how many times you want to study the subject for the current run of the routine and since you want to 
                numerify it using time and not a number of repetitions, you can set the exercise as timed.
                """.trimIndent(),
                        color = MaterialTheme.colors.secondary,
                        fontWeight = MaterialTheme.typography.h3.fontWeight,
                        style = TextStyle(fontSize = (screenwidth*0.04).sp)
                    )
                }

            }

        Box(modifier = Modifier
            .width(width = (screenwidth * 0.95).dp)
            .clip(
                RoundedCornerShape(
                    topStart = (screenwidth * 0.0005).dp,
                    topEnd = (screenwidth * 0.0005).dp,
                    bottomEnd = (screenwidth * 0.0005).dp,
                    bottomStart = (screenwidth * 0.0005).dp
                )
            )
            .border(
                width = (screenwidth * 0.0005).dp,
                color = MaterialTheme.colors.primary
            )
            .padding(all = (screenwidth * 0.05).dp)
        ) {

            Column(verticalArrangement = Arrangement.spacedBy((screenwidth*0.07).dp)) {
                Text(
                    "Q. How are routines and exercises related?",
                    color = MaterialTheme.colors.primary,
                    fontWeight = MaterialTheme.typography.h6.fontWeight,
                    style = TextStyle(fontSize = (screenwidth*0.06).sp)
                )
                Text(
                    text = """
                A. Routines are a collection of exercises. You can create a routine and add exercises to it. You have to add all exercises while creating a routine. You can edit the exercises but not the routine later, which means that exercises can't be added or removed once added to the routine.
                """.trimIndent(),
                    color = MaterialTheme.colors.secondary,
                    fontWeight = MaterialTheme.typography.h3.fontWeight,
                    style = TextStyle(fontSize = (screenwidth*0.04).sp)
                )
            }
        }


        Box(modifier = Modifier
            .width(width = (screenwidth * 0.95).dp)
            .clip(
                RoundedCornerShape(
                    topStart = (screenwidth * 0.0005).dp,
                    topEnd = (screenwidth * 0.0005).dp,
                    bottomEnd = (screenwidth * 0.0005).dp,
                    bottomStart = (screenwidth * 0.0005).dp
                )
            )
            .border(
                width = (screenwidth * 0.0005).dp,
                color = MaterialTheme.colors.primary
            )
            .padding(all = (screenwidth * 0.06).dp)
        ) {

            Column(verticalArrangement = Arrangement.spacedBy((screenwidth*0.07).dp)) {

                Text(
                    "Q. How to create a routine?",
                    color = MaterialTheme.colors.primary,
                    fontWeight = MaterialTheme.typography.h6.fontWeight,
                    style = TextStyle(fontSize = (screenwidth*0.06).sp)
                )
                Text(
                    text = """A. You can create a routine by clicking on the 'Create Routine' button in the home screen. You will be asked to enter the name of the routine and then you can add exercises to it.
                Each exercise has a:
                name : name of the exercise, should be unique throughout the app and across all routines.
                description : description of the exercise, just a few words about the exercise like, how to do it and what things to keep in mind. Optional.
                rest (between sets) : this is a integer time in seconds  to tell how much rest the app will allow you to take between two sets of the exercise. This is not the rest time between two exercises. This is the rest time between two sets of the different exercises.
                rest (between same categories) : integer, Is explained later.
                time: If you set the "is exercise timed?" checkbox, then the exercise will be timed and you will be asked to enter the time in seconds. This is the time for which you have to do the exercise. It is the responsibility of the app to start a timer for this time when doing this exercise. You don't have to manually create timer or count time.
                reps: If you do not set the "is exercise timed?" checkbox, then the exercise will be a normal exercise and you will be asked to enter the number of reps. This is the number of times you have to do the exercise. It is the responsibility of the user to count the number of reps. The app will not do it for you.
                
                Remember that routines have just a:
                name: name of the routine, should be unique throughout the app and across all routines.
                description: description of the routine, just a few words about the routine like, what does it accomplish. Optional
                """.trimIndent(),
                    color = MaterialTheme.colors.secondary,
                    fontWeight = MaterialTheme.typography.h3.fontWeight,
                    style = TextStyle(fontSize = (screenwidth*0.04).sp)
                )
            }
        }

        Box(modifier = Modifier
            .width(width = (screenwidth * 0.95).dp)
            .clip(
                RoundedCornerShape(
                    topStart = (screenwidth * 0.0005).dp,
                    topEnd = (screenwidth * 0.0005).dp,
                    bottomEnd = (screenwidth * 0.0005).dp,
                    bottomStart = (screenwidth * 0.0005).dp
                )
            )
            .border(
                width = (screenwidth * 0.0005).dp,
                color = MaterialTheme.colors.primary
            )
            .padding(all = (screenwidth * 0.06).dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy((screenwidth*0.07).dp)) {

                Text(
                    "Q. What is a category?",
                    color = MaterialTheme.colors.primary,
                    fontWeight = MaterialTheme.typography.h6.fontWeight,
                    style = TextStyle(fontSize = (screenwidth*0.06).sp)
                )
                Text(
                    text = """
            A. Exercises inside a routine are also grouped into different Category. When you start a routine, you will only be able to do exercises of a single category that you defined while creating a routine. This means that no two exercises with the different categories can be done togather. If some category is left, you can do it the next time you start the routine and only if the rest periods of the exercises in the other routine is complete.
            
            The need for category is to allow your body or mind to have variety in whatever routine you follow. In case of workouts, the variety becomes important because doing heavy pushups and other heavy tricep exercises togather might injure triceps since they both target the same muscle group. So what you would want to do here, is to put barbell push and pushups into different categories
            so that there is a rest gap between these two exercises.
            
        """.trimIndent(),
                    color = MaterialTheme.colors.secondary,
                    fontWeight = MaterialTheme.typography.h6.fontWeight,
                    style = TextStyle(fontSize = (screenwidth*0.04).sp)
                )
            }
        }

        Box(modifier = Modifier
            .width(width = (screenwidth * 0.95).dp)
            .clip(
                RoundedCornerShape(
                    topStart = (screenwidth * 0.0005).dp,
                    topEnd = (screenwidth * 0.0005).dp,
                    bottomEnd = (screenwidth * 0.0005).dp,
                    bottomStart = (screenwidth * 0.0005).dp
                )
            )
            .border(
                width = (screenwidth * 0.0005).dp,
                color = MaterialTheme.colors.primary
            )
            .padding(all = (screenwidth * 0.06).dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy((screenwidth*0.07).dp)) {


                Text(
                    "Q. How much rest in days to put for any exercise?",
                    color = MaterialTheme.colors.primary,
                    fontWeight = MaterialTheme.typography.h6.fontWeight,
                    style = TextStyle(fontSize = (screenwidth*0.06).sp)
                )
                Text(
                    """
            A. As a recommendation, keep the rest in days value for exercises in same category as same. This will ensure that whenever the routine allows you to do a category, it allows you to do all exercises in the same category, together. If you don't put same rest days for same category exercises, the next time you do the category, the more rest requiring exercise might not be scheduled with the others since its rest time is not completed.
            
            And as a general tip, put rest in days at least 2 for any workout exercises.
            For study routines, you have to decide for yourself how often do you want to study a subject or topic.
            
            But remember EXERCISES have rest in days NOT category or routines.
            """.trimIndent(),
                    color = MaterialTheme.colors.secondary,
                    fontWeight = MaterialTheme.typography.h6.fontWeight,
                    style = TextStyle(fontSize = (screenwidth*0.04).sp)
                )
            }
        }

        Box(modifier = Modifier
            .width(width = (screenwidth * 0.95).dp)
            .clip(
                RoundedCornerShape(
                    topStart = (screenwidth * 0.0005).dp,
                    topEnd = (screenwidth * 0.0005).dp,
                    bottomEnd = (screenwidth * 0.0005).dp,
                    bottomStart = (screenwidth * 0.0005).dp
                )
            )
            .border(
                width = (screenwidth * 0.0005).dp,
                color = MaterialTheme.colors.primary
            )
            .padding(all = (screenwidth * 0.06).dp)
        ) {

            Column(verticalArrangement = Arrangement.spacedBy((screenwidth*0.07).dp)) {
                Text(
                    "Q. Will putting different categories ensure that different category exercises cannot be done on same day?",
                    color = MaterialTheme.colors.primary,
                    fontWeight = MaterialTheme.typography.h6.fontWeight,
                    style = TextStyle(fontSize = (screenwidth*0.06).sp)
                )

                Text(
                    """
            A. Short answer. Yes.
            Long answer. It depends on how much rest you put for different categories. 
            
            Lets say you have A exercise put into category 1 and B exercise put into category 2. If you put rest in days for both as 1 day. It will mean that if you complete A today. Next day, you can start B, but after completing B, if you again try to start the routine, you will be able to start A, since A required rest of 1 day, which means, by today, its rest is complete.
            
            So, its very important, that you carefully decide proper rest in days for any category.
        """.trimIndent(),
                    color = MaterialTheme.colors.secondary,
                    fontWeight = MaterialTheme.typography.h3.fontWeight,
                    style = TextStyle(fontSize = (screenwidth*0.04).sp)
                )
            }
        }

        Box(modifier = Modifier
            .width(width = (screenwidth * 0.95).dp)
            .clip(
                RoundedCornerShape(
                    topStart = (screenwidth * 0.0005).dp,
                    topEnd = (screenwidth * 0.0005).dp,
                    bottomEnd = (screenwidth * 0.0005).dp,
                    bottomStart = (screenwidth * 0.0005).dp
                )
            )
            .border(
                width = (screenwidth * 0.0005).dp,
                color = MaterialTheme.colors.primary
            )
            .padding(all = (screenwidth * 0.06).dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy((screenwidth*0.07).dp)) {

                Text(
                    "Q. Why do categories have integer values?",
                    color = MaterialTheme.colors.primary,
                    fontWeight = MaterialTheme.typography.h6.fontWeight,
                    style = TextStyle(fontSize = (screenwidth*0.06).sp)
                )

                Text(
                    """
            A. Because they are exercises are executed in order of category. 
            
            As an example,Let's say, A,B,C exercises are put into category 1; and B,C,D exercises are put into category 2.
            So, next time if you start a routine, A,B,C exercises will be scheduled and only after completing these exercises, Category 2 exercises will be scheduled(depending on whether the rest period for category 2 exercises are complete or not).
            
            So, category allows routines to execute itself in batches.
        """.trimIndent(),
                    color = MaterialTheme.colors.secondary,
                    fontWeight = MaterialTheme.typography.h3.fontWeight,
                    style = TextStyle(fontSize = (screenwidth*0.04).sp)
                )
            }
        }

        Box(modifier = Modifier
            .width(width = (screenwidth * 0.95).dp)
            .clip(
                RoundedCornerShape(
                    topStart = (screenwidth * 0.0005).dp,
                    topEnd = (screenwidth * 0.0005).dp,
                    bottomEnd = (screenwidth * 0.0005).dp,
                    bottomStart = (screenwidth * 0.0005).dp
                )
            )
            .border(
                width = (screenwidth * 0.0005).dp,
                color = MaterialTheme.colors.primary
            )
            .padding(all = (screenwidth * 0.06).dp)
        ) {

            Column(verticalArrangement = Arrangement.spacedBy((screenwidth*0.07).dp)) {

                Text(
                    "Q.Difference between rest in days and rest between sets ",
                    color = MaterialTheme.colors.primary,
                    fontWeight = MaterialTheme.typography.h6.fontWeight,
                    style = TextStyle(fontSize = (screenwidth*0.06).sp)
                )
                Text(
                    """
            A. Both are rests, except that rest between sets is literally the period for which the rest timer in the app runs to allow you to rest yourself and be ready for the next exercise set.
             And rest in days, is the rest for the complete exercise, until its scheduled to be done next.
            """.trimIndent(),
                    color = MaterialTheme.colors.secondary,
                    fontWeight = MaterialTheme.typography.h3.fontWeight,
                    style = TextStyle(fontSize = (screenwidth*0.04).sp)
                )
            }

        }


    }

}