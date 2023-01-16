package com.example.simplehelper

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import com.example.gravityhelper.Motion
import com.example.gravityhelper.SimulationActivity
import com.example.gravityhelper.databinding.ActivityParametersBinding
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*


class ParametersActivity : AppCompatActivity() {
    private lateinit var binding: ActivityParametersBinding
    private lateinit var database : DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityParametersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = FirebaseDatabase.getInstance().getReference("Motions")

        binding.sensorDataButton.setOnClickListener {
            val xAxis = binding.xAxisInput.text.toString().toDouble()
            val yAxis = binding.yAxisInput.text.toString().toDouble()

            val intent: Intent = Intent(this, MainActivity::class.java)
            intent.putExtra("xThreshold", xAxis)
            intent.putExtra("yThreshold", yAxis)

            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val currentDateTime = dateFormat.format(Date())

            val motion = Motion(xAxis.toFloat(),yAxis.toFloat(),currentDateTime)
            database.child(motion.time).setValue(motion)

            startActivity(intent)
        }
        binding.simulateDataButton.setOnClickListener {
            val xAxis = binding.xAxisInput.text.toString().toDouble()
            val yAxis = binding.yAxisInput.text.toString().toDouble()

            val intent: Intent = Intent(this, SimulationActivity::class.java)
            intent.putExtra("xThreshold", xAxis)
            intent.putExtra("yThreshold", yAxis)
            startActivity(intent)
        }
    }
}