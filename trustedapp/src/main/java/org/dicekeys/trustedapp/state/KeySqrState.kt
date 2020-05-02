package org.dicekeys.trustedapp.state

import kotlinx.coroutines.*
import org.dicekeys.keysqr.DiceKey
import org.dicekeys.keysqr.Face
import org.dicekeys.keysqr.FaceRead
import org.dicekeys.keysqr.KeySqr

object KeySqrState {
  private var _diceKeyRead: KeySqr<FaceRead>? = null
  private var _diceKey: DiceKey? = null

  val diceKeyRead: KeySqr<FaceRead>? get() { return _diceKeyRead
  }
  val diceKey: DiceKey? get() { return _diceKey
  }

  var deferredLoadDiceKey: Deferred<DiceKey>? = null
  suspend fun getDiceKey(
    loadDiceKey: () -> Deferred<DiceKey>
  ): DiceKey =
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

  fun setDiceKeyRead(newKeySqrRead: KeySqr<FaceRead>) {
    _diceKeyRead = newKeySqrRead
    _diceKey = DiceKey(newKeySqrRead.faces)
  }
  fun setDiceKey(newDiceKey: DiceKey) {
    _diceKeyRead = null
    _diceKey = newDiceKey
  }
  fun clear() {
    _diceKeyRead = null
    _diceKey = null
  }

}
