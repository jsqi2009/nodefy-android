package im.vector.app.kelare.network.event

import im.vector.app.kelare.network.response.DialerAccountInfoResponse
import retrofit2.Response

/**
 * @author jsqi
 */
class DialerAccountInfoResponseEvent: ResponseEvent<DialerAccountInfoResponse> {

    constructor(basicResponse: DialerAccountInfoResponse, response: Response<*>) : super(basicResponse, response) {}

    constructor(paramRetrofitError: Throwable) : super(paramRetrofitError) {}
}
