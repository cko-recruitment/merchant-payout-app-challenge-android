package com.example.androidinterview.core

import android.content.Context
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.androidinterview.screens.home.HomeViewModel
import com.example.androidinterview.screens.payout.PayoutViewModel
import com.example.androidinterview.screens.transactions.TransactionsViewModel
import manoellribeiro.dev.data.DataLayerFactory
import manoellribeiro.dev.domain.usecase.CreatePayoutUseCase
import manoellribeiro.dev.domain.usecase.GetActivityUseCase
import manoellribeiro.dev.domain.usecase.GetBalanceUseCase
import manoellribeiro.dev.domain.usecase.IsPayoutFormCompleteUseCase
import manoellribeiro.dev.domain.usecase.MaskIbanUseCase
import manoellribeiro.dev.domain.usecase.ObserveIsOnlineUseCase
import manoellribeiro.dev.domain.usecase.ParsePayoutAmountUseCase
import manoellribeiro.dev.domain.usecase.ShouldRequestBiometricUseCase
import manoellribeiro.dev.domain.usecase.ValidateIbanUseCase

/** The composition root: the only place that knows :data, and where implementations are chosen. */
class AppContainer(baseUrl: String, context: Context) {

    private val merchantRepository = DataLayerFactory.createMerchantRepository(baseUrl)
    private val deviceIdentityRepository = DataLayerFactory.createDeviceIdentityRepository(context, baseUrl)
    private val connectivityService = DataLayerFactory.createConnectivityService(context)

    val homeViewModelFactory = viewModelFactory {
        initializer {
            HomeViewModel(
                getBalance = GetBalanceUseCase(merchantRepository),
                getActivity = GetActivityUseCase(merchantRepository),
            )
        }
    }

    val transactionsViewModelFactory = viewModelFactory {
        initializer { TransactionsViewModel(GetActivityUseCase(merchantRepository)) }
    }

    val payoutViewModelFactory = viewModelFactory {
        initializer {
            PayoutViewModel(
                savedStateHandle = createSavedStateHandle(),
                isPayoutFormComplete = IsPayoutFormCompleteUseCase(),
                parsePayoutAmount = ParsePayoutAmountUseCase(),
                validateIban = ValidateIbanUseCase(),
                maskIban = MaskIbanUseCase(),
                shouldRequestBiometric = ShouldRequestBiometricUseCase(),
                createPayout = CreatePayoutUseCase(merchantRepository, deviceIdentityRepository),
                observeIsOnline = ObserveIsOnlineUseCase(connectivityService),
            )
        }
    }
}
