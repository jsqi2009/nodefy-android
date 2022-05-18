package im.vector.app.kelare.network.event


import android.text.TextUtils

import im.vector.app.kelare.network.response.BResponse

import java.io.InterruptedIOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

import retrofit2.Response

open class ResponseEvent<T : BResponse> {
    var networkError: Throwable? = null
    var response: Response<*>? = null
    var model: T? = null
    internal var errorMessage: String = ""

    val retMsg: String
        get() = if (model != null) {
            this.model!!.retMsg
        } else {
            ""
        }

    val statusCode: Int
        get() = if (this.response != null) this.response!!.code() else 0

    /*val isSuccess: Boolean
        get() = this.networkError == null && (model!!.code == 200 || model!!.code == 201)*/

    val isSuccess: Boolean
        get() = this.networkError == null && (model!!.errcode == null && model!!.error == null)

    constructor(t: T, response: Response<*>) {
        this.model = t
        this.response = response
    }

    constructor(error: Throwable) {
        this.networkError = error
    }

    constructor(error: Throwable, errorMessage: String) {
        this.networkError = error
        this.errorMessage = errorMessage
    }

    fun getErrorMessage(): String {
        if (model != null) {
            if (model!!.code == 200 || model!!.code == 201) {
                return this.model!!.retMsg
            }
        }
        if (this.networkError != null) {
            if (networkError is UnknownHostException) {
                return if (TextUtils.isEmpty(errorMessage)) "网络连接失败，请检查网络设置" else errorMessage
            } else if (networkError is SocketTimeoutException) {
                return if (TextUtils.isEmpty(errorMessage)) "网络请求超时,请稍后重试" else errorMessage
            } else if (networkError is ConnectException) {
                return if (TextUtils.isEmpty(errorMessage)) "网络连接失败，请检查网络设置" else errorMessage
            } else if (networkError is InterruptedIOException) {
                return if (TextUtils.isEmpty(errorMessage)) "网络请求超时,请稍后重试" else errorMessage
            }
            return this.networkError!!.message!!
        }
        return "Network connection failed, please check network settings"
    }

    fun hasNetworkError(): Boolean {
        return this.networkError != null
    }

    fun hasAccountError(): Boolean {
        return model != null && model!!.errNum > 0
    }
}
