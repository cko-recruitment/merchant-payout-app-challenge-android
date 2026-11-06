package manoellribeiro.dev.domain.repository

import manoellribeiro.dev.domain.model.ActivityPage
import manoellribeiro.dev.domain.model.MerchantBalance
import manoellribeiro.dev.domain.model.Payout
import manoellribeiro.dev.domain.model.PayoutRequest

/** Failures are returned as [manoellribeiro.dev.domain.error.DataError]. */
interface MerchantRepository {
    suspend fun getBalance(): Result<MerchantBalance>

    /** Pass a null [cursor] for the first page, then the previous page's next cursor. */
    suspend fun getActivity(cursor: String?, limit: Int): Result<ActivityPage>

    suspend fun createPayout(request: PayoutRequest): Result<Payout>
}
