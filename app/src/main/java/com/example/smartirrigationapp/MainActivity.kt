package com.example.smartirrigationapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast

class MainActivity : AppCompatActivity() {

    private lateinit var btnMonitor: Button
    private lateinit var btnHistory: Button
    private lateinit var btnAbout: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnMonitor = findViewById(R.id.btnMonitor)
        btnHistory = findViewById(R.id.btnHistory)
        btnAbout = findViewById(R.id.btnAbout)

        // Ir a la pantalla de monitoreo
        btnMonitor.setOnClickListener {
            startActivity(Intent(this, MonitorActivity::class.java))
        }

        // Ver historial
        btnHistory.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }

        btnAbout.setOnClickListener {
            Toast.makeText(this, "App de riego inteligente\nVersión Kotlin", Toast.LENGTH_LONG).show()
        }
    }
}