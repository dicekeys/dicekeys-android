package org.dicekeys.keys

import android.util.Base64
import com.squareup.moshi.*

internal
class Base64Adapter {
    @FromJson
    fun fromJson(s: String): ByteArray {
        return Base64.decode(s, Base64.NO_WRAP)
    }

    @ToJson
    fun toJson(byteArray: ByteArray): String
    {
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }
}
