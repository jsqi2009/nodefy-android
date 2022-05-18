package im.vector.app.kelare.network.event

import im.vector.app.kelare.network.response.DevicesListResponse
import retrofit2.Response

/**
 * @author jsqi
 *
 */
class DevicesListResponseEvent: ResponseEvent<DevicesListResponse> {

    constructor(basicResponse: DevicesListResponse, response: Response<*>) : super(basicResponse, response) {}

    constructor(paramRetrofitError: Throwable) : super(paramRetrofitError) {}
}
