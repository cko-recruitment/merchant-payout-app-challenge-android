package manoellribeiro.dev.data.remote

import manoellribeiro.dev.data.remote.model.RemoteActivityPage
import manoellribeiro.dev.data.remote.model.RemoteDevice
import manoellribeiro.dev.data.remote.model.RemoteMerchantResponse
import manoellribeiro.dev.data.remote.model.RemotePayoutRequest
import manoellribeiro.dev.data.remote.model.RemotePayoutResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

internal interface MerchantApi {
    @GET("api/merchant")
    suspend fun getMerchant(): RemoteMerchantResponse

    /** Retrofit leaves out the cursor parameter when it's null. */
    @GET("api/merchant/activity")
    suspend fun getActivity(
        @Query("cursor") cursor: String?,
        @Query("limit") limit: Int,
    ): RemoteActivityPage

    @POST("api/payouts")
    suspend fun createPayout(@Body request: RemotePayoutRequest): RemotePayoutResponse

    @GET("api/devices")
    suspend fun getDevice(): RemoteDevice
}
