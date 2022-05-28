package org.dicekeys.app.bip39


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.dicekeys.app.extensions.fromHex
import org.junit.Assert
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.BlockJUnit4ClassRunner
import java.io.File

@RunWith(BlockJUnit4ClassRunner::class)
class MnemonicsUnitTest {

    lateinit var dataSet : BIP39TestDataSet

    @Before
    fun init(){
        val json = File("src/test/resources/data/BIP-0039-test-values.json").readText(Charsets.UTF_8)
        dataSet = Json.decodeFromString(json)
    }


    @Test
    fun testDataSet(){
        Assert.assertNotNull(dataSet)

        for(testSet in dataSet.values){
            Assert.assertEquals(testSet[1], Mnemonics.MnemonicCode(testSet[0].fromHex()).mnemonic)
        }
    }
}

@Serializable
data class BIP39TestDataSet constructor(
    @SerialName("english")
    val values: List<List<String>>
)