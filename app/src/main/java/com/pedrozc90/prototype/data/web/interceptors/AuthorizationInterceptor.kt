package com.pedrozc90.prototype.data.web.interceptors

import com.pedrozc90.prototype.data.local.PreferencesRepository
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Interceptor that adds Authorization: Bearer $token when token exists.
 *
 * It calls the TokenRepository.getToken() using runBlocking because interceptors are synchronous.
 * This is safe because OkHttp executes interceptors on a background thread. Prefer caching token in memory
 * if you want to avoid any potential blocking on every request.
 */
class AuthorizationInterceptor(private val preferences: PreferencesRepository) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = preferences.token

        val original = chain.request()
        val request = if (!token.isNullOrBlank()) {
            original.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            original
        }

        return chain.proceed(request)
    }

}
