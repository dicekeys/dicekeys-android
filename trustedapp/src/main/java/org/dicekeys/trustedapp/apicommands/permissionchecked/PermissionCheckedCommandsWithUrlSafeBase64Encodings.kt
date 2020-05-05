package org.dicekeys.trustedapp.apicommands.permissionchecked

import android.util.Base64

abstract class PermissionCheckedCommandsWithUrlSafeBase64Encodings(
  private val permissionCheckedSeedAccessor: PermissionCheckedSeedAccessor
) : PermissionCheckedMarshalledCommands(permissionCheckedSeedAccessor) {

  override fun unmarshallBinaryParameter(parameterName: String): ByteArray? =
    unmarshallStringParameter(parameterName)?.let{
      Base64.decode(unmarshallRequiredStringParameter(parameterName), Base64.URL_SAFE)
    }

  override fun marshallResult(responseParameterName: String, value: ByteArray): PermissionCheckedMarshalledCommands {
    marshallResult(responseParameterName, Base64.encodeToString(value, Base64.URL_SAFE))
    return this
  }

}