#include <string>
#include <jni.h>
#include "native-object-jni.hpp"
#include "./read-dicekey/lib-read-dicekey/graphics/cv.h"
#include "./read-dicekey/lib-read-dicekey/read-dicekey.hpp"
#include "./read-dicekey/lib-dicekey/lib-dicekey.hpp"

extern "C" {

JNIEXPORT jlong Java_org_dicekeys_read_ReadDiceKey_constructJNI(
	JNIEnv* env,
	jobject obj
) {
	return (jlong)(new DiceKeyImageProcessor());
}

JNIEXPORT jboolean Java_org_dicekeys_read_ReadDiceKey_processImage(
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
		getNativeObjectPtr<DiceKeyImageProcessor>(env,obj)->
		  processImage((int)width, (int)height, (size_t)bytesPerRow, pointerToByteArrayForGrayscaleChannel)
	);
}

JNIEXPORT void Java_org_dicekeys_read_ReadDiceKey_renderAugmentationOverlay(
	JNIEnv* env,
	jobject  obj,
	jint width,
	jint height,
	jobject byteBufferForOverlay
	) {
	void *pointerToByteArrayForOverlay = env->GetDirectBufferAddress(byteBufferForOverlay);
	if (pointerToByteArrayForOverlay != NULL) {
		getNativeObjectPtr<DiceKeyImageProcessor>(env,obj)->renderAugmentationOverlay((int)width, (int)height, (uint32_t*)pointerToByteArrayForOverlay);
	}
}

JNIEXPORT jstring Java_org_dicekeys_read_ReadDiceKey_jsonDiceKeyRead(
	JNIEnv* env,
	jobject  obj
) {
	return stringToJString(env, getNativeObjectPtr<DiceKeyImageProcessor>(env, obj)->jsonDiceKeyRead());
}

JNIEXPORT void Java_org_dicekeys_read_ReadDiceKey_destructJNI(
	JNIEnv* env,
	jobject  obj
) {
	deleteNativeObjectPtr<DiceKeyImageProcessor>(env, obj);
}

} // extern c
