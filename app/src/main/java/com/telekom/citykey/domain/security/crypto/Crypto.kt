package com.telekom.citykey.domain.security.crypto

import android.content.Context
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import timber.log.Timber

class Crypto(val context: Context) {

    private val prefs = EncryptedSharedPreferences.create(
        "secrets",
        MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun store(kvPair: Pair<String, String>) {
        prefs.edit {
            Timber.i(kvPair.toString())
            putString(kvPair.first, kvPair.second)
        }
    }

    fun get(key: String) = prefs.getString(key, null).also(Timber::i)

    fun remove(keys: List<String>) {
        prefs.edit {
            keys.forEach(this::remove)
        }
    }
}
