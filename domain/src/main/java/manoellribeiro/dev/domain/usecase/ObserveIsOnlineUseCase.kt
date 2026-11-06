package manoellribeiro.dev.domain.usecase

import kotlinx.coroutines.flow.Flow
import manoellribeiro.dev.domain.service.ConnectivityService

class ObserveIsOnlineUseCase(private val connectivityService: ConnectivityService) {

    operator fun invoke(): Flow<Boolean> = connectivityService.observeIsOnline()
}
