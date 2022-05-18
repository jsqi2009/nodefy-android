package im.vector.app.kelare.network.event

import im.vector.app.kelare.network.response.SaveAccountInfoResponse
import retrofit2.Response

/**
 * @author jsqi
 */
class SaveAccountInfoResponseEvent: ResponseEvent<SaveAccountInfoResponse> {

    constructor(basicResponse: SaveAccountInfoResponse, response: Response<*>) : super(basicResponse, response) {}

    constructor(paramRetrofitError: Throwable) : super(paramRetrofitError) {}
}
