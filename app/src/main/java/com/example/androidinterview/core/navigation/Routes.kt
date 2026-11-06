package com.example.androidinterview.core.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

// Serializable so rememberNavBackStack can restore the back stack after process death.
@Serializable
data object HomeRoute : NavKey

@Serializable
data object TransactionsRoute : NavKey

@Serializable
data object PayoutsRoute : NavKey
