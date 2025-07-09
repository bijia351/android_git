package com.anguomob.git.data.api

import com.anguomob.git.BuildConfig
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val builder = originalRequest.newBuilder()

        if (originalRequest.url.host.contains("api.github.com")) {
            builder.addHeader("Authorization", "Bearer ${BuildConfig.GITHUB_API_TOKEN}")
        }

        val newRequest = builder.build()
        return chain.proceed(newRequest)
    }
}
