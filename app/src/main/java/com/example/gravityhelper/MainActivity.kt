package com.example.simplehelper

import android.content.Context
import android.content.Intent
import android.content.Intent.getIntent
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.gravityhelper.Motion
import com.example.gravityhelper.MqttHelper
import com.example.gravityhelper.R
import com.example.gravityhelper.databinding.ActivityMainBinding
import com.github.mikephil.charting.charts.ScatterChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.ScatterData
import com.github.mikephil.charting.data.ScatterDataSet
import com.github.mikephil.charting.utils.EntryXComparator
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity(),SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private lateinit var accelerometer: Sensor
    private var sensorFrequency: Int = SensorManager.SENSOR_DELAY_NORMAL
    private lateinit var data: ScatterData
    private lateinit var scatterChart: ScatterChart
    private lateinit var mqttClient: MqttAndroidClient
    private lateinit var database : DatabaseReference
    private lateinit var mqttHelper: MqttHelper
    private lateinit var binding : ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = FirebaseDatabase.getInstance().getReference("Motions")
        mqttHelper = MqttHelper(applicationContext)
        startMqtt()

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY)

        sensorFrequency = SensorManager.SENSOR_DELAY_UI


        scatterChart = findViewById(R.id.scatterChart)
        data = ScatterData()
        val scatterDataSet = ScatterDataSet(null, "")
        scatterDataSet.setColor(Color.RED)
        scatterDataSet.setScatterShape(ScatterChart.ScatterShape.CIRCLE)
        scatterDataSet.setScatterShapeSize(22f)
        data.addDataSet(scatterDataSet)
        scatterChart.data = data
        scatterChart.invalidate()
        val yAxis = scatterChart.axisLeft
        yAxis.setAxisMaximum(10f)
        yAxis.setAxisMinimum(-10f)
        val xAxis = scatterChart.xAxis
        xAxis.setAxisMaximum(10f)
        xAxis.setAxisMinimum(-10f)

        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
    }

    private fun startMqtt() {
        mqttHelper.mqttClient.setCallback(object : MqttCallback {
            override fun messageArrived(topic: String?, message: MqttMessage?) {
                Log.d(MqttHelper.TAG, "Receive message: ${message.toString()} from topic: $topic")
            }

            override fun connectionLost(cause: Throwable?) {
                Log.d(MqttHelper.TAG, "Connection lost ${cause.toString()}")
            }

            override fun deliveryComplete(token: IMqttDeliveryToken?) {

            }
        })
    }
    override fun onPause() {
        sensorManager.unregisterListener(this)
        super.onPause()
    }
    override fun onDestroy() {
        sensorManager.unregisterListener(this)
        super.onDestroy()
    }

    override fun onSensorChanged(event: SensorEvent?) {
        val xThreshold = getIntent().getDoubleExtra("xThreshold",2.5)
        val yThreshold = getIntent().getDoubleExtra("yThreshold",2.5)
        val zThreshold = getIntent().getDoubleExtra("zThreshold",2.5)
        if (event?.sensor == accelerometer) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]
            if(x > xThreshold && y > yThreshold)  {
                Log.d("Accelerometer", "significant motion detected")
                Log.d("Accelerometer", "X = "+x+" Y = "+y+" Z = "+z)
                if(x>-15 && y>-15  && x<15 && y<15 )  {

                    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    val currentDateTime = dateFormat.format(Date())
                    val payload = "Significant motion detected: X= "+x+" Y = "+y+" Z = "+z+"-- time : "+currentDateTime
                    mqttHelper.publish(MqttHelper.SUBSCRIPTION_TOPIC, payload)

                    val motion = Motion(x,y,currentDateTime)
                    database.child(motion.time).setValue(motion)



                    data.addEntry(Entry(x, y), 0)
                    val sortedEntries = TreeMap<Float, Entry>()

                    val scatterDataSet = data.getDataSetByIndex(0) as ScatterDataSet
                    for (i in 0 until scatterDataSet.entryCount) {
                        val entry = scatterDataSet.getEntryForIndex(i)
                        sortedEntries[entry.x] = entry
                    }
                    val sortedDataSet = ScatterDataSet(sortedEntries.values.toList(), scatterDataSet.label)

                    data.addDataSet(sortedDataSet)
                    data.notifyDataChanged()
                    scatterChart.notifyDataSetChanged()
                    scatterChart.invalidate()
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
       return
    }

}


