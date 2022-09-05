package im.vector.app.kelare.network.event

import im.vector.app.kelare.network.response.GetAllContactRelationResponse
import im.vector.app.kelare.network.response.GetContactRelationResponse
import im.vector.app.kelare.network.response.GetContactResponse
import retrofit2.Response

/**
 * @author jsqi
 */
class GetAllContactRelationResponseEvent: ResponseEvent<GetAllContactRelationResponse> {

    constructor(basicResponse: GetAllContactRelationResponse, response: Response<*>) : super(basicResponse, response) {}

    constructor(paramRetrofitError: Throwable) : super(paramRetrofitError) {}
}
