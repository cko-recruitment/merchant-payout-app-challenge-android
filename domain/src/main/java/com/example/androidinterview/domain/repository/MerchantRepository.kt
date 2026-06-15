package com.example.androidinterview.domain.repository

import com.example.androidinterview.domain.model.ActivityPage
import com.example.androidinterview.domain.model.Merchant

interface MerchantRepository {
    suspend fun getMerchant(): Result<Merchant>
    suspend fun getActivity(cursor: String? = null, limit: Int = 15): Result<ActivityPage>
}
