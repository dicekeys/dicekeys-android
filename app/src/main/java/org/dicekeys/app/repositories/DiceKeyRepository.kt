package org.dicekeys.app.repositories

import org.dicekeys.dicekey.DiceKey

/*
    A Repo to store DiceKeys. DiceKeys can be only in memory or backed by encrypted storage
 */
class DiceKeyRepository {
    private var diceKeys = mutableMapOf<String, DiceKey<*>>()

    fun exists(dice: DiceKey<*>) = exists(dice.keyId)
    fun exists(diceId: String) = diceKeys.containsKey(diceId)

    fun set(dice: DiceKey<*>) {
        diceKeys[dice.keyId] = dice
    }

    fun get(diceId: String) = diceKeys[diceId]
}