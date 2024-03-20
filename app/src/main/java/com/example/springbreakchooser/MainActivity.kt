package com.example.springbreakchooser

import ShakeDetector
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.springbreakchooser.R.*
import java.util.Objects
import kotlin.math.sqrt

private lateinit var sensorManager: SensorManager
private var accelerometer: Sensor? = null
private lateinit var shakeDetector: ShakeDetector

private const val TAG: String = "MainActivity"

class MainActivity : AppCompatActivity() {
    private lateinit var operationSpinner: Spinner

    private var record: Boolean = false
    private lateinit var speechRecognizer: SpeechRecognizer

    private var selectedLanguageCode: String = ""

    private var sensorManager: SensorManager? = null
    private var acceleration = 0f
    private var currentAcceleration = 0f
    private var lastAcceleration = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout.activity_main)

        operationSpinner = findViewById(id.operationSpinner)

        val operations = arrayOf("English", "French", "Spanish", "Mandarin")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, operations)
        operationSpinner.adapter = adapter

        operationSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedLanguageCode = when (parent.getItemAtPosition(position).toString()) {
                    "English" -> "en"
                    "French" -> "fr"
                    "Spanish" -> "es"
                    "Mandarin" -> "zh"
                    else -> null.toString()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>){}
        }


        Log.d(TAG, "after spinner$selectedLanguageCode")

        val recordButton: Button = findViewById(id.button)

        if (packageManager.hasSystemFeature(PackageManager.FEATURE_MICROPHONE) &&
            ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.RECORD_AUDIO), 1)
        }

        setupListener()

        recordButton.setOnClickListener {
            if (selectedLanguageCode == null){
                Toast.makeText(
                    this@MainActivity,
                    "You haven't chosen any languages",
                    Toast.LENGTH_SHORT
                ).show()
            }else {
                if (!record) {
                    record()
                    record = true
                    recordButton.text = "Stop"
                } else {
                    speechRecognizer.stopListening()
                    record = false
                    recordButton.text = "Record"
                }
            }
        }

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        Objects.requireNonNull(sensorManager)!!
            .registerListener(sensorListener, sensorManager!!
                .getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL)

        acceleration = 10f
        currentAcceleration = SensorManager.GRAVITY_EARTH
        lastAcceleration = SensorManager.GRAVITY_EARTH


//        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
//        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
//        shakeDetector = ShakeDetector {
//            // This block will be executed when a shake is detected
//            openMapLocation()
//        }
    }

    private val sensorListener: SensorEventListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {

            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]
            lastAcceleration = currentAcceleration


            currentAcceleration = sqrt((x * x + y * y + z * z).toDouble()).toFloat()
            val delta: Float = currentAcceleration - lastAcceleration
            acceleration = acceleration * 0.9f + delta

            if (acceleration > 11) {
                Log.d(TAG, selectedLanguageCode)
                val gmmIntentUri = when (selectedLanguageCode) {
                    "en" -> Uri.parse("geo:51.5007,-0.1245?z=16")
                    "fr" -> Uri.parse("geo:48.8584,2.2945?z=16")
                    "es" -> Uri.parse("geo:41.40369,2.17433?z=16")
                    "zh" -> Uri.parse("geo:39.916344,116.397155?z=16")
                    else -> (null)
                }

                if (gmmIntentUri == null){
                    Toast.makeText(
                        this@MainActivity,
                        "You haven't chosen any languages",
                        Toast.LENGTH_SHORT
                    ).show()
                }else{
                    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                    mapIntent.setPackage("com.google.android.apps.maps")

                    val greetingMessage = when (selectedLanguageCode) {
                        "en" -> "Hello!"
                        "fr" -> "Bonjour!"
                        "es" -> "Hola！"
                        "zh" -> "你好！"
                        else -> {null}
                    }

                    Toast.makeText(
                        this@MainActivity,
                        greetingMessage,
                        Toast.LENGTH_SHORT
                    ).show()

                    startActivity(mapIntent)
                }


            }
        }
        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
    }

    private fun record() {

        if (packageManager.hasSystemFeature(PackageManager.FEATURE_MICROPHONE) &&
            ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.RECORD_AUDIO), 1)
        }


        val prompt = when (selectedLanguageCode) {
            "en" -> "Recording starts"
            "fr" -> "L'enregistrement commence"
            "es" -> "Comienza la grabación"
            "zh" -> "记录开始"
            else -> {"You haven't chosen any language yet!"}
        }



//        packageManager.hasSystemFeature(PackageManager.FEATURE_MICROPHONE)

        Toast.makeText(
            this,
            prompt,
            Toast.LENGTH_SHORT
        ).show()

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, selectedLanguageCode)
        }
        speechRecognizer.startListening(intent)
    }

    // Inside an Activity or Fragment:

    private fun setupListener() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, selectedLanguageCode)
        }

        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onResults(results: Bundle) {
                Log.d(TAG,"RecognitionListener.onResults")
                    val matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    findViewById<EditText>(id.editTextText).setText(matches[0])

                    Log.d(TAG, "Recognized speech match: " + matches.toString() )
                }
            }

            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {
                Log.d(TAG, "onBeginningOfSpeech")
            }
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {
                Log.d(TAG, "onEndOfSpeech")
            }
            override fun onError(error: Int) {
                Log.d(TAG, "onError: $error")
            }
            override fun onPartialResults(partialResults: Bundle?) {
                Log.d(TAG, "partialResults: $partialResults")
            }
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

//        speechRecognizer.startListening(intent)

    }

    override fun onResume() {
        sensorManager?.registerListener(sensorListener, sensorManager!!.getDefaultSensor(
            Sensor .TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL
        )
        super.onResume()
    }

    override fun onPause() {
        sensorManager!!.unregisterListener(sensorListener)
        super.onPause()
    }


}