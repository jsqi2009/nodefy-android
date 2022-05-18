package im.vector.app.kelare.network.event

import im.vector.app.kelare.network.response.GetContactResponse
import retrofit2.Response

/**
 * @author jsqi
 */
class GetContactResponseEvent: ResponseEvent<GetContactResponse> {

    constructor(basicResponse: GetContactResponse, response: Response<*>) : super(basicResponse, response) {}

    constructor(paramRetrofitError: Throwable) : super(paramRetrofitError) {}
}
