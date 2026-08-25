package co.privado.finly.di

import co.privado.finly.BuildConfig
import co.privado.finly.data.local.ColaOfflineDataSource
import co.privado.finly.data.local.SessionDataStore
import co.privado.finly.data.repository.AuthRepositoryImpl
import co.privado.finly.data.repository.AccountRepositoryImpl
import co.privado.finly.data.repository.CategoryRepositoryImpl
import co.privado.finly.data.repository.TransactionRepositoryImpl
import co.privado.finly.data.repository.ProcesadorNotificaciones
import co.privado.finly.domain.repository.AuthRepository
import co.privado.finly.service.NotificadorApp
import co.privado.finly.util.ConectividadHelper
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.functions.Functions
import io.github.jan.supabase.realtime.Realtime
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SupabaseModule {

    @Provides
    @Singleton
    fun provideSupabaseClient(): SupabaseClient {
        return createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_ANON_KEY
        ) {
            install(Auth)
            install(Postgrest)
            install(Functions)
            install(Realtime)
        }
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindAccountRepository(impl: AccountRepositoryImpl): co.privado.finly.domain.repository.AccountRepository

    @Binds
    @Singleton
    abstract fun bindCategoryRepository(impl: CategoryRepositoryImpl): co.privado.finly.domain.repository.CategoryRepository

    @Binds
    @Singleton
    abstract fun bindTransactionRepository(impl: TransactionRepositoryImpl): co.privado.finly.domain.repository.TransactionRepository

    @Binds
    @Singleton
    abstract fun bindWhitelistRepository(impl: co.privado.finly.data.repository.WhitelistRepositoryImpl): co.privado.finly.domain.repository.WhitelistRepository
}
