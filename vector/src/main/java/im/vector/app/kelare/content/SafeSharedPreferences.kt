package im.vector.app.kelare.content


import android.content.Context

class SafeSharedPreferences(paramContext: Context, paramString: String) {
    private val mBufferedSharedPreferences: BufferedSharedPreferences

    init {
        this.mBufferedSharedPreferences = BufferedSharedPreferences(paramContext, paramString)
    }

    fun apply() {
        this.mBufferedSharedPreferences.apply()
    }

    fun clear() {
        this.mBufferedSharedPreferences.clear()
    }

    fun clearBuffers(): SafeSharedPreferences {
        this.mBufferedSharedPreferences.clearBuffers()
        return this
    }

    fun clearRemoveBuffer(): SafeSharedPreferences {
        this.mBufferedSharedPreferences.clearRemoveBuffer()
        return this
    }

    fun clearWriteBuffer(): SafeSharedPreferences {
        this.mBufferedSharedPreferences.clearWriteBuffer()
        return this
    }

    fun getBoolean(paramString: String, paramBoolean: Boolean): Boolean {
        return java.lang.Boolean.valueOf(getString(paramString, java.lang.Boolean.toString(paramBoolean)))
    }

    fun getDouble(paramString: String, paramDouble: Double): Double {
        return java.lang.Double.valueOf(getString(paramString, java.lang.Double.toString(paramDouble))!!).toDouble()
    }

    fun getFloat(paramString: String, paramFloat: Float): Float {
        return java.lang.Float.valueOf(getString(paramString, java.lang.Float.toString(paramFloat))!!).toFloat()
    }

    fun getInt(paramString: String, paramInt: Int): Int {
        return Integer.valueOf(getString(paramString, Integer.toString(paramInt))!!).toInt()
    }

    fun getLong(paramString: String, paramLong: Long): Long {
        return java.lang.Long.valueOf(getString(paramString, java.lang.Long.toString(paramLong))!!).toLong()
    }

    fun getString(paramString1: String, paramString2: String): String? {
        return this.mBufferedSharedPreferences.getString(paramString1, paramString2)
    }

    fun put(paramString: String, paramDouble: Double): SafeSharedPreferences {
        this.mBufferedSharedPreferences.put(paramString, java.lang.Double.toString(paramDouble))
        return this
    }

    fun put(paramString: String, paramFloat: Float): SafeSharedPreferences {
        this.mBufferedSharedPreferences.put(paramString, java.lang.Float.toString(paramFloat))
        return this
    }

    fun put(paramString: String, paramInt: Int): SafeSharedPreferences {
        this.mBufferedSharedPreferences.put(paramString, Integer.toString(paramInt))
        return this
    }

    fun put(paramString: String, paramLong: Long): SafeSharedPreferences {
        this.mBufferedSharedPreferences.put(paramString, java.lang.Long.toString(paramLong))
        return this
    }

    fun put(paramString1: String, paramString2: String): SafeSharedPreferences {
        this.mBufferedSharedPreferences.put(paramString1, paramString2)
        return this
    }

    fun put(paramString: String, paramBoolean: Boolean): SafeSharedPreferences {
        this.mBufferedSharedPreferences.put(paramString, java.lang.Boolean.toString(paramBoolean))
        return this
    }

    fun remove(paramString: String): SafeSharedPreferences {
        this.mBufferedSharedPreferences.remove(paramString)
        return this
    }
}
