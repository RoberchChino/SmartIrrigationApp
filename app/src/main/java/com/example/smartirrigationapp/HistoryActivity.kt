package com.example.smartirrigationapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView

class HistoryActivity : AppCompatActivity() {

    private lateinit var lvHistory: ListView
    private lateinit var btnCloseHistory: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        lvHistory = findViewById(R.id.lvHistory)
        btnCloseHistory = findViewById(R.id.btnCloseHistory)

        // Tomar la lista de lecturas
        val readings = ReadingsHolder.readingsList

        // Generar un listado de strings (formateados)
        val adapter = ArrayAdapter<String>(
            this,
            android.R.layout.simple_list_item_1,
            readings.map { formatReading(it) }
        )
        lvHistory.adapter = adapter

        btnCloseHistory.setOnClickListener {
            finish()
        }
    }

    private fun formatReading(r: Reading): String {
        return "Humedad: ${r.humidity}% - ${r.dateTime}\n" +
                "Riego: ${if (r.irrigationOn) "Activo" else "Inactivo"}"
    }
}