package org.dicekeys.trustedapp.state

import kotlinx.coroutines.*
import org.dicekeys.dicekey.DiceKey
import org.dicekeys.dicekey.FaceRead
import org.dicekeys.dicekey.SimpleDiceKey

object DiceKeyState {
  private var _diceKeyRead: DiceKey<FaceRead>? = null
  private var _diceKey: SimpleDiceKey? = null

  val diceKeyRead: DiceKey<FaceRead>? get() { return _diceKeyRead
  }
  val diceKey: SimpleDiceKey? get() { return _diceKey
  }

  var deferredLoadDiceKey: Deferred<SimpleDiceKey>? = null
  suspend fun getDiceKey(
    loadDiceKey: () -> Deferred<SimpleDiceKey>
  ): SimpleDiceKey =
    // If the diceKey is already loaded, return it.
    _diceKey ?:
    // If  a load has already been set underway, wait for it
    deferredLoadDiceKey?.await() ?:
    // Kick of the loading of a diceKey, and...
    loadDiceKey().apply {
      // before we start waiting for it, safe the deferral
      // so that if getDiceKey gets called again while we're
      // waiting for the DiceKey to load, we can wait on
      // this request rather than starting another.
      deferredLoadDiceKey = this
    }.await().apply {
      setDiceKey(this)
    }

  fun setDiceKeyRead(newDiceKeyRead: DiceKey<FaceRead>) {
    _diceKeyRead = newDiceKeyRead
    _diceKey = DiceKey(newDiceKeyRead.faces)
  }
  fun setDiceKey(newDiceKey: SimpleDiceKey) {
    _diceKeyRead = null
    _diceKey = newDiceKey
  }
  fun clear() {
    _diceKeyRead = null
    _diceKey = null
  }

}
