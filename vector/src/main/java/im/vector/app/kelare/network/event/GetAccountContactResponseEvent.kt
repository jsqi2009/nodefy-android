package im.vector.app.kelare.network.event

import im.vector.app.kelare.network.response.GetAccountContactResponse
import im.vector.app.kelare.network.response.GetContactResponse
import retrofit2.Response

/**
 * @author jsqi
 */
class GetAccountContactResponseEvent: ResponseEvent<GetAccountContactResponse> {

    constructor(basicResponse: GetAccountContactResponse, response: Response<*>) : super(basicResponse, response) {}

    constructor(paramRetrofitError: Throwable) : super(paramRetrofitError) {}
}
