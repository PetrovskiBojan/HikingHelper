package com.example.simplehelper

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import com.example.gravityhelper.SimulationActivity
import com.example.gravityhelper.databinding.ActivityParametersBinding


class ParametersActivity : AppCompatActivity() {
    private lateinit var binding: ActivityParametersBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityParametersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.sensorDataButton.setOnClickListener {
            val xAxis = binding.xAxisInput.text.toString().toDouble()
            val yAxis = binding.yAxisInput.text.toString().toDouble()

            val intent: Intent = Intent(this, MainActivity::class.java)
            intent.putExtra("xThreshold", xAxis)
            intent.putExtra("yThreshold", yAxis)

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