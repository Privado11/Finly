package co.privado.finly.domain.repository

import co.privado.finly.domain.model.AllowedApp

interface WhitelistRepository {
    suspend fun isAllowed(packageName: String): Boolean
    suspend fun getWhitelist(): Result<List<AllowedApp>>
    suspend fun setAllowed(packageName: String, displayName: String, active: Boolean): Result<Unit>
    suspend fun remove(packageName: String): Result<Unit>
}
