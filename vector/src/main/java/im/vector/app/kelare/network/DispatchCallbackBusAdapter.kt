package im.vector.app.kelare.network

/**
 * Created by jsqi on 4/3/22.
 */

import im.vector.app.kelare.network.event.ResponseEvent
import im.vector.app.kelare.network.response.BResponse
import com.squareup.otto.Bus

import retrofit2.Response

class DispatchCallbackBusAdapter : DispatchCallback<BResponse> {

    private var mEventBus: Bus? = null
    private var mEventClass: Class<out ResponseEvent<*>>? = null
    private var mRequestCallParameter: Any? = null

    constructor(bus: Bus, paramClass: Class<out ResponseEvent<*>>) {
        this.mEventBus = bus
        this.mEventClass = paramClass
    }

    constructor(bus: Bus, paramClass: Class<out ResponseEvent<*>>, o: Any) {
        this.mEventBus = bus
        this.mRequestCallParameter = o
        this.mEventClass = paramClass
    }

    private fun createResponseEvent(basicResponse: BResponse, response: Response<*>): Any {
        val localObject: Any
        try {
            val localClass = this.mEventClass
            val arrayOfClass = arrayOfNulls<Class<*>>(2)
            arrayOfClass[0] = basicResponse.javaClass
            arrayOfClass[1] = Response::class.java
            localObject = localClass!!.getConstructor(*arrayOfClass).newInstance(basicResponse, response)

        } catch (e: Exception) {
            throw RuntimeException(e)
        }

        return localObject
    }

    private fun createResponseEvent(basicResponse: BResponse, response: Response<*>, o: Any): Any {
        val localObject: Any
        try {
            val localClass = this.mEventClass
            val parameterTypes = arrayOfNulls<Class<*>>(3)
            parameterTypes[0] = basicResponse.javaClass
            parameterTypes[1] = Response::class.java
            parameterTypes[2] = o.javaClass
            localObject = localClass!!.getConstructor(*parameterTypes).newInstance(basicResponse, response, o)
        } catch (e: Exception) {
            throw RuntimeException(e)
        }

        return localObject
    }

    private fun createResponseEvent(error: Throwable, o: Any): Any {
        try {
            return mEventClass!!.getConstructor(Throwable::class.java, o.javaClass).newInstance(error, o)
        } catch (e: Exception) {
            throw RuntimeException(e)
        }

    }


    private fun createResponseEvent(error: Throwable): Any {
        try {
            return mEventClass!!.getConstructor(Throwable::class.java).newInstance(error)
        } catch (e: Exception) {
            throw RuntimeException(e)
        }

    }

    override fun onDispatchError(dispatchResponse: BResponse, response: Response<*>) {
        if (mRequestCallParameter != null) {
            this.mEventBus!!.post(createResponseEvent(dispatchResponse, response, mRequestCallParameter!!))
        } else {
            this.mEventBus!!.post(createResponseEvent(dispatchResponse, response))
        }

    }

    override fun onDispatchNetworkError(error: Throwable) {
        if (mRequestCallParameter != null) {
            this.mEventBus!!.post(createResponseEvent(error, mRequestCallParameter!!))
        } else {
            this.mEventBus!!.post(createResponseEvent(error))
        }

    }

    override fun onDispatchSuccess(dispatchResponse: BResponse, response: Response<*>) {
        if (mRequestCallParameter != null) {
            this.mEventBus!!.post(createResponseEvent(dispatchResponse, response, mRequestCallParameter!!))
        } else {
            this.mEventBus!!.post(createResponseEvent(dispatchResponse, response))
        }
    }


}
