package org.dicekeys.app.repositories

import com.nhaarman.mockitokotlin2.whenever
import org.dicekeys.app.repositories.DiceKeyRepository
import org.dicekeys.dicekey.DiceKey
import org.dicekeys.dicekey.Face
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class DiceKeyRepositoryUnitTests {

    private lateinit var repo: DiceKeyRepository

    @Mock
    private lateinit var diceKey: DiceKey<Face>

    @Before
    fun setup() {
        repo = DiceKeyRepository()

        whenever(diceKey.keyId).thenReturn("keyId")
    }

    @Test
    fun onInit_sizeShouldBe_zero() {
        assertEquals(0, repo.size())
    }

    @Test
    fun test_setter_getter() {
        repo.set(diceKey)

        assertTrue(repo.exists(diceKey))
        assertTrue(repo.exists(diceKey.keyId))

        val diceKeyFromRepo = repo.get(diceKey.keyId)

        assertEquals(diceKey, diceKeyFromRepo)
    }

    @Test
    fun test_nonExistantDiceKey() {
        assertFalse(repo.exists("random-key"))
    }

    @Test
    fun whenCleared_sizeShouldBe_zero() {
        repo.set(diceKey)

        repo.clear()

        assertFalse(repo.exists(diceKey))
        assertFalse(repo.exists(diceKey.keyId))

        assertEquals(0, repo.size())
    }

}