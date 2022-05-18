
package im.vector.app.kelare.content

import android.content.Context

class HashStorage internal constructor(private val mSafeSharedPreferences: SafeSharedPreferences) {

    constructor(paramContext: Context, paramString: String) : this(SafeSharedPreferences(paramContext, paramString)) {}

    fun buffer(): Buffer {
        return Buffer()
    }

    fun clear() {
        this.mSafeSharedPreferences.clear()
    }

    fun getBoolean(paramString: String): Boolean {
        return this.mSafeSharedPreferences.getBoolean(paramString, false)
    }

    fun getBoolean(paramString: String, paramBoolean: Boolean): Boolean {
        return this.mSafeSharedPreferences.getBoolean(paramString, paramBoolean)
    }

    fun getDouble(paramString: String): Double {
        return this.mSafeSharedPreferences.getDouble(paramString, 0.0)
    }

    fun getDouble(paramString: String, paramDouble: Double): Double {
        return this.mSafeSharedPreferences.getDouble(paramString, paramDouble)
    }

    fun getFloat(paramString: String): Float {
        return this.mSafeSharedPreferences.getFloat(paramString, 0.0f)
    }

    fun getFloat(paramString: String, paramFloat: Float): Float {
        return this.mSafeSharedPreferences.getFloat(paramString, paramFloat)
    }

    fun getInt(paramString: String): Int {
        return this.mSafeSharedPreferences.getInt(paramString, 0)
    }

    fun getInt(paramString: String, paramInt: Int): Int {
        return this.mSafeSharedPreferences.getInt(paramString, paramInt)
    }

    fun getLong(paramString: String): Long {
        return this.mSafeSharedPreferences.getLong(paramString, 0L)
    }

    fun getLong(paramString: String, paramLong: Long): Long {
        return this.mSafeSharedPreferences.getLong(paramString, paramLong)
    }

    fun getString(paramString: String): String {
        return this.mSafeSharedPreferences.getString(paramString, "")!!
    }

    fun put(paramString: String, paramDouble: Double) {
        this.mSafeSharedPreferences.put(paramString, paramDouble)
        this.mSafeSharedPreferences.apply()
    }

    fun put(paramString: String, paramFloat: Float) {
        this.mSafeSharedPreferences.put(paramString, paramFloat)
        this.mSafeSharedPreferences.apply()
    }

    fun put(paramString: String, paramInt: Int) {
        this.mSafeSharedPreferences.put(paramString, paramInt)
        this.mSafeSharedPreferences.apply()
    }

    fun put(paramString: String, paramLong: Long) {
        this.mSafeSharedPreferences.put(paramString, paramLong)
        this.mSafeSharedPreferences.apply()
    }

    fun put(paramString1: String, paramString2: String) {
        this.mSafeSharedPreferences.put(paramString1, paramString2)
        this.mSafeSharedPreferences.apply()
    }

    fun put(paramString: String, paramBoolean: Boolean) {
        this.mSafeSharedPreferences.put(paramString, paramBoolean)
        this.mSafeSharedPreferences.apply()
    }

    fun remove(paramString: String) {
        this.mSafeSharedPreferences.remove(paramString)
        this.mSafeSharedPreferences.apply()
    }

    inner class Buffer {

        fun apply() {
            this@HashStorage.mSafeSharedPreferences.apply()
        }

        fun clear() {
            this@HashStorage.mSafeSharedPreferences.clearBuffers()
        }

        fun put(paramString: String, paramDouble: Double): Buffer {
            this@HashStorage.mSafeSharedPreferences.put(paramString, paramDouble)
            return this
        }

        fun put(paramString: String, paramFloat: Float): Buffer {
            this@HashStorage.mSafeSharedPreferences.put(paramString, paramFloat)
            return this
        }

        fun put(paramString: String, paramInt: Int): Buffer {
            this@HashStorage.mSafeSharedPreferences.put(paramString, paramInt)
            return this
        }

        fun put(paramString: String, paramLong: Long): Buffer {
            this@HashStorage.mSafeSharedPreferences.put(paramString, paramLong)
            return this
        }

        fun put(paramString1: String, paramString2: String): Buffer {
            this@HashStorage.mSafeSharedPreferences.put(paramString1, paramString2)
            return this
        }

        fun put(paramString: String, paramBoolean: Boolean): Buffer {
            this@HashStorage.mSafeSharedPreferences.put(paramString, paramBoolean)
            return this
        }

        fun remove(paramString: String): Buffer {
            this@HashStorage.mSafeSharedPreferences.remove(paramString)
            return this
        }
    }
}
