#include <string>
#include <jni.h>
#include "native-object-jni.hpp"
#include "./read-keysqr/lib-read-keysqr/graphics/cv.h"
#include "./read-keysqr/lib-read-keysqr/read-keysqr.hpp"
#include "./read-keysqr/lib-keysqr/lib-keysqr.hpp"

extern "C" {

JNIEXPORT jlong Java_org_dicekeys_read_ReadKeySqr_constructJNI(
	JNIEnv* env,
	jobject obj
) {
	return (jlong)(new KeySqrImageReader());
}

JNIEXPORT jboolean Java_org_dicekeys_read_ReadKeySqr_processImage(
	JNIEnv* env,
	jobject obj,
	jint width,
	jint height,
	jint bytesPerRow,
	jobject byteBufferForGrayscaleChannel
) {
	void *pointerToByteArrayForGrayscaleChannel = env->GetDirectBufferAddress(byteBufferForGrayscaleChannel);
	return (jboolean) (
		pointerToByteArrayForGrayscaleChannel != NULL &&
		getNativeObjectPtr<KeySqrImageReader>(env,obj)->
		  processImage((int)width, (int)height, (size_t)bytesPerRow, pointerToByteArrayForGrayscaleChannel)
	);
}

JNIEXPORT void Java_org_dicekeys_read_ReadKeySqr_renderAugmentationOverlay(
	JNIEnv* env,
	jobject  obj,
	jint width,
	jint height,
	jobject byteBufferForOverlay
	) {
	void *pointerToByteArrayForOverlay = env->GetDirectBufferAddress(byteBufferForOverlay);
	if (pointerToByteArrayForOverlay != NULL) {
		getNativeObjectPtr<KeySqrImageReader>(env,obj)->renderAugmentationOverlay((int)width, (int)height, (uint32_t*)pointerToByteArrayForOverlay);
	}
}

JNIEXPORT jstring Java_org_dicekeys_read_ReadKeySqr_jsonKeySqrRead(
	JNIEnv* env,
	jobject  obj
) {
	return stringToJString(env, getNativeObjectPtr<KeySqrImageReader>(env, obj)->jsonKeySqrRead());
}

JNIEXPORT void Java_org_dicekeys_read_ReadKeySqr_destructJNI(
	JNIEnv* env,
	jobject  obj
) {
	deleteNativeObjectPtr<KeySqrImageReader>(env, obj);
}

} // extern c
