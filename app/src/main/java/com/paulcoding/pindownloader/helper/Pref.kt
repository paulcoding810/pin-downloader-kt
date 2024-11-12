package com.paulcoding.pindownloader.helper

import com.tencent.mmkv.MMKV
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update


const val IS_PREMIUM = "is_premium"

object AppPreference {
    private val kv: MMKV = MMKV.defaultMMKV()

    private var _isPremium = MutableStateFlow(kv.getBoolean(IS_PREMIUM, true))
    val isPremium = _isPremium.asStateFlow()

    fun setIsPremium(premium: Boolean) {
        _isPremium.update { premium }
        kv.putBoolean(IS_PREMIUM, premium)
    }
}