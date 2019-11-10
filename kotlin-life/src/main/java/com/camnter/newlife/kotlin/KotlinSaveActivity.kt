package com.camnter.newlife.kotlin

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_kotlin_save.*

/**
 * @author CaMnter
 */

class KotlinSaveActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kotlin_save)
        saveText.text = "Hello Kotlin 2333"
    }
}