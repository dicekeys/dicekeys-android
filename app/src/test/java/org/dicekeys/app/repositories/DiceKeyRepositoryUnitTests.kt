package org.dicekeys.app.repositories


import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import org.dicekeys.app.encryption.EncryptedDiceKey
import org.dicekeys.app.encryption.EncryptedStorage
import org.dicekeys.dicekey.DiceKey
import org.dicekeys.dicekey.Face
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@RunWith(MockitoJUnitRunner::class)
class DiceKeyRepositoryUnitTests {

    private lateinit var repo: DiceKeyRepository

    @Mock
    lateinit var encryptedStorage: EncryptedStorage

    @Mock
    private lateinit var diceKey: DiceKey<Face>

    @get:Rule
    val taskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setup() {
        whenever(encryptedStorage.getDiceKeysLiveData()).thenReturn(mock())

        repo = DiceKeyRepository(mock(), encryptedStorage)

        whenever(diceKey.keyId).thenReturn("keyId")
        whenever(diceKey.centerFace()).thenReturn(Face.fromHumanReadableForm("A1t"))
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