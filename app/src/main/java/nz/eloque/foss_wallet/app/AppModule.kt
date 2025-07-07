package nz.eloque.foss_wallet.app

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import androidx.work.WorkManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import nz.eloque.foss_wallet.persistence.ChecklistDao
import nz.eloque.foss_wallet.persistence.WalletDb
import nz.eloque.foss_wallet.persistence.checklist.ChecklistRepository
import nz.eloque.foss_wallet.persistence.localization.PassLocalizationDao
import nz.eloque.foss_wallet.persistence.localization.PassLocalizationRepository
import nz.eloque.foss_wallet.persistence.pass.PassDao
import nz.eloque.foss_wallet.persistence.pass.PassRepository


@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    fun provideLocalizationRepository(localizationDao: PassLocalizationDao): PassLocalizationRepository {
        return PassLocalizationRepository(localizationDao)
    }

    @Provides
    fun providePassRepository(@ApplicationContext context: Context, passDao: PassDao): PassRepository {
        return PassRepository(context, passDao)
    }

    @Provides
    fun provideChecklistRepository(checklistDao: ChecklistDao): ChecklistRepository {
        return ChecklistRepository(checklistDao)
    }

    @Provides
    fun provideWalletDb(@ApplicationContext context: Context): WalletDb {
        return WalletDb.getDb(context)
    }

    @Provides
    fun providePassDao(walletDb: WalletDb): PassDao {
        return walletDb.passDao()
    }

    @Provides
    fun provideLocalizationDAo(walletDb: WalletDb): PassLocalizationDao {
        return walletDb.localizationDao()
    }

    @Provides
    fun provideChecklistDao(walletDb: WalletDb): ChecklistDao {
        return walletDb.checklistDao()
    }

    @Provides
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager {
        return WorkManager.getInstance(context)
    }

    @Provides
    fun provideSharedPrefs(@ApplicationContext context: Context): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(context)!!
    }
}
