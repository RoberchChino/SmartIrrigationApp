package com.example.smartirrigationapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Random

class MonitorActivity : AppCompatActivity() {

    private lateinit var tvHumidityValue: TextView
    private lateinit var pbHumidity: ProgressBar
    private lateinit var swAutoMode: Switch
    private lateinit var btnManualIrrigation: Button
    private lateinit var tvIrrigationStatus: TextView
    private lateinit var btnUpdateHumidity: Button
    private lateinit var btnBack: Button

    private var isIrrigationOn = false
    private val HUMIDITY_THRESHOLD = 30
    private val random = Random()

    // Notificaciones
    private val CHANNEL_ID = "RIEGO_CHANNEL_ID"
    private val NOTIFICATION_ID = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_monitor)

        // Crear canal notificaciones (8.0+)
        createNotificationChannel()

        // Vincular vistas
        tvHumidityValue = findViewById(R.id.tvHumidityValue)
        pbHumidity = findViewById(R.id.pbHumidity)
        swAutoMode = findViewById(R.id.swAutoMode)
        btnManualIrrigation = findViewById(R.id.btnManualIrrigation)
        tvIrrigationStatus = findViewById(R.id.tvIrrigationStatus)
        btnUpdateHumidity = findViewById(R.id.btnUpdateHumidity)
        btnBack = findViewById(R.id.btnBack)

        pbHumidity.max = 100

        // Botón manual inicialmente deshabilitado si auto está ON
        btnManualIrrigation.isEnabled = !swAutoMode.isChecked

        // Listener para actualizar lectura
        btnUpdateHumidity.setOnClickListener {
            val humidity = random.nextInt(101) // 0..100
            updateHumidityUI(humidity)

            // Si auto está activado, controlar riego
            if (swAutoMode.isChecked) {
                if (humidity < HUMIDITY_THRESHOLD) {
                    activateIrrigation()
                } else {
                    deactivateIrrigation()
                }
            }

            // Registrar en historial
            val dateTime = getCurrentDateTimeString()
            val reading = Reading(humidity, dateTime, isIrrigationOn)
            ReadingsHolder.readingsList.add(reading)

            // Enviar al servidor
            sendReadingToServer(reading)
        }

        // Switch: modo automático / manual
        swAutoMode.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Auto => apagar riego manual y deshabilitar botón
                deactivateIrrigation()
                btnManualIrrigation.isEnabled = false
                btnManualIrrigation.text = "Activar Riego (Manual)"
            } else {
                // Manual => habilitar botón
                btnManualIrrigation.isEnabled = true
            }
        }

        // Botón riego manual
        btnManualIrrigation.setOnClickListener {
            if (!swAutoMode.isChecked) {
                if (isIrrigationOn) {
                    deactivateIrrigation()
                    btnManualIrrigation.text = "Activar Riego (Manual)"
                } else {
                    activateIrrigation()
                    btnManualIrrigation.text = "Desactivar Riego (Manual)"
                }
            } else {

            }
        }

        // Botón regresar
        btnBack.setOnClickListener {
            finish()
        }
    }


    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Notificaciones de Riego"
            val descriptionText = "Se muestran cuando se activa el riego"
            val importance = NotificationManager.IMPORTANCE_HIGH

            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }


    private fun updateHumidityUI(humidity: Int) {
        tvHumidityValue.text = "Humedad: $humidity %"
        pbHumidity.progress = humidity

        val colorRes = when {
            humidity < 30 -> R.color.colorRed
            humidity < 60 -> R.color.colorOrange
            else -> R.color.colorGreen
        }
        tvHumidityValue.setTextColor(ContextCompat.getColor(this, colorRes))
    }

    private fun activateIrrigation() {
        isIrrigationOn = true
        tvIrrigationStatus.text = "Riego: Activo"
        showRiegoNotification()
    }


    private fun deactivateIrrigation() {
        isIrrigationOn = false
        tvIrrigationStatus.text = "Riego: Inactivo"
    }

    private fun showRiegoNotification() {
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Cambia si quieres otro icono
            .setContentTitle("Riego Activado")
            .setContentText("El sistema encendió el riego (auto o manual).")
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        NotificationManagerCompat.from(this).notify(NOTIFICATION_ID, builder.build())
    }

    private fun getCurrentDateTimeString(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return dateFormat.format(Date())
    }

    private fun sendReadingToServer(reading: Reading) {
        val gson = Gson()
        val readingJson = gson.toJson(reading)

        val client = OkHttpClient()

        val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
        val body = readingJson.toRequestBody(mediaType)

        val request = Request.Builder()
            .url("https://reqres.in/api/readings")
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@MonitorActivity, "Fallo al enviar: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseStr = response.body?.string()
                if (response.isSuccessful) {
                    runOnUiThread {
                        Toast.makeText(this@MonitorActivity, "Enviado OK: $responseStr", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@MonitorActivity, "Error servidor: $responseStr", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }
}