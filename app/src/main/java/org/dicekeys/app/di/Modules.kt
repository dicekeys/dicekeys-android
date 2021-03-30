package org.dicekeys.app.di

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.dicekeys.app.AppLifecycleObserver
import org.dicekeys.app.encryption.AppKeystore
import org.dicekeys.app.encryption.BiometricsHelper
import org.dicekeys.app.encryption.EncryptedStorage
import org.dicekeys.app.repositories.DiceKeyRepository
import org.dicekeys.app.repositories.RecipeRepository
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
    fun provideRecipesRepository(@ApplicationContext context: Context): RecipeRepository {
        return RecipeRepository(context.getSharedPreferences("recipes", Context.MODE_PRIVATE))
    }

    @Singleton
    @Provides
    fun provideBiometricsHelper(appKeystore: AppKeystore, encryptedStorage: EncryptedStorage): BiometricsHelper {
        return BiometricsHelper(appKeystore, encryptedStorage)
    }

    @Singleton
    @Provides
    fun provideDiceKeyRepository(sharedPreferences: SharedPreferences): DiceKeyRepository {
        return DiceKeyRepository(sharedPreferences)
    }

    @Singleton
    @Provides
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(context)
    }

    @Singleton
    @Provides
    fun provideAppLifecycleObserver(@ApplicationContext context: Context, diceKeyRepository: DiceKeyRepository): AppLifecycleObserver {
        return AppLifecycleObserver(context, diceKeyRepository)
    }
}