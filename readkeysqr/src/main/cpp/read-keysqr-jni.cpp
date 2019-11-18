#include <string>
#include <jni.h>
#include "./read-keysqr/lib-read-keysqr/graphics/cv.h"

#include "./read-keysqr/lib-read-keysqr/read-keysqr.h"

extern "C" {

JNIEXPORT jstring JNICALL Java_com_example_readkeysqr_ReadKeySqr_ReadKeySqrJson(
     JNIEnv* env,
     jobject obj,
     jint width,
     jint height,
     jint bytesPerRow,
     jobject byteBufferForGrayscaleChannel
 ) {
 	std::string jsonResult;
 	void *pointerToByteArrayForGrayscaleChannel = env->GetDirectBufferAddress(byteBufferForGrayscaleChannel);
 	if (pointerToByteArrayForGrayscaleChannel != NULL) {
 		try {
 			jsonResult = readKeySqrJson((int) width, (int) height, (size_t) bytesPerRow, (void*) pointerToByteArrayForGrayscaleChannel);
		} catch (...) {
 			jsonResult = "null";
 		}
 	}
 	return env->NewStringUTF(jsonResult.c_str());
}

JNIEXPORT jstring JNICALL Java_com_example_readkeysqr_ReadKeySqr_HelloFromOpenCV(JNIEnv *env, jobject obj) {
    std::string name = "OPENCV Version ";
    name += CV_VERSION;

    return env->NewStringUTF(name.c_str());
}

KeySqrImageReader reader = KeySqrImageReader();

JNIEXPORT jboolean  JNICALL Java_com_example_readkeysqr_ReadKeySqr_ProcessImage(
		JNIEnv* env,
		jobject  obj,
		jint width,
		jint height,
		jint bytesPerRow,
		jobject byteBufferForGrayscaleChannel,
		jobject byteBufferForOverlay
		)
{
	void *pointerToByteArrayForGrayscaleChannel = env->GetDirectBufferAddress(byteBufferForGrayscaleChannel);
	if (pointerToByteArrayForGrayscaleChannel != NULL) {
		bool res = reader.processImage((int)width, (int)height, (size_t)bytesPerRow, (void*) pointerToByteArrayForGrayscaleChannel);

		void *pointerToByteArrayForOverlay = env->GetDirectBufferAddress(byteBufferForOverlay);
		if(pointerToByteArrayForOverlay != NULL)
		{
			reader.renderAugmentationOverlay((int)width, (int)height, (uint32_t*)pointerToByteArrayForOverlay);
		}

		return res;
	} else{
		return false;
	}
}

}
