package com.example.springbreakchooser

import ShakeDetector
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.springbreakchooser.R.*

private lateinit var sensorManager: SensorManager
private var accelerometer: Sensor? = null
private lateinit var shakeDetector: ShakeDetector

private const val TAG: String = "MainActivity"

class MainActivity : AppCompatActivity() {
    private lateinit var operationSpinner: Spinner

    private var record: Boolean = false
    private lateinit var speechRecognizer: SpeechRecognizer

    private var selectedLanguageCode: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout.activity_main)

        operationSpinner = findViewById(id.operationSpinner)

        val operations = arrayOf("English", "French", "Spanish", "Mandarin")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, operations)
        operationSpinner.adapter = adapter

        val recordButton: Button = findViewById(R.id.button)

        if (packageManager.hasSystemFeature(PackageManager.FEATURE_MICROPHONE) &&
            ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.RECORD_AUDIO), 1)
        }

        setupListener()

        recordButton.setOnClickListener {
            if (!record) {
                record()
                record = true
            } else {
                speechRecognizer.stopListening()
                record = false
            }
        }

//        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
//        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
//        shakeDetector = ShakeDetector {
//            // This block will be executed when a shake is detected
//            openMapLocation()
//        }
    }

    private fun record() {

        if (packageManager.hasSystemFeature(PackageManager.FEATURE_MICROPHONE) &&
            ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.RECORD_AUDIO), 1)
        }


        val prompt = when (operationSpinner.selectedItem.toString()) {
            "English" -> "Recording starts"
            "French" -> "L'enregistrement commence"
            "Spanish" -> "Comienza la grabación"
            "Mandarin" -> "记录开始"
            else -> {null}
        }

        selectedLanguageCode = when (operationSpinner.selectedItem.toString()) {
            "English" -> "en"
            "French" -> "fr"
            "Spanish" -> "es"
            "Mandarin" -> "zh"
            else -> ("en")
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

//    private fun checkPermission() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            ActivityCompat.requestPermissions(
//                this,
//                arrayOf<String>(Manifest.permission.RECORD_AUDIO),
//                RecordAudioRequestCode
//            )
//        }
//    }

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

//    override fun onResume() {
//        super.onResume()
//        sensorManager.registerListener(shakeDetector, accelerometer, SensorManager.SENSOR_DELAY_UI)
//    }
//
//    override fun onPause() {
//        super.onPause()
//        sensorManager.unregisterListener(shakeDetector)
//    }


}