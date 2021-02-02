package org.dicekeys.app.recipes

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class DerivationRecipe(
        @SerialName("allow")
        val allow: List<Host>,

        @SerialName("#")
        val sequence: Int = 1
) {
    val id
        get() = hashCode().toString()

    override fun toString(): String {
        return Json { encodeDefaults = false }.encodeToString(this)
    }
}


@Serializable
data class Host(
        @SerialName("host")
        val host: String,
)