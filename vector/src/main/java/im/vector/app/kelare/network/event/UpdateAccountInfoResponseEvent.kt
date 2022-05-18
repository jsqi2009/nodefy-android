package im.vector.app.kelare.network.event

import im.vector.app.kelare.network.response.UpdateAccountInfoResponse
import retrofit2.Response

/**
 * @author jsqi
 */
class UpdateAccountInfoResponseEvent: ResponseEvent<UpdateAccountInfoResponse> {

    constructor(basicResponse: UpdateAccountInfoResponse, response: Response<*>) : super(basicResponse, response) {}

    constructor(paramRetrofitError: Throwable) : super(paramRetrofitError) {}
}
