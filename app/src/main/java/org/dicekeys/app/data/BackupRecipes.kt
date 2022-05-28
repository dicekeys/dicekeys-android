package org.dicekeys.app.data

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.dicekeys.api.DerivationRecipe

// Currently unused as support for this filestructure should be added to the other clients.
@Serializable
data class BackupRecipes constructor(
    val version: Int? = null,
    val createdAt: Instant? = null,
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