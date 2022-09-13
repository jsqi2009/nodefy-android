package im.vector.app.kelare.network.event

import im.vector.app.kelare.network.response.GetBotRoomResponse
import im.vector.app.kelare.network.response.SetBotRoomResponse
import retrofit2.Response

/**
 * @author jsqi
 */
class SetBotRoomResponseEvent: ResponseEvent<SetBotRoomResponse> {

    constructor(basicResponse: SetBotRoomResponse, response: Response<*>) : super(basicResponse, response) {}

    constructor(paramRetrofitError: Throwable) : super(paramRetrofitError) {}
}
