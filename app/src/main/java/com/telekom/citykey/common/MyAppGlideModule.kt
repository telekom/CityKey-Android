package com.telekom.citykey.common

import android.content.Context
import androidx.annotation.NonNull
import com.bumptech.glide.Glide
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.module.AppGlideModule
import okhttp3.OkHttpClient
import java.io.InputStream

@GlideModule
class MyAppGlideModule : AppGlideModule() {
    override fun registerComponents(@NonNull context: Context, @NonNull glide: Glide, @NonNull registry: Registry) {
        val okHttpClient = OkHttpClient.Builder()
        // hostname Verifier is always  true because we load images from different domains
        okHttpClient.hostnameVerifier { _, _ -> true }
        registry.replace(GlideUrl::class.java, InputStream::class.java, OkHttpUrlLoader.Factory(okHttpClient.build()))
    }
}
