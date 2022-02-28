package org.dicekeys.app.viewmodels

import org.dicekeys.app.TestViewModel
import org.dicekeys.app.encryption.EncryptedDiceKey
import org.dicekeys.app.encryption.EncryptedStorage
import org.dicekeys.app.repositories.DiceKeyRepository
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@RunWith(MockitoJUnitRunner::class)
class ListDiceKeysViewModelUnitTests : TestViewModel<ListDiceKeysViewModel>() {

    @Mock
    lateinit var encryptedStorage: EncryptedStorage

    @Mock
    lateinit var  diceKeyRepository: DiceKeyRepository

    @Mock
    lateinit var  encryptedDiceKey: EncryptedDiceKey


    @Before
    fun setup(){
        viewModel = ListDiceKeysViewModel(encryptedStorage, diceKeyRepository)
    }

    @Test
    fun test_remove(){
        whenever(encryptedDiceKey.keyId).thenReturn("keyId")

        viewModel.remove(encryptedDiceKey)

        // Verify that remove also called in EncryptedStorage and DiceKeyRepository
        verify(encryptedStorage).remove(encryptedDiceKey)
        verify(diceKeyRepository).remove(eq("keyId"))
    }

}