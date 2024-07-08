package com.julianczaja.stations.data.remote

import okhttp3.Interceptor
import okhttp3.Response

class VersionHeaderInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
            .newBuilder()
            .addHeader("X-KOLEO-Version", "1")
            .build()

        return chain.proceed(request)
    }
}
