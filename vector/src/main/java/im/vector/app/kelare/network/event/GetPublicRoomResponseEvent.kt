package im.vector.app.kelare.network.event

import im.vector.app.kelare.network.response.GetPublicRoomResponse
import im.vector.app.kelare.network.response.SaveContactInfoResponse
import retrofit2.Response

/**
 * @author jsqi
 */
class GetPublicRoomResponseEvent: ResponseEvent<GetPublicRoomResponse> {

    constructor(basicResponse: GetPublicRoomResponse, response: Response<*>) : super(basicResponse, response) {}

    constructor(paramRetrofitError: Throwable) : super(paramRetrofitError) {}
}
