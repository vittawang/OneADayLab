package com.sunspot.collect.fragl

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.sunspot.collect.R

/**
 * 显示一次 FragmentTransaction 的提交方法和实例信息。
 */
class CommitDemoFragment : Fragment(R.layout.fragl_commit_demo_fragment) {

    val demoLabel: String
        get() = requireArguments().getString(ARG_METHOD).orEmpty()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val arguments = requireArguments()
        view.findViewById<TextView>(R.id.fragl_commit_fragment_method).text =
            arguments.getString(ARG_METHOD)
        view.findViewById<TextView>(R.id.fragl_commit_fragment_instance).text =
            getString(
                R.string.fragl_commit_fragment_instance,
                arguments.getInt(ARG_INSTANCE)
            )
        view.findViewById<TextView>(R.id.fragl_commit_fragment_created).text =
            getString(
                R.string.fragl_commit_fragment_created,
                arguments.getString(ARG_CREATED_TIME).orEmpty()
            )
    }

    companion object {
        private const val ARG_METHOD = "method"
        private const val ARG_INSTANCE = "instance"
        private const val ARG_CREATED_TIME = "created_time"

        fun newInstance(
            method: String,
            instance: Int,
            createdTime: String
        ): CommitDemoFragment =
            CommitDemoFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_METHOD, method)
                    putInt(ARG_INSTANCE, instance)
                    putString(ARG_CREATED_TIME, createdTime)
                }
            }
    }
}
