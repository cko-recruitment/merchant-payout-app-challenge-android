package manoellribeiro.dev.domain.usecase

import manoellribeiro.dev.domain.model.Money
import manoellribeiro.dev.domain.model.Payout
import manoellribeiro.dev.domain.model.PayoutRequest
import manoellribeiro.dev.domain.repository.DeviceIdentityRepository
import manoellribeiro.dev.domain.repository.MerchantRepository

class CreatePayoutUseCase(
    private val merchantRepository: MerchantRepository,
    private val deviceIdentityRepository: DeviceIdentityRepository,
) {
    suspend operator fun invoke(amount: Money, iban: String): Result<Payout> {
        val deviceId = deviceIdentityRepository.getDeviceId().getOrElse { return Result.failure(it) }
        return merchantRepository.createPayout(PayoutRequest(amount, iban, deviceId))
    }
}
