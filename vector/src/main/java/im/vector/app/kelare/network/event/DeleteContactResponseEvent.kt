package im.vector.app.kelare.network.event

import im.vector.app.kelare.network.response.DeleteContactResponse
import retrofit2.Response

/**
 * @author jsqi
 */
class DeleteContactResponseEvent: ResponseEvent<DeleteContactResponse> {

    constructor(basicResponse: DeleteContactResponse, response: Response<*>) : super(basicResponse, response) {}

    constructor(paramRetrofitError: Throwable) : super(paramRetrofitError) {}
}
