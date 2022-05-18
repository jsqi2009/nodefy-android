
package im.vector.app.kelare.content

import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import android.text.TextUtils

import java.util.HashMap
import java.util.HashSet
import kotlin.collections.Map.Entry

class BufferedSharedPreferences(paramContext: Context, paramString: String) {
    private var mRemoveBuffer: MutableSet<String>? = null
    private var mSharedPreferences: SharedPreferences? = null
    private var mWriteBuffer: MutableMap<String, Any>? = null

    init {
        if (!TextUtils.isEmpty(paramString)) {
            this.mWriteBuffer = HashMap()
            this.mRemoveBuffer = HashSet()
            this.mSharedPreferences = paramContext.getSharedPreferences(paramString, 0)
        }
    }

    fun apply() {
        val editor: Editor
        try {
            editor = this.mSharedPreferences!!.edit()
            val localIterator1 = this.mRemoveBuffer!!.iterator()
            while (localIterator1.hasNext()) {
                val str = localIterator1.next()
                this.mWriteBuffer!!.remove(str)
                editor.remove(str)
            }
        } finally {
        }
        val iterator = this.mWriteBuffer!!.entries.iterator()
        while (iterator.hasNext()) {
            val localEntry = iterator.next() as Entry<*, *>
            val value = localEntry.value
            if (value is String)
                editor.putString(localEntry.key as String, value)
            else if (value is Int)
                editor.putInt(localEntry.key as String, value.toInt())
            else if (value is Long)
                editor.putLong(localEntry.key as String, value.toLong())
            else if (value is Float)
                editor.putFloat(localEntry.key as String, value.toFloat())
            else if (value is Boolean)
                editor.putBoolean(localEntry.key as String, value)
        }
        editor.apply()
        this.mWriteBuffer!!.clear()
        this.mRemoveBuffer!!.clear()
    }

    fun clear() {
        val localEditor = this.mSharedPreferences!!.edit()
        localEditor.clear()
        localEditor.apply()
        this.mWriteBuffer!!.clear()
    }

    fun clearBuffers(): BufferedSharedPreferences {
        return clearWriteBuffer().clearRemoveBuffer()

    }

    fun clearRemoveBuffer(): BufferedSharedPreferences {
        this.mRemoveBuffer!!.clear()
        return this

    }

    fun clearWriteBuffer(): BufferedSharedPreferences {

        this.mWriteBuffer!!.clear()
        return this

    }

    fun getBoolean(paramString: String, paramBoolean: Boolean): Boolean {
        return mSharedPreferences!!.getBoolean(paramString, paramBoolean)

    }

    fun getFloat(paramString: String, paramFloat: Float): Float {
        return mSharedPreferences!!.getFloat(paramString, paramFloat)

    }

    fun getInt(paramString: String, paramInt: Int): Int {
        return mSharedPreferences!!.getInt(paramString, paramInt)

    }

    fun getLong(paramString: String, paramLong: Long): Long {
        return mSharedPreferences!!.getLong(paramString, paramLong)

    }

    fun getString(paramString1: String, paramString2: String): String? {
        return mSharedPreferences!!.getString(paramString1, paramString2)

    }

    fun put(paramString: String, paramFloat: Float): BufferedSharedPreferences {
        this.mWriteBuffer!![paramString] = java.lang.Float.valueOf(paramFloat)
        return this

    }

    fun put(paramString: String, paramInt: Int): BufferedSharedPreferences {

        this.mWriteBuffer!![paramString] = Integer.valueOf(paramInt)
        return this

    }

    fun put(paramString: String, paramLong: Long): BufferedSharedPreferences {

        this.mWriteBuffer!![paramString] = java.lang.Long.valueOf(paramLong)
        return this

    }

    fun put(paramString1: String, paramString2: String): BufferedSharedPreferences {

        this.mWriteBuffer!![paramString1] = paramString2
        return this

    }

    fun put(paramString: String, paramBoolean: Boolean): BufferedSharedPreferences {

        this.mWriteBuffer!![paramString] = java.lang.Boolean.valueOf(paramBoolean)
        return this

    }

    fun remove(paramString: String): BufferedSharedPreferences {

        this.mRemoveBuffer!!.add(paramString)
        return this

    }
}
