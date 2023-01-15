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
import com.example.gravityhelper.R
import com.github.mikephil.charting.charts.ScatterChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.ScatterData
import com.github.mikephil.charting.data.ScatterDataSet
import com.github.mikephil.charting.utils.EntryXComparator
import com.jjoe64.graphview.GraphView
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.MqttMessage
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var sensorManager: SensorManager
    private lateinit var accelerometer: Sensor
    private var sensorFrequency: Int = SensorManager.SENSOR_DELAY_NORMAL
    private lateinit var graph: GraphView
    private lateinit var data: ScatterData
    private lateinit var scatterChart: ScatterChart
    private lateinit var mqttClient: MqttAndroidClient

    private class SensorEventListenerImpl(private val data: ScatterData,private val accelerometer: Sensor,private val scatterChart: ScatterChart
                                          ,private val xTreshold:Double,private val yThreshold:Double, private val zThreshold:Double) : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {

            if (event?.sensor == accelerometer) {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]
                if(x > xTreshold && y > yThreshold)  {
                    Log.d("Accelerometer", "significant motion detected")
                    Log.d("Accelerometer", "X = "+x+" Y = "+y+" Z = "+z)
                    if(x>-9 && y>-9  && x<10 && y<10 )  {


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
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {return}
    }




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY)

        sensorFrequency = SensorManager.SENSOR_DELAY_UI

        val xThreshold = getIntent().getDoubleExtra("xThreshold",2.5)
        val yThreshold = getIntent().getDoubleExtra("yThreshold",2.5)
        val zThreshold = getIntent().getDoubleExtra("zThreshold",2.5)




        scatterChart = findViewById(R.id.scatterChart)
        data = ScatterData()
        val scatterDataSet = ScatterDataSet(null, "")
        scatterDataSet.setColor(Color.RED)
        scatterDataSet.setScatterShape(ScatterChart.ScatterShape.CIRCLE)
        scatterDataSet.setScatterShapeSize(12f)
        data.addDataSet(scatterDataSet)
        scatterChart.data = data
        scatterChart.invalidate()
        val yAxis = scatterChart.axisLeft
        yAxis.setAxisMaximum(10f)
        yAxis.setAxisMinimum(-10f)
        val xAxis = scatterChart.xAxis
        xAxis.setAxisMaximum(10f)
        xAxis.setAxisMinimum(-10f)

        val listener = SensorEventListenerImpl(data, accelerometer, scatterChart,xThreshold,yThreshold,zThreshold)

        sensorManager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_UI)

        mqttClient = MqttAndroidClient(this, "tcp://localhost:1883", "AndroidSensorClient")
        try {
            mqttClient.connect()
        } catch (e: MqttException) {
            e.printStackTrace()
        }



    }
    override fun onPause() {
        val xThreshold = getIntent().getDoubleExtra("xThreshold",2.5)
        val yThreshold = getIntent().getDoubleExtra("yThreshold",2.5)
        val zThreshold = getIntent().getDoubleExtra("zThreshold",2.5)
        val listener = SensorEventListenerImpl(data, accelerometer, scatterChart, xThreshold,yThreshold,zThreshold)
        sensorManager.unregisterListener(listener)
        super.onPause()
    }
    override fun onDestroy() {
        val xThreshold = getIntent().getDoubleExtra("xThreshold",2.5)
        val yThreshold = getIntent().getDoubleExtra("yThreshold",2.5)
        val zThreshold = getIntent().getDoubleExtra("zThreshold",2.5)
        val listener = SensorEventListenerImpl(data, accelerometer, scatterChart, xThreshold,yThreshold,zThreshold)
        sensorManager.unregisterListener(listener)
        super.onDestroy()
    }

}


