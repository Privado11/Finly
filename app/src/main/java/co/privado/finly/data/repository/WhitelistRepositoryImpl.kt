package co.privado.finly.data.repository

import co.privado.finly.domain.model.AllowedApp
import co.privado.finly.domain.repository.WhitelistRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WhitelistRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClient
) : WhitelistRepository {
    override suspend fun isAllowed(packageName: String): Boolean {
        return try {
            val list = supabase.from("allowed_apps").select {
                filter { eq("package_name", packageName); eq("active", true) }
            }.decodeList<AllowedApp>()
            list.isNotEmpty()
        } catch (_: Exception) { false }
    }
    override suspend fun getWhitelist(): Result<List<AllowedApp>> = runCatching {
        supabase.from("allowed_apps").select().decodeList()
    }
    override suspend fun setAllowed(packageName: String, displayName: String, active: Boolean): Result<Unit> = runCatching {
        val userId = supabase.auth.currentUserOrNull()?.id
            ?: throw IllegalStateException("Tu sesión expiró. Inicia sesión nuevamente.")
        supabase.from("allowed_apps").upsert(
            AllowedApp(userId = userId, packageName = packageName, displayName = displayName, active = active)
        )
        Unit
    }
    override suspend fun remove(packageName: String): Result<Unit> = runCatching {
        supabase.from("allowed_apps").delete { filter { eq("package_name", packageName) } }
        Unit
    }
}
