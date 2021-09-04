package org.dicekeys.read

import org.dicekeys.dicekey.DiceKey
import org.junit.Test

class DiceKeyUnitTest {
    @Test
    fun test_humanReadableFormWithOrientation() {
        DiceKey.fromHumanReadableForm("A1tA1tA1tA1tA1tA1tA1tA1tA1tA1tA1tA1tA1tA1tA1tA1tA1tA1tA1tA1tA1tA1tA1tA1tA1t")
    }

    @Test
    fun test_humanReadableFormWithoutOrientation() {
        DiceKey.fromHumanReadableForm("A1A1A1A1A1A1A1A1A1A1A1A1A1A1A1A1A1A1A1A1A1A1A1A1A1")
    }
}