package org.dicekeys.app

import android.os.Build
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.*
import org.dicekeys.api.DerivationRecipe
import org.dicekeys.api.canonicalizeRecipeJson
import org.dicekeys.api.getWildcardOfRegisteredDomainFromCandidateWebUrl
import org.dicekeys.crypto.seeded.DerivationOptions

/*
 * A builder to create custom or template based Recipes
 */
class RecipeBuilder constructor(val type: DerivationOptions.Type, val scope: CoroutineScope, val template: DerivationRecipe?) {

    enum class BuildType {
        Online, Purpose, Raw, Template
    }
    val buildType = MutableLiveData(if(template == null) BuildType.Online else BuildType.Template)

    val domains = MutableLiveData("")
    val purpose = MutableLiveData("")
    val rawJson = MutableLiveData("")

    val sequence = MutableLiveData(template?.sequence?.toString() ?: "")
    val lengthInChars : MutableLiveData<String> = MutableLiveData(template?.lengthInChars?.toString() ?: "")
    val lengthInBytes : MutableLiveData<String> = MutableLiveData(template?.lengthInBytes?.toString() ?: "")

    // used only in raw json
    var name = MutableLiveData("")

    val derivationRecipeLiveData = MutableLiveData<DerivationRecipe?>()

    init {

        // Listen for changes on all LiveData objects
        listOf(buildType, domains, purpose, name, sequence, lengthInChars, lengthInBytes)
            .map {
                it.asFlow()
                .drop(1) // drop initial value
                .filterNotNull()
                .onEach {
                    build()
                }.launchIn(scope)
            }

        rawJson.asFlow()
            .onEach {
                val waitForSequenceToBeUpdated :Boolean = try {
                    // update sequence when rawJson if changed
                    Json.parseToJsonElement(rawJson.value!!).jsonObject["#"]?.jsonPrimitive?.intOrNull?.let { sequenceFromRaw ->
                        if (sequence.value != sequenceFromRaw.toString()) {
                            sequence.value = sequenceFromRaw.toString()
                            true // Skip building as we want first to have sequence be updated
                        }else{
                            false
                        }
                    }
                    false
                } catch (e: Exception) { false }

                if(!waitForSequenceToBeUpdated){
                    build()
                }


            }.launchIn(scope)

        build()
    }

    fun sequenceUp(){
        updateSequence( (sequence.value?.toIntOrNull() ?: 1) + 1)
    }

    fun sequenceDown(){
        updateSequence( (sequence.value?.toIntOrNull() ?: 2) - 1)
    }

    fun updateSequence(s: Int){
        if(s > 0) {
            sequence.postValue(s.toString())
        }
    }

    fun setEditRawJson(isEditRawJson: Boolean){
        if(isEditRawJson) {
            rawJson.value = getDerivationRecipe()?.recipeJson ?:  "{}"
            name.value = getDerivationRecipe()?.name ?: ""
            buildType.value = BuildType.Raw
        }else{
            buildType.value = if(template == null) if(domains.value.isNullOrBlank() && !purpose.value.isNullOrBlank()) BuildType.Purpose else BuildType.Online else BuildType.Template
        }
    }

    fun reset() {
        domains.value = ""
        purpose.value = ""
        sequence.value = ""
        lengthInChars.value = ""
        lengthInBytes.value = ""
    }

    private fun getDomainList(): List<String> {
        return domains.value
            ?.split(",")
            ?.map { it -> it.replace(" ", "") } // remove all whitespace
            ?.map { it -> it.trim { it == '.' || it == '/' } } // remove leading and trailing chars
            ?.filter { it.isNotBlank() }
            ?.map { urlOrDomain ->
                getWildcardOfRegisteredDomainFromCandidateWebUrl(urlOrDomain) ?: urlOrDomain
            }
            ?.distinct() // prevent duplicate unique values
            ?.sorted() ?: listOf()
    }

    fun build(): DerivationRecipe? {
        return (try {

            val sequenceNumber = try {
                1.coerceAtLeast(sequence.value!!.toInt())
            } catch (e: Exception){
                1
            }

            val lengthInChars = try {
                val i = lengthInChars.value!!.toInt()
                if (i in 8..999) i else 0
            } catch (e: Exception){
                0
            }

            val lengthInBytes = try {
                val i = lengthInBytes.value!!.toInt()
                if (i in 16..999) i else 0
            } catch (e: Exception){
                0
            }

            when(buildType.value!!){
                BuildType.Online -> {
                    DerivationRecipe.createCustomOnlineRecipe(
                        type = type,
                        domains = getDomainList(),
                        sequenceNumber = sequenceNumber,
                        lengthInChars = lengthInChars,
                        lengthInBytes = lengthInBytes
                    )
                }
                BuildType.Purpose -> {
                    DerivationRecipe.createCustomPurposeRecipe(
                        type = type,
                        purpose = purpose.value ?: "",
                        sequenceNumber = sequenceNumber,
                        lengthInChars = lengthInChars,
                        lengthInBytes = lengthInBytes
                    )
                }
                BuildType.Raw -> {
                    DerivationRecipe.createCustomRawJsonRecipe(
                        type = type,
                        name = name.value,
                        rawJson = rawJson.value ?: "{}",
                        sequenceNumber = sequenceNumber
                    )?.also {

                        scope.launch {

                            // run in in IO Dispatcher for performance reasons
                            val canonicalized = withContext(Dispatchers.IO) {
                                canonicalizeRecipeJson(rawJson.value)
                            }
                            // update rawJson if it different from the recipe
                            if(canonicalized != null && it.recipeJson != canonicalized){
                                rawJson.value = it.recipeJson
                            }

                            try {
                                // update sequence from raw json
                                Json.parseToJsonElement(rawJson.value!!).jsonObject["#"]?.jsonPrimitive?.intOrNull?.let { sequenceFromRaw ->
                                    if (sequence.value != sequenceFromRaw.toString()) {
                                        sequence.value = sequenceFromRaw.toString()
                                    }
                                }
                            } catch (e: Exception) { }
                        }
                    }
                }
                BuildType.Template -> {
                    DerivationRecipe.createRecipeFromTemplate(
                        template = template!!,
                        sequenceNumber = sequenceNumber,
                        lengthInChars = lengthInChars,
                        lengthInBytes = lengthInBytes
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }).also {
            derivationRecipeLiveData.value = it
        }
    }

    @JvmName("getDerivationRecipeValue")
    fun getDerivationRecipe(): DerivationRecipe? = derivationRecipeLiveData.value
}