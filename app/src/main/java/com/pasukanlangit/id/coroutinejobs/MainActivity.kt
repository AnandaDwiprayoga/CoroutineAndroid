package com.pasukanlangit.id.coroutinejobs

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ProgressBar
import android.widget.Toast
import com.pasukanlangit.id.coroutinejobs.databinding.ActivityMainBinding
import kotlinx.coroutines.*
import java.util.concurrent.CancellationException

class MainActivity : AppCompatActivity() {

    private val PROGRESS_MAX = 100
    private val PROGRESS_START = 0
    private val JOB_TIME = 4000

    private lateinit var binding: ActivityMainBinding
    private lateinit var job  : CompletableJob


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.jobButton.setOnClickListener {
            if(!::job.isInitialized){
                initJob()
            }
            binding.jobProgressBar.startJobOrCancel(job)
        }
    }

    fun initJob(){
        binding.jobButton.text = "Start Job #1"
        updateJobCompleteTextView("")
        job = Job()
        job.invokeOnCompletion {
            it?.message.let { msg ->
                var message = msg
                if(message.isNullOrEmpty()){
                    message = "Unkown cancellation error."
                }
                showToast(message)
            }
        }

        binding.jobProgressBar.max = PROGRESS_MAX
        binding.jobProgressBar.progress = PROGRESS_START
    }

    private fun ProgressBar.startJobOrCancel(job: Job){
        if(this.progress > 0){
            resetJob()
        }else{
            binding.jobButton.text = "Cancel job #1"
            CoroutineScope(Dispatchers.IO + job).launch {
                for(i in PROGRESS_START..PROGRESS_MAX){
                    delay((JOB_TIME/PROGRESS_MAX).toLong())
                    this@startJobOrCancel.progress = i
                }
                updateJobCompleteTextView("Job is complete")
            }
        }
    }

    private fun updateJobCompleteTextView(text: String){
        GlobalScope.launch(Dispatchers.Main) {
            binding.jobCompleteText.text = text
        }
    }
    private fun resetJob() {
        if(job.isActive || job.isCancelled){
            job.cancel(CancellationException("Resetting job"))
        }
        initJob()
    }



    private fun showToast(text: String){
        GlobalScope.launch(Dispatchers.Main){
            Toast.makeText(this@MainActivity, text, Toast.LENGTH_SHORT).show()
        }
    }
}