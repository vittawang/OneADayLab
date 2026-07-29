package com.sunspot.odl

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import com.sunspot.dialog.CommonUseDialog
import com.sunspot.libext.addAndCommit
import com.sunspot.odl.databinding.ActivityMainBinding
import com.sunspot.progress.ProgressBarActivity

class MainActivity : FragmentActivity() {
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
        binding.progressBar.setOnClickListener { startActivity(Intent(this, ProgressBarActivity::class.java)) }
        UsualFragment().addAndCommit(supportFragmentManager, container = R.id.frag_container)
    }
}