package com.example.dashcarr.di.modules

import com.example.dashcarr.data.repository.FirebaseAuthRepository
import com.example.dashcarr.domain.repository.IFirebaseAuthRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module
interface AuthRepositoryModule {
    @Binds
    fun bindAuthRepository(repository: FirebaseAuthRepository): IFirebaseAuthRepository
}