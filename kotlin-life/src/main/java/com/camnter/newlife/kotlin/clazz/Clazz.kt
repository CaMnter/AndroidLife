package com.camnter.newlife.kotlin.clazz

import android.content.Context
import android.os.Bundle
import android.widget.Toast

/**
 * @author CaMnter
 */

open class KotlinSuperClass(name: String)

class KotlinClass(name: String, age: String) : KotlinSuperClass(name) {

    init {

    }

    fun onCreate(savedInstanceState: Bundle?) {

    }


    fun add(x: Int, y: Int): Int = x + y

    fun toast(context: Context, message: String, length: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(context, message, length).show()
    }

    fun niceToast(context: Context, message: String, tag: String = niceText("KotlinClass"), length: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(context, "class: $tag", length).show()
    }

    private fun niceText(text: String): String = "class: $text"


}