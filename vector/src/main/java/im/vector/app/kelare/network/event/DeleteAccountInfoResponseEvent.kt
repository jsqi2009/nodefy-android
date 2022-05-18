package im.vector.app.kelare.network.event

import im.vector.app.kelare.network.response.DeleteAccountInfoResponse
import retrofit2.Response

/**
 * @author jsqi
 */
class DeleteAccountInfoResponseEvent: ResponseEvent<DeleteAccountInfoResponse> {

    constructor(basicResponse: DeleteAccountInfoResponse, response: Response<*>) : super(basicResponse, response) {}

    constructor(paramRetrofitError: Throwable) : super(paramRetrofitError) {}
}
