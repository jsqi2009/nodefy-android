package im.vector.app.kelare.network.event

import im.vector.app.kelare.network.response.GetContactRelationResponse
import im.vector.app.kelare.network.response.GetContactResponse
import im.vector.app.kelare.network.response.UpdateContactRelationResponse
import retrofit2.Response

/**
 * @author jsqi
 */
class UpdateContactRelationResponseEvent: ResponseEvent<UpdateContactRelationResponse> {

    constructor(basicResponse: UpdateContactRelationResponse, response: Response<*>) : super(basicResponse, response) {}

    constructor(paramRetrofitError: Throwable) : super(paramRetrofitError) {}
}
