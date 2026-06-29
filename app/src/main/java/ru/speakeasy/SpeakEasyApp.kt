package ru.speakeasy

import android.app.Application
import androidx.room.Room
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SpeakEasyApp : Application() {
    lateinit var database: AppDatabase
        private  set

    override fun onCreate() {
        super.onCreate()

        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "hello_database"
        ).build()

        CoroutineScope(Dispatchers.IO).launch {
            database.greetingDao().insertGreeting(
                GreetingEntity(id = 1, message = "Hello Android from Room")
            )
        }
    }
}