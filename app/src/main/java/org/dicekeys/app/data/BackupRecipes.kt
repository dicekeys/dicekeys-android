package org.dicekeys.app.data

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.dicekeys.api.DerivationRecipe

@Serializable
data class BackupRecipes constructor(
    @SerialName("version")
    val version: Int? = null,

    @SerialName("createdAt")
    val createdAt: Instant? = null,

    @SerialName("recipes")
    val recipes: List<DerivationRecipe>,
) {

    override fun toString(): String = Json.encodeToString(this)

    companion object {
        fun create(recipes: List<DerivationRecipe>) = BackupRecipes(
                version = 1,
                createdAt = Clock.System.now(),
                recipes = recipes
            )
    }
}