package org.dicekeys.app.repositories

import org.dicekeys.app.encryption.EncryptedDiceKey
import org.dicekeys.dicekey.DiceKey

/*
    A Repo to store DiceKeys. DiceKeys can be only in memory or backed by encrypted storage
 */
class DiceKeyRepository {
    private var diceKeys = mutableMapOf<String, DiceKey<*>>()

    fun exists(encryptedDiceKey: EncryptedDiceKey) = exists(encryptedDiceKey.keyId)
    fun exists(diceKey: DiceKey<*>) = exists(diceKey.keyId)
    fun exists(keyId: String) = diceKeys.containsKey(keyId)

    fun set(diceKey: DiceKey<*>) {
        diceKeys[diceKey.keyId] = diceKey
    }

    fun get(keyId: String) = diceKeys[keyId]

    fun remove(diceKey: DiceKey<*>) = remove(diceKey.keyId)
    fun remove(keyId: String){
        diceKeys.remove(keyId)
    }

    fun clear(){
        diceKeys.clear()
    }
}