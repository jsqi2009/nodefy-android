package im.vector.app.kelare.network.event

import im.vector.app.kelare.network.response.LoginResponse
import retrofit2.Response

/**
 * @author jsqi
 */
class LoginResponseEvent: ResponseEvent<LoginResponse> {

    constructor(basicResponse: LoginResponse, response: Response<*>) : super(basicResponse, response) {}

    constructor(paramRetrofitError: Throwable) : super(paramRetrofitError) {}
}
