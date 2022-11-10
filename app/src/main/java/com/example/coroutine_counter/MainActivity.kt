package com.example.coroutine_counter

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

    private var TAG: String = "KOTLIN CO-ROUTINES"
    lateinit var counterText : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        counterText = findViewById(R.id.count)
    }


    fun updateCount(view: View){
        Log.d(TAG, "${Thread.currentThread().name}")
        counterText.text = "${counterText.text.toString().toInt() + 1}"
    }

    /**
     * This routine is to mimic the api call which might end up as a long running task and freeze the execution of the app
     */
    fun longRunningTask(){
        Log.d(TAG, "${Thread.currentThread().name}")
        for(i in 1..10000000000L){}
    }

    //solving this problem using threads in kotlin
    fun doAction(view: View){
        thread(start = true){
            longRunningTask()
        }
    }

}