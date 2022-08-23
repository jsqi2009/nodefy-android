package im.vector.app.kelare.network.event

import im.vector.app.kelare.network.response.GetAccountContactResponse
import im.vector.app.kelare.network.response.GetContactResponse
import im.vector.app.kelare.network.response.PresenceStatusResponse
import retrofit2.Response

/**
 * @author jsqi
 */
class PresenceStatusResponseEvent: ResponseEvent<PresenceStatusResponse> {

    constructor(basicResponse: PresenceStatusResponse, response: Response<*>) : super(basicResponse, response) {}

    constructor(paramRetrofitError: Throwable) : super(paramRetrofitError) {}
}
