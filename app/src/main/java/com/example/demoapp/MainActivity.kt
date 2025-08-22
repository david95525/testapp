package com.example.demoapp

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.mlsdk.ObjectDetectionSDK

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 建立 SDK
        val sdk = ObjectDetectionSDK(this)

        // 載入測試圖片 (res/drawable/demo.jpg)
        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.demo)

        // 在 background thread 執行推理
        Thread {
            val results = sdk.detectObjects(bitmap)

            runOnUiThread {
                results.forEach {
                    Log.d("Detection", "Class: ${it.clazz}, Score: ${it.detectionScore}, W:${it.width}, H:${it.height}")
                }
            }
        }.start()
    }
}
