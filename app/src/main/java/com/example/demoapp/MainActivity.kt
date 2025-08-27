package com.example.demoapp

import android.widget.ImageView
import android.widget.TextView
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.mlsdk.ObjectDetectionSDK

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val imageView = findViewById<ImageView>(R.id.imageView)
        val resultText = findViewById<TextView>(R.id.resultText)
        // 建立 SDK
        val sdk = ObjectDetectionSDK(this)

        // 載入測試圖片 (res/drawable/demo.jpg)
        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.demo)

        // 在 background thread 執行推理
        Thread {
            val results = sdk.detectObjects(bitmap)

            runOnUiThread {
                val sb = StringBuilder()
                results.forEach {
                    sb.append("Class: ${it.clazz}, Score: ${"%.2f".format(it.detectionScore)}, W:${it.width}, H:${it.height}\n")
                    Log.d("Detection", sb.toString())
                }
                resultText.text = sb.toString()
                results.forEach {
                    Log.d("Detection", "Class: ${it.clazz}, Score: ${it.detectionScore}, W:${it.width}, H:${it.height}")
                }
            }
        }.start()
    }
}
