package org.dicekeys.app.viewmodels

import org.dicekeys.app.TestViewModel
import org.dicekeys.app.encryption.EncryptedStorage
import org.dicekeys.app.repositories.DiceKeyRepository
import org.dicekeys.dicekey.DiceKey
import org.dicekeys.dicekey.Face
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@RunWith(MockitoJUnitRunner::class)
class DiceKeyViewModelUnitTests : TestViewModel<DiceKeyViewModel>() {

    @Mock
    lateinit var encryptedStorage: EncryptedStorage

    @Mock
    lateinit var  diceKeyRepository: DiceKeyRepository

    @Mock
    lateinit var  diceKey: DiceKey<Face>

    @Before
    fun setup(){
        whenever(encryptedStorage.getDiceKeysLiveData()).thenReturn(mock())
        whenever(diceKeyRepository.getActiveDiceKey()).thenReturn(diceKey)


        viewModel = DiceKeyViewModel(encryptedStorage, diceKeyRepository)
    }

    @Test
    fun testForget(){
        viewModel.forget()

        // verify DiceKey is removed from DiceKeyRepository
        verify(diceKeyRepository).remove(eq(diceKey))
    }

    @Test
    fun testRemove(){
        viewModel.remove()

        // Verify that remove also called in EncryptedStorage and DiceKeyRepository
        verify(encryptedStorage).remove(diceKey)
    }

}