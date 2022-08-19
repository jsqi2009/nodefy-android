package im.vector.app.kelare.network.event

import im.vector.app.kelare.network.response.UpdateContactInfoResponse
import im.vector.app.kelare.network.response.UpdateDetailContactInfoResponse
import retrofit2.Response

/**
 * @author jsqi
 */
class UpdateDetailContactInfoResponseEvent: ResponseEvent<UpdateDetailContactInfoResponse> {

    constructor(basicResponse: UpdateDetailContactInfoResponse, response: Response<*>) : super(basicResponse, response) {}

    constructor(paramRetrofitError: Throwable) : super(paramRetrofitError) {}
}
