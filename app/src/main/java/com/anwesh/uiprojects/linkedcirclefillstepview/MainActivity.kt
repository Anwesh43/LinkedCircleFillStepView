package com.anwesh.uiprojects.linkedcirclefillstepview

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.anwesh.uiprojects.circlefillstepview.CircleFillStepView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CircleFillStepView.create(this)
    }
}
