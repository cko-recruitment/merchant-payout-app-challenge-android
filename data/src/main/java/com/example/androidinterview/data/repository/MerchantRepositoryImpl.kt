package com.example.androidinterview.data.repository

import com.example.androidinterview.data.di.IoDispatcher
import com.example.androidinterview.data.model.toDomain
import com.example.androidinterview.data.network.MerchantApi
import com.example.androidinterview.domain.model.ActivityPage
import com.example.androidinterview.domain.model.Merchant
import com.example.androidinterview.domain.repository.MerchantRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

internal class MerchantRepositoryImpl @Inject constructor(
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val api: MerchantApi,
) : MerchantRepository {

    override suspend fun getMerchant(): Result<Merchant> = withContext(ioDispatcher) {
        runCatching { api.getMerchant().toDomain() }
    }

    override suspend fun getActivity(cursor: String?, limit: Int): Result<ActivityPage> =
        withContext(ioDispatcher) {
            runCatching { api.getActivity(cursor, limit).toDomain() }
        }
}
