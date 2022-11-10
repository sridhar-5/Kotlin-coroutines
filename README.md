# Kotlin-coroutines

### Basic Concepts before co-routines:

- program - set of instructions ( When we try to start or instantiate a program a process gets created)
- Process (Actual instance of the program) which consists of the following:
    - processID
    - state
    - Memory
    - Handles for Networking, File system
    - Thread (Thread of execution)

Sequential Execution:

![Screenshot 2022-11-10 at 10.06.13 PM.png](Kotlin-coroutines%209290eea78b0b43119a251a7bb85c6044/Screenshot_2022-11-10_at_10.06.13_PM.png)

Now what if we encounter a situation where the instruction 2 (Marked in red) is taking a long time to execute: 

There are two ways to handle this :

- wait for it to complete execution and proceed to executing the other instructions. (old school)
- create an other thread with in the same process and execute the instruction-2 in parallel to other instructions without waiting. (Most systems currently use this)

![Screenshot 2022-11-10 at 10.10.07 PM.png](Kotlin-coroutines%209290eea78b0b43119a251a7bb85c6044/Screenshot_2022-11-10_at_10.10.07_PM.png)

consider this image, Thread-1 is in execution and encounters a blocking code which should wait for a response from an API to continue execution. Now consider Thread-2 which is reading files from a file system. till the point both the blocking code completes their respective I/O operations the threads are in wait state. so it is clear that our CPU is not being used efficiently. so now the idea is can we re-use the thread when it is in wait state to do something else ? This is the base question that gave raise to co-routines.

![Screenshot 2022-11-10 at 10.34.20 PM.png](Kotlin-coroutines%209290eea78b0b43119a251a7bb85c6044/Screenshot_2022-11-10_at_10.34.20_PM.png)

### COROUTINES

![Screenshot 2022-11-10 at 10.43.55 PM.png](Kotlin-coroutines%209290eea78b0b43119a251a7bb85c6044/Screenshot_2022-11-10_at_10.43.55_PM.png)

The main difference in between a coroutine and a thread is that coroutines can execute different tasks on the same thread. How is that ? 

Refer the picture above, coroutines are built on top of threads, so they’re really just a smart of managing or using threads. Now consider there is a process that is being executed and it encounters a blocking code (right-top image). The thread when is waiting for response from an API (first green portion - image1) will be used to execute some other tasks. when the API response is available there are two situation that can occur:

1. The thread that started has completed execution of other task that it was executing while waiting for the API response. 
2. The thread that started has not completed the execution of the task it took while waiting for API response.

**Case-1:** 

This is pretty straight-forward case where in when the response becomes available it simply continues executing the remaining task on the same thread that was executing other tasks while waiting for the response. 

**Case-2** : (What is the thread that was waiting and executing other tasks get occupied with executing the other tasks it took while waiting for API response ? )

This is where coroutines really happen to be useful if the thread which took other tasks while waiting for the response from API gets occupied with the other tasks then we can’t just snatch it back from it’s execution flow. so the routine when the response is available requests an other thread from the thread pool and continues execution of the rest of the task that was left.

How are co-routines useful in Android: 

The image shows how an application is executed in android behind the scenes. pretty much all the logic and handlers are executed on the main thread. 

![Screenshot 2022-11-10 at 11.43.54 PM.png](Kotlin-coroutines%209290eea78b0b43119a251a7bb85c6044/Screenshot_2022-11-10_at_11.43.54_PM.png)

How does main thread execute all these tasks ?

The answer to this question is very similar to an event driven architecture. Android has something called as looper in place of event loop. 

![Screenshot 2022-11-11 at 12.17.05 AM.png](Kotlin-coroutines%209290eea78b0b43119a251a7bb85c6044/Screenshot_2022-11-11_at_12.17.05_AM.png)

As tasks gets added to the message queue, looper sequentially shifts the tasks on to the main thread where the execution takes place but there certainly is a problem with this what if a few tasks take long time (API calls can be unresponsive at times). In this case the app freezes becomes unresponsive. so there is no way the user can do something else with the app. This will eventually lead to bad user-experience. 

There exists a few ways to solve this:

1. Using Threads
2. Using Co-routines

Using threads: 

```kotlin

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
```

when a long execution task is encountered then a new thread is created and the this time taking task keeps running on the new thread keeping the main thread free for other tasks which keeps app responsive. The primary goal is achieved here. But still there is a major problem with this kind of approach. we cannot create a lot of threads as thread consumes a lot of memory and there is only a limited number of threads we can create on a machine, and once the no of requests reaches the no of threads that can be created at once there is good chance that the app may still go unresponsive.

Using co-routines:

co-routines are just like threads (can be called light weight threads) but not threads. They work on top of threads. we need two things to implement coroutines

1. coroutine scope (defines a boundary for a coroutines) suppose we have an activity component and it has some coroutines in it the moment the activity component gets destroyed all the coroutines that comes with it also gets destroyed this is the advantage of defining the scope for coroutines. 
2. coroutine context (on what threads the coroutine is going to operate)