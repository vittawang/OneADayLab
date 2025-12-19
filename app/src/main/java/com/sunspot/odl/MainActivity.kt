package com.sunspot.odl

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.core.view.WindowCompat.enableEdgeToEdge

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(window)
    }
}