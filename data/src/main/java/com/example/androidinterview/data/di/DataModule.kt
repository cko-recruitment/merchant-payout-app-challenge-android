package com.example.androidinterview.data.di

import com.example.androidinterview.data.network.MerchantApi
import com.example.androidinterview.data.network.RetrofitClient
import com.example.androidinterview.data.repository.MerchantRepositoryImpl
import com.example.androidinterview.data.repository.PayoutRepositoryImpl
import com.example.androidinterview.domain.repository.MerchantRepository
import com.example.androidinterview.domain.repository.PayoutRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class DataModule {

    @Binds @Singleton abstract fun bindMerchantRepository(impl: MerchantRepositoryImpl): MerchantRepository
    @Binds @Singleton abstract fun bindPayoutRepository(impl: PayoutRepositoryImpl): PayoutRepository
}

@Module
@InstallIn(SingletonComponent::class)
internal object NetworkModule {
    @Provides @Singleton fun provideMerchantApi(): MerchantApi = RetrofitClient.merchantApi
    @Provides @Singleton @IoDispatcher fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO
}
