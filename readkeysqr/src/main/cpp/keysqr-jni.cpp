#include <string>
#include <jni.h>
#include "./read-keysqr/lib-keysqr/lib-keysqr.hpp"

extern "C" {

JNIEXPORT jbyteArray JNICALL Java_com_keysqr_KeySqrKt_keySqrGetSeed(
     JNIEnv* env,
     jobject obj,
     jstring keySqrInHumanReadableFormWithOrientationsObj,
     jstring jsonKeyDerivationOptionsObj,
     jstring clientsApplicationIdObj
 ) {
	const std::string keySqrInHumanReadableFormWithOrientations(
			env->GetStringUTFChars( keySqrInHumanReadableFormWithOrientationsObj, NULL )
	);
	const std::string jsonKeyDerivationOptions(
			env->GetStringUTFChars( jsonKeyDerivationOptionsObj, NULL )
	);
	const std::string clientsApplicationId(
			env->GetStringUTFChars( clientsApplicationIdObj, NULL )
	);
	const KeySqrFromString keySqr(keySqrInHumanReadableFormWithOrientations);
	Seed seed(keySqr, jsonKeyDerivationOptions, clientsApplicationId);
	const auto seedBuffer = seed.reveal();
 	jbyteArray ret = env->NewByteArray(seedBuffer.length);
	env->SetByteArrayRegion(ret, 0, seedBuffer.length, (jbyte*) seedBuffer.data);
	return ret;
}

JNIEXPORT jbyteArray JNICALL Java_com_keysqr_KeySqrKt_keySqrGetPublicKey(
		JNIEnv* env,
		jobject  obj,
		jstring keySqrInHumanReadableFormWithOrientationsObj,
		jstring jsonKeyDerivationOptionsObj,
		jstring clientsApplicationIdObj
) {
	const std::string keySqrInHumanReadableFormWithOrientations(
			env->GetStringUTFChars( keySqrInHumanReadableFormWithOrientationsObj, NULL )
	);
	const std::string jsonKeyDerivationOptions(
			env->GetStringUTFChars( jsonKeyDerivationOptionsObj, NULL )
	);
	const std::string clientsApplicationId(
			env->GetStringUTFChars( clientsApplicationIdObj, NULL )
	);

	const KeySqrFromString keySqr(keySqrInHumanReadableFormWithOrientations);
	const PublicPrivateKeyPair gkp(
			keySqr,
			jsonKeyDerivationOptions,
			clientsApplicationId
	);
	const std::vector<unsigned char> publicKey = gkp.getPublicKey().getPublicKeyBytes();
	jbyteArray ret = env->NewByteArray(publicKey.size());
	env->SetByteArrayRegion(ret, 0, publicKey.size(), (jbyte*) publicKey.data());
	return ret;
}


}
