package com.themaskedguardian.fiskalniskener

import android.app.Application
import com.google.android.material.color.DynamicColors

class FiskalniSkenerApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Primenjujemo dinamičke boje na sve aktivnosti ako je Android 12+
        DynamicColors.applyToActivitiesIfAvailable(this)
    }
}
