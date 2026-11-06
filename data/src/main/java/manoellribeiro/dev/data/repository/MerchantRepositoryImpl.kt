package manoellribeiro.dev.data.repository

import manoellribeiro.dev.data.remote.MerchantApi
import manoellribeiro.dev.data.remote.apiCall
import manoellribeiro.dev.data.remote.model.toDomain
import manoellribeiro.dev.data.remote.model.toRemote
import manoellribeiro.dev.domain.model.ActivityPage
import manoellribeiro.dev.domain.model.MerchantBalance
import manoellribeiro.dev.domain.model.Payout
import manoellribeiro.dev.domain.model.PayoutRequest
import manoellribeiro.dev.domain.repository.MerchantRepository

internal class MerchantRepositoryImpl(
    private val api: MerchantApi,
) : MerchantRepository {

    override suspend fun getBalance(): Result<MerchantBalance> = apiCall {
        api.getMerchant().toDomain()
    }

    override suspend fun getActivity(cursor: String?, limit: Int): Result<ActivityPage> = apiCall {
        api.getActivity(cursor, limit).toDomain()
    }

    override suspend fun createPayout(request: PayoutRequest): Result<Payout> = apiCall {
        api.createPayout(request.toRemote()).toDomain()
    }
}
