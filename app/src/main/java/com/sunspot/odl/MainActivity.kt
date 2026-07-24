package com.sunspot.odl

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import com.sunspot.dialog.CommonUseDialog
import com.sunspot.odl.databinding.ActivityMainBinding

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        var binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnShowDialog.setOnClickListener {
            val dialog = CommonUseDialog(this)
            dialog.setTitle("提示")
                .setMessge("Just Relax")
                .show()
        }
    }
}