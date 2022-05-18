package im.vector.app.kelare.network

/**
 * Created by jsqi on 4/3/22.
 */
import im.vector.app.kelare.network.response.BResponse

import retrofit2.Response

interface DispatchCallback<T : BResponse> {

    fun onDispatchError(paramT: T, paramResponse: Response<*>)

    fun onDispatchNetworkError(paramRetrofitError: Throwable)

    fun onDispatchSuccess(paramT: T, paramResponse: Response<*>)
}
