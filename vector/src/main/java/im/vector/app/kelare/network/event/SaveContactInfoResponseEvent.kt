package im.vector.app.kelare.network.event

import im.vector.app.kelare.network.response.SaveContactInfoResponse
import retrofit2.Response

/**
 * @author jsqi
 */
class SaveContactInfoResponseEvent: ResponseEvent<SaveContactInfoResponse> {

    constructor(basicResponse: SaveContactInfoResponse, response: Response<*>) : super(basicResponse, response) {}

    constructor(paramRetrofitError: Throwable) : super(paramRetrofitError) {}
}
