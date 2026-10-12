package com.example.catplayground.image

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.Typeface
import com.example.catplayground.domain.model.CaptionColor
import com.example.catplayground.domain.model.CaptionPosition
import com.example.catplayground.domain.model.VisualFilter

object CatImageProcessor {

    fun applyFilter(source: Bitmap, filter: VisualFilter): Bitmap {
        if (filter == VisualFilter.ORIGINAL) {
            return source
        }

        val filteredBitmap = Bitmap.createBitmap(source.width, source.height, source.config ?: Bitmap.Config.ARGB_8888)
        val canvas = Canvas(filteredBitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        val colorMatrix = ColorMatrix(filter.getColorMatrix())
        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        canvas.drawBitmap(source, 0f, 0f, paint)

        return filteredBitmap
    }

    fun renderMeme(
        source: Bitmap,
        caption: String,
        position: CaptionPosition,
        textColor: CaptionColor,
        filter: VisualFilter
    ): Bitmap {
        val baseBitmap = applyFilter(source, filter)
        if (caption.isBlank()) {
            return baseBitmap
        }

        val outputBitmap = baseBitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(outputBitmap)
        val width = outputBitmap.width
        val height = outputBitmap.height

        val textSize = (width * 0.07f).coerceIn(36f, 120f)

        // Outline paint (Stroke)
        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.textSize = textSize
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            style = Paint.Style.STROKE
            strokeWidth = textSize * 0.12f
            color = Color.BLACK
            textAlign = Paint.Align.CENTER
        }

        // Fill paint
        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.textSize = textSize
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            style = Paint.Style.FILL
            color = textColor.hexColor.toInt()
            textAlign = Paint.Align.CENTER
        }

        val x = width / 2f
        val lines = wrapText(caption.uppercase(), fillPaint, (width * 0.9f))

        val lineHeight = textSize * 1.25f
        val totalTextHeight = lines.size * lineHeight

        val startY = when (position) {
            CaptionPosition.TOP -> textSize * 1.3f
            CaptionPosition.CENTER -> (height / 2f) - (totalTextHeight / 2f) + textSize
            CaptionPosition.BOTTOM -> height - (totalTextHeight) + (textSize * 0.8f)
        }

        lines.forEachIndexed { index, line ->
            val y = startY + (index * lineHeight)
            canvas.drawText(line, x, y, strokePaint)
            canvas.drawText(line, x, y, fillPaint)
        }

        return outputBitmap
    }

    private fun wrapText(text: String, paint: Paint, maxWidth: Float): List<String> {
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var currentLine = StringBuilder()

        for (word in words) {
            val potentialLine = if (currentLine.isEmpty()) word else "$currentLine $word"
            if (paint.measureText(potentialLine) <= maxWidth) {
                currentLine = StringBuilder(potentialLine)
            } else {
                if (currentLine.isNotEmpty()) {
                    lines.add(currentLine.toString())
                }
                currentLine = StringBuilder(word)
            }
        }
        if (currentLine.isNotEmpty()) {
            lines.add(currentLine.toString())
        }
        return lines.ifEmpty { listOf(text) }
    }
}
