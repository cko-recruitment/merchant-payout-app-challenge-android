package manoellribeiro.dev.domain.usecase

import manoellribeiro.dev.domain.model.MerchantBalance
import manoellribeiro.dev.domain.repository.MerchantRepository

class GetBalanceUseCase(private val merchantRepository: MerchantRepository) {

    suspend operator fun invoke(): Result<MerchantBalance> = merchantRepository.getBalance()
}
