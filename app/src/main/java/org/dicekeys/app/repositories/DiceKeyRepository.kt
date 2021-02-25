package org.dicekeys.app.repositories

import org.dicekeys.app.encryption.EncryptedDiceKey
import org.dicekeys.dicekey.DiceKey
import org.dicekeys.dicekey.Face

/*
 * DiceKeyRepository
 *
 * A Repository handling in-memory DiceKeys.
 * The app currently allows only one unlocked (in-memory) DiceKey, the repo can handle multiple.
 *
 */

class DiceKeyRepository {
    private var diceKeys = mutableMapOf<String, DiceKey<Face>>()

    fun exists(encryptedDiceKey: EncryptedDiceKey) = exists(encryptedDiceKey.keyId)
    fun exists(diceKey: DiceKey<*>) = exists(diceKey.keyId)
    fun exists(keyId: String) = diceKeys.containsKey(keyId)

    fun set(diceKey: DiceKey<Face>) {
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

    // Get the Active DiceKey, currently the app allows only one unlocked dicekey, so just pick the first
    fun getActiveDiceKey() = if(diceKeys.isEmpty()) null else diceKeys[diceKeys.keys.first()]

    fun size() = diceKeys.size
}