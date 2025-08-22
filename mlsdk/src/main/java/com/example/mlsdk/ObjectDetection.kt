package com.example.mlsdk

import android.content.Context
import android.graphics.Bitmap
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.image.TensorImage

data class Detection(
    val width: Int,
    val height: Int,
    val detectionScore: Float,
    val clazz: Int
)

class ObjectDetectionSDK(context: Context) {

    private val interpreter: Interpreter
    private val inputWidth: Int
    private val inputHeight: Int
    private val maxDetections: Int

    init {
        // 載入模型
        val model = FileUtil.loadMappedFile(context, "demo.tflite")
        interpreter = Interpreter(model)

        // 取得輸入 tensor 尺寸
        val inputTensorShape = interpreter.getInputTensor(0).shape() // [1, height, width, 3]
        inputHeight = inputTensorShape[1]
        inputWidth = inputTensorShape[2]

        // 取得輸出 tensor 最大檢測數量
        val outputShape = interpreter.getOutputTensor(0).shape() // [1, maxDetections, 4]
        maxDetections = outputShape[1]
    }

    fun detectObjects(bitmap: Bitmap): List<Detection> {
        // Resize bitmap
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, inputWidth, inputHeight, true)
        val inputImage = TensorImage.fromBitmap(resizedBitmap)
        val inputBuffer = inputImage.buffer

        // 建立輸出 Array 對應模型
        val outputBoxes = Array(1) { Array(maxDetections) { FloatArray(4) } }  // [1, maxDetections, 4]
        val outputClasses = Array(1) { FloatArray(maxDetections) }             // [1, maxDetections]
        val outputScores = Array(1) { FloatArray(maxDetections) }              // [1, maxDetections]
        val numDetections = FloatArray(1)                                      // [1]

        val outputs = mapOf(
            0 to outputBoxes,
            1 to outputClasses,
            2 to outputScores,
            3 to numDetections
        )

        // 執行推理
        interpreter.runForMultipleInputsOutputs(arrayOf(inputBuffer), outputs)

        // 解析結果
        val detections = mutableListOf<Detection>()
        val num = numDetections[0].toInt().coerceAtMost(maxDetections)

        for (i in 0 until num) {
            val score = outputScores[0][i]
            if (score > 0.5f) {
                val left = outputBoxes[0][i][0]
                val top = outputBoxes[0][i][1]
                val right = outputBoxes[0][i][2]
                val bottom = outputBoxes[0][i][3]

                detections.add(
                    Detection(
                        width = ((right - left) * bitmap.width).toInt(),
                        height = ((bottom - top) * bitmap.height).toInt(),
                        detectionScore = score,
                        clazz = outputClasses[0][i].toInt()
                    )
                )
            }
        }

        return detections
    }
}

