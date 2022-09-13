package im.vector.app.kelare.network.event

import im.vector.app.kelare.network.response.GetBotRoomResponse
import retrofit2.Response

/**
 * @author jsqi
 */
class GetBotRoomResponseEvent: ResponseEvent<GetBotRoomResponse> {

    constructor(basicResponse: GetBotRoomResponse, response: Response<*>) : super(basicResponse, response) {}

    constructor(paramRetrofitError: Throwable) : super(paramRetrofitError) {}
}
