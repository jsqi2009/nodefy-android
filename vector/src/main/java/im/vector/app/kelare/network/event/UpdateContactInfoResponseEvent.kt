package im.vector.app.kelare.network.event

import im.vector.app.kelare.network.response.UpdateContactInfoResponse
import retrofit2.Response

/**
 * @author jsqi
 */
class UpdateContactInfoResponseEvent: ResponseEvent<UpdateContactInfoResponse> {

    constructor(basicResponse: UpdateContactInfoResponse, response: Response<*>) : super(basicResponse, response) {}

    constructor(paramRetrofitError: Throwable) : super(paramRetrofitError) {}
}
