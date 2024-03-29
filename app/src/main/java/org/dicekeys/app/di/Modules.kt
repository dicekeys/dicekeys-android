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
import org.dicekeys.app.data.BackupManager
import org.dicekeys.app.encryption.AppKeyStore
import org.dicekeys.app.encryption.BiometricsHelper
import org.dicekeys.app.encryption.EncryptedStorage
import org.dicekeys.app.migrations.Migrator
import org.dicekeys.app.repositories.DiceKeyRepository
import org.dicekeys.app.repositories.RecipeRepository
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class Modules {

    @Singleton
    @Provides
    fun provideAppKeystore(): AppKeyStore {
        return AppKeyStore()
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
    fun provideBiometricsHelper(appKeyStore: AppKeyStore, encryptedStorage: EncryptedStorage): BiometricsHelper {
        return BiometricsHelper(appKeyStore, encryptedStorage)
    }

    @Singleton
    @Provides
    fun provideDiceKeyRepository(sharedPreferences: SharedPreferences, encryptedStorage: EncryptedStorage): DiceKeyRepository {
        return DiceKeyRepository(sharedPreferences, encryptedStorage)
    }

    @Singleton
    @Provides
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(context)
    }

    @Singleton
    @Provides
    fun provideAppLifecycleObserver(sharedPreferences: SharedPreferences, diceKeyRepository: DiceKeyRepository): AppLifecycleObserver {
        return AppLifecycleObserver(sharedPreferences, diceKeyRepository)
    }

    @Singleton
    @Provides
    fun provideMigrator(@ApplicationContext context: Context, sharedPreferences: SharedPreferences): Migrator {
        return Migrator(context, sharedPreferences)
    }

    @Singleton
    @Provides
    fun provideBackupManager(@ApplicationContext context: Context, recipeRepository :RecipeRepository): BackupManager {
        return BackupManager(context, recipeRepository)
    }
}