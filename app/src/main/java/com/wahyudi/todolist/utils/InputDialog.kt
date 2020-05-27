package com.wahyudi.todolist.utils

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.Window
import com.wahyudi.todolist.R
import kotlinx.android.synthetic.main.dialog_input.*

class InputDialog(context: Context, private val title: String, private val formLayout: View, private val saveAction: () -> Unit): Dialog(context){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setCancelable(false)
        setContentView(R.layout.dialog_input)

        if (formLayout.parent != null) {
            (formLayout.parent as ViewGroup).removeView(formLayout)
        }

        tv_title.text = title
        form_container.addView(formLayout)
        btn_cancel.setOnClickListener { dismiss() }
        btn_save.setOnClickListener {
            saveAction()
            dismiss()
        }
    }
}