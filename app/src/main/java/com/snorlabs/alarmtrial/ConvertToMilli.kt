package com.snorlabs.alarmtrial

class ConvertToMilli(hour: Int, minute: Int, private val callback: ConvertToMilliCallback) {
    val hourMilli: Double = hour * 3.6e6
    val minuteMilli: Double = minute * 6.0e4
    val totalMilli: Double = hourMilli + minuteMilli

    init {
        callback.onConversionComplete(totalMilli)
    }
}