package org.dicekeys.trustedapp.state

import org.dicekeys.keysqr.Face
import org.dicekeys.keysqr.FaceRead
import org.dicekeys.keysqr.KeySqr

object KeySqrState {
    private var _keySqrRead: KeySqr<FaceRead>? = null
    private var _keySqr: KeySqr<Face>? = null
    private var _canonicalKeySqr: KeySqr<Face>? = null

    val keySqrRead: KeySqr<FaceRead>? get() { return _keySqrRead
    }
    val keySqr: KeySqr<Face>? get() { return _keySqr
    }
    val canonicalKeySqr: KeySqr<Face>? get() { return _canonicalKeySqr
    }

    fun setKeySquareRead(newKeySqrRead: KeySqr<FaceRead>) {
        _keySqrRead = newKeySqrRead
        _keySqr = newKeySqrRead.rotate(0)
        _canonicalKeySqr = _keySqr?.toCanonicalRotation()
    }
    fun setKeySquareEnteredManually(newKeySqr: KeySqr<Face>) {
        _keySqrRead = null
        _keySqr = newKeySqr
        _canonicalKeySqr = _keySqr?.toCanonicalRotation()
    }
    fun clear() {
        _keySqrRead = null
        _keySqr = null
        _canonicalKeySqr = null
    }

    fun getCanonicalHumanReadableForm(): String? {
        return canonicalKeySqr?.toHumanReadableForm()
    }


}