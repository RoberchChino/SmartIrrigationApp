package com.example.smartirrigationapp

data class Reading(
    val humidity: Int,
    val dateTime: String,
    val irrigationOn: Boolean
)