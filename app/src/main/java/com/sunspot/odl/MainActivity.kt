package com.sunspot.odl

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import com.sunspot.collect.fragl.FragmentCommitDemoActivity
import com.sunspot.collect.fragl.CoroutineActivity
import com.sunspot.collect.fragl.FragShowHideActivity
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
        binding.progressBar.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    ProgressBarActivity::class.java
                )
            )
        }
        UsualFragment().addAndCommit(supportFragmentManager, container = R.id.frag_container)

        binding.coroutine.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    CoroutineActivity::class.java
                )
            )
        }

        binding.fragmentLifecycle.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    FragShowHideActivity::class.java
                )
            )
        }

        binding.fragmentCommitDemo.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    FragmentCommitDemoActivity::class.java
                )
            )
        }
    }
}
