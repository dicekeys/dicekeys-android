package org.dicekeys.app.data

import kotlinx.serialization.Serializable
import org.dicekeys.api.DerivationRecipe

@Serializable
data class BackupRecipes(
    val version: Int,
    val recipes: List<DerivationRecipe>
)