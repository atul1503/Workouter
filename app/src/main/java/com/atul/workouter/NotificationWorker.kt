package com.atul.workouter


import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class NotificationWorker(private val context: Context,params: WorkerParameters): CoroutineWorker(context,params){

    companion object {
        private const val CHANNEL_ID = "workout_notification_channel"
        private const val NOTIFICATION_GROUP = "workout_notifications"
    }

    private fun sendNotification(title: String, message: String, routineName: String){
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Create notification channel if needed
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Workout Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Workout Notifications"
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Create a unique notification ID based on routine name
        val notificationId = routineName.hashCode()

        // Check if notification already exists
        val activeNotifications = notificationManager.activeNotifications
        val notificationExists = activeNotifications.any { it.id == notificationId }

        // If notification already exists, don't create a new one
        if (notificationExists) {
            return
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setGroup(NOTIFICATION_GROUP)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(notificationId, notification)
    }

    private fun getNextCategory(routine: Routine,exercises: List<Exercise>,lastCategory: Int): Int {
        val firstCategory= exercises.sortedBy { it.category }[0].category
        var nextCategory=lastCategory
        for(exercise in exercises){
            if(exercise.category!!>nextCategory){
                nextCategory=exercise.category!!
                break
            }
            else if(exercises.indexOf(exercise)==exercises.size-1){
                nextCategory=firstCategory!!
                break
            }
        }
        return nextCategory
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val db = DatabaseProvider.getDatabase(context)
            val routineDao = db.routineDao()
            val routines = routineDao.getAllRoutines()

            for (routine in routines) {
                val lastCategory = routine.lastDoneCategory
                if(lastCategory==null) {
                    sendNotification(
                        "Workout Reminder",
                        "${routine.name} routine is ready to be done!",
                        routine.name
                    )
                    continue
                }
                var exercises = routineDao.getRoutineWithExercises(routine.name)[0].exercises
                val nextCategory = getNextCategory(routine, exercises, lastCategory!!)
                exercises = exercises.filter { it.category == nextCategory }
                    .sortedByDescending { it.lastDoneDate }
                if (exercises.isEmpty()) {
                    continue
                }
                val latestExercise = exercises[0]
                val nextReadyTime = latestExercise.lastDoneDate.time+(latestExercise.restTime*86_400_000L).toLong()
                val timeLeft = nextReadyTime-System.currentTimeMillis()
                if(timeLeft<=0) {
                    sendNotification(
                        "Workout Reminder",
                        "${routine.name} routine is ready to be done!",
                        routine.name
                    )
                }
            }

            return@withContext Result.success()
        }
        catch (e: Exception) {
            return@withContext Result.failure()
        }
    }
}


