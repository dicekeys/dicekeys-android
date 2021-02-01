package org.dicekeys.app.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.dicekeys.app.encryption.AppKeystore
import org.dicekeys.app.encryption.BiometricsHelper
import org.dicekeys.app.encryption.EncryptedStorage
import org.dicekeys.app.repositories.DiceKeyRepository
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class Modules {

    @Singleton
    @Provides
    fun provideAppKeystore(): AppKeystore {
        return AppKeystore()
    }

    @Singleton
    @Provides
    fun provideEncryptedStorage(@ApplicationContext context: Context): EncryptedStorage {
        return EncryptedStorage(context.getSharedPreferences("dice_keys", Context.MODE_PRIVATE))
    }

    @Singleton
    @Provides
    fun provideBiometricsHelper(appKeystore: AppKeystore, encryptedStorage: EncryptedStorage): BiometricsHelper {
        return BiometricsHelper(appKeystore, encryptedStorage)
    }

    @Singleton
    @Provides
    fun provideDiceKeyRepository(): DiceKeyRepository {
        return DiceKeyRepository()
    }
}