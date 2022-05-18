package im.vector.app.kelare.network

/**
 * Created by jsqi on 4/3/22.
 */

import android.text.TextUtils

import com.google.gson.ExclusionStrategy
import com.google.gson.FieldAttributes
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import im.vector.app.kelare.network.event.ResponseEvent
import im.vector.app.kelare.network.response.BResponse
import im.vector.app.kelare.content.AndroidBus

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class DispatchClient(private var mBus: AndroidBus) {

    fun enqueue(call: Call<JsonObject>, clasz: Class<*>, eventClass: Class<out ResponseEvent<*>>) {
        val dispatchCallbackBusAdapter = DispatchCallbackBusAdapter(mBus, eventClass)
        val callback = InternalCallback(clasz, dispatchCallbackBusAdapter)
        call.enqueue(callback)
    }

    fun enqueue(o: Any, call: Call<JsonObject>, clasz: Class<*>, eventClass: Class<out ResponseEvent<*>>) {
        val dispatchCallbackBusAdapter = DispatchCallbackBusAdapter(mBus, eventClass, o)
        val callback = InternalCallback(clasz, dispatchCallbackBusAdapter)
        call.enqueue(callback)
    }

    fun enqueue(call: Call<JsonObject>, clasz: Class<*>, eventClass: Class<out ResponseEvent<*>>, flag: String) {
        val dispatchCallbackBusAdapter = DispatchCallbackBusAdapter(mBus, eventClass)
        val callback = InternalCallback(clasz, dispatchCallbackBusAdapter, flag)
        call.enqueue(callback)
    }

    fun enqueue(call: Call<JsonObject>, clasz: Class<*>, eventClass: Class<out ResponseEvent<*>>, flag: String, postion: Int) {
        val dispatchCallbackBusAdapter = DispatchCallbackBusAdapter(mBus, eventClass)
        val callback = InternalCallback(clasz, dispatchCallbackBusAdapter, flag, postion)
        call.enqueue(callback)
    }

    private inner class InternalCallback : Callback<JsonObject> {

        private var position = -1
        var clasz: Class<BResponse>
        private var callback: DispatchCallbackBusAdapter? = null
        private var flag = ""

        constructor(clasz: Class<*>, dispatchCallbackBusAdapter: DispatchCallbackBusAdapter) {
            this.clasz = clasz as Class<BResponse>
            this.callback = dispatchCallbackBusAdapter
        }

        constructor(clasz: Class<*>, dispatchCallbackBusAdapter: DispatchCallbackBusAdapter, flag: String) {
            this.clasz = clasz as Class<BResponse>
            this.callback = dispatchCallbackBusAdapter
            this.flag = flag
        }

        constructor(clasz: Class<*>, dispatchCallbackBusAdapter: DispatchCallbackBusAdapter, flag: String, position: Int) {
            this.clasz = clasz as Class<BResponse>
            this.callback = dispatchCallbackBusAdapter
            this.flag = flag
            this.position = position
        }


        override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
            val jsonRpcResponse = response.body()
            if (response.isSuccessful && jsonRpcResponse != null) {
                try {
                    val gson = getGson(jsonRpcResponse)
                    val bResponse = gson.fromJson(jsonRpcResponse, clasz)
                    if (!TextUtils.isEmpty(flag)) {
                        bResponse.flag = flag
                    }
                    bResponse.position = position


                    callback!!.onDispatchSuccess(bResponse, response)
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            } else if (response.code() == 400 || response.code() == 401 || response.code() == 403) {
                val body = response.errorBody()
                try {
                    val gson = Gson()
                    val bResponse = gson.fromJson(body!!.string(), clasz)
                    callback!!.onDispatchSuccess(bResponse, response)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                //callback!!.onDispatchLogout()
            } else {
                callback!!.onDispatchNetworkError(Throwable("network_error"))
            }
        }

        override fun onFailure(call: Call<JsonObject>, t: Throwable) {
            val message = t.message
            if ("Socket closed" != message && "Canceled" != message) {
                callback!!.onDispatchNetworkError(t)
            }
        }


        private fun getGson(jsonObject: JsonObject?): Gson {
            var gson: Gson? = null
            if (jsonObject != null) {
                //val errNumJsonObject = jsonObject.get("code")
                //val errNum = errNumJsonObject.asInt

                //if (errNum == 0 || errNum == 403 || errNum == 200 || errNum == 201) {
                if (!jsonObject.has("errcode")) {
                    gson = Gson()
                } else {
                    gson = GsonBuilder().setExclusionStrategies(
                        object : ExclusionStrategy {
                            override fun shouldSkipField(f: FieldAttributes): Boolean {
                                return f.name == "data"
                            }

                            override fun shouldSkipClass(clazz: Class<*>): Boolean {
                                // 过滤掉 类名包含 Bean的类
                                return false
                            }
                        }).create()
                }
            } else {
                gson = Gson()
            }
            return gson!!
        }
    }
}
