package com.example.catplayground.domain.model

enum class VisualFilter(val displayName: String) {
    ORIGINAL("Original"),
    MONOCHROME("B&W"),
    WARM("Warm"),
    NEGATIVE("Negative"),
    SEPIA("Sepia"),
    COOL("Cool");

    fun getColorMatrix(): FloatArray {
        return when (this) {
            ORIGINAL -> floatArrayOf(
                1f, 0f, 0f, 0f, 0f,
                0f, 1f, 0f, 0f, 0f,
                0f, 0f, 1f, 0f, 0f,
                0f, 0f, 0f, 1f, 0f
            )
            MONOCHROME -> floatArrayOf(
                0.33f, 0.59f, 0.11f, 0f, 0f,
                0.33f, 0.59f, 0.11f, 0f, 0f,
                0.33f, 0.59f, 0.11f, 0f, 0f,
                0f, 0f, 0f, 1f, 0f
            )
            WARM -> floatArrayOf(
                1.2f, 0f, 0f, 0f, 30f,
                0f, 1.0f, 0f, 0f, 10f,
                0f, 0f, 0.8f, 0f, -20f,
                0f, 0f, 0f, 1f, 0f
            )
            NEGATIVE -> floatArrayOf(
                -1f, 0f, 0f, 0f, 255f,
                0f, -1f, 0f, 0f, 255f,
                0f, 0f, -1f, 0f, 255f,
                0f, 0f, 0f, 1f, 0f
            )
            SEPIA -> floatArrayOf(
                0.393f, 0.769f, 0.189f, 0f, 0f,
                0.349f, 0.686f, 0.168f, 0f, 0f,
                0.272f, 0.534f, 0.131f, 0f, 0f,
                0f, 0f, 0f, 1f, 0f
            )
            COOL -> floatArrayOf(
                0.8f, 0f, 0f, 0f, -10f,
                0f, 1.0f, 0f, 0f, 10f,
                0f, 0f, 1.3f, 0f, 30f,
                0f, 0f, 0f, 1f, 0f
            )
        }
    }
}
