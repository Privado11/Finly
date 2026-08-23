package co.privado.finly.domain.repository

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<Unit>
    suspend fun signUp(email: String, password: String, firstName: String, lastName: String): Result<Unit>
    suspend fun signInWithGoogle(idToken: String): Result<Unit>
    suspend fun signOut()
    suspend fun currentUserId(): String?
    suspend fun isSessionValid(): Boolean
}
