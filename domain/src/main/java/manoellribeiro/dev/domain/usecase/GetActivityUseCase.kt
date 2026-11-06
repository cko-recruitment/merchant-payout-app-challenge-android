package manoellribeiro.dev.domain.usecase

import manoellribeiro.dev.domain.model.ActivityPage
import manoellribeiro.dev.domain.repository.MerchantRepository

class GetActivityUseCase(private val merchantRepository: MerchantRepository) {

    /** Pass a null [cursor] for the first page, then the previous page's next cursor. */
    suspend operator fun invoke(cursor: String?, limit: Int): Result<ActivityPage> =
        merchantRepository.getActivity(cursor, limit)
}
