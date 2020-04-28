package org.dicekeys.trustedapp.apicommands.permissionchecked

import android.util.Base64

abstract class PermissionCheckedCommandsWithUrlSafeBase64Encodings(
  private val permissionCheckedSeedAccessor: PermissionCheckedSeedAccessor
) : PermissionCheckedMarshalledCommands(permissionCheckedSeedAccessor) {

  override fun binaryParameter(parameterName: String): ByteArray? =
    stringParameter(parameterName)?.let{
      Base64.decode(requiredStringParameter(parameterName), Base64.URL_SAFE)
    }

  override fun respondWith(responseParameterName: String, value: ByteArray): PermissionCheckedMarshalledCommands {
    respondWith(responseParameterName, Base64.encodeToString(value, Base64.URL_SAFE))
    return this
  }

}