package im.vector.app.kelare.network.event

import im.vector.app.kelare.network.response.GetContactRelationResponse
import im.vector.app.kelare.network.response.GetContactResponse
import retrofit2.Response

/**
 * @author jsqi
 */
class GetContactRelationResponseEvent: ResponseEvent<GetContactRelationResponse> {

    constructor(basicResponse: GetContactRelationResponse, response: Response<*>) : super(basicResponse, response) {}

    constructor(paramRetrofitError: Throwable) : super(paramRetrofitError) {}
}
