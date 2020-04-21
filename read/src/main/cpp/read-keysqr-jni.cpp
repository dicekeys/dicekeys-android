#include <string>
#include <jni.h>
#include "./read-keysqr/lib-read-keysqr/graphics/cv.h"
#include "./read-keysqr/lib-read-keysqr/read-keysqr.h"
#include "./read-keysqr/lib-keysqr/lib-keysqr.hpp"

extern "C" {

//JNIEXPORT jstring JNICALL Java_org_dicekeys_readkeysqr_ReadKeySqr_ReadKeySqrJson(
//     JNIEnv* env,
//     jobject obj,
//     jint width,
//     jint height,
//     jint bytesPerRow,
//     jobject byteBufferForGrayscaleChannel
// ) {
// 	std::string jsonResult;
// 	void *pointerToByteArrayForGrayscaleChannel = env->GetDirectBufferAddress(byteBufferForGrayscaleChannel);
// 	if (pointerToByteArrayForGrayscaleChannel != NULL) {
// 		try {
// 			jsonResult = readKeySqrJson((int) width, (int) height, (size_t) bytesPerRow, (void*) pointerToByteArrayForGrayscaleChannel);
//		} catch (...) {
// 			jsonResult = "null";
// 		}
// 	}
// 	return env->NewStringUTF(jsonResult.c_str());
//}


JNIEXPORT jlong Java_org_dicekeys_read_ReadKeySqr_newKeySqrImageReaderJNI(
		JNIEnv* env,
		jobject obj
		)
{
	return (long)(new KeySqrImageReader());
}

JNIEXPORT jboolean Java_org_dicekeys_read_ReadKeySqr_processImageJNI(
		JNIEnv* env,
		jobject obj,
		jlong reader,
		jint width,
		jint height,
		jint bytesPerRow,
		jobject byteBufferForGrayscaleChannel
		)
{
	KeySqrImageReader* pReader = (KeySqrImageReader*)reader;

	void *pointerToByteArrayForGrayscaleChannel = env->GetDirectBufferAddress(byteBufferForGrayscaleChannel);
	if (pointerToByteArrayForGrayscaleChannel != NULL) {
		return pReader->processImage((int)width, (int)height, (int)bytesPerRow, pointerToByteArrayForGrayscaleChannel);
	} else
	{
		return false;
	}
}

JNIEXPORT void Java_org_dicekeys_read_ReadKeySqr_renderAugmentationOverlayJNI(
		JNIEnv* env,
		jobject  obj,
		jlong reader,
		jint width,
		jint height,
		jobject byteBufferForOverlay
		)
{
	KeySqrImageReader* pReader = (KeySqrImageReader*)reader;

	void *pointerToByteArrayForOverlay = env->GetDirectBufferAddress(byteBufferForOverlay);
	if(pointerToByteArrayForOverlay != NULL)
	{
		pReader->renderAugmentationOverlay((int)width, (int)height, (uint32_t*)pointerToByteArrayForOverlay);
	}
}

JNIEXPORT jstring Java_org_dicekeys_read_ReadKeySqr_jsonKeySqrReadJNI(
		JNIEnv* env,
		jobject  obj,
		jlong reader
)
{
	KeySqrImageReader* pReader = (KeySqrImageReader*)reader;
	std::string json = pReader->jsonKeySqrRead();
	return env->NewStringUTF(json.c_str());
}

JNIEXPORT void Java_org_dicekeys_read_ReadKeySqr_deleteKeySqrImageReaderJNI(
		JNIEnv* env,
		jobject  obj,
		jlong reader
		)
{
	delete (KeySqrImageReader*)reader;
}
}
