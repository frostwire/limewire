#include <jni.h>
#include <windows.h>
#include <dshow.h>
#include "net_sf_jdshow_IBasicAudio.h"
#include "jniutils.h"
#include "utils.h"
#include <iostream>


JNIEXPORT jint JNICALL Java_net_sf_jdshow_IBasicAudio_put_1Volume
  (JNIEnv *pEnv, jobject o, jlong volume)
{
	//std::cout<<"IBasicAudio_put_1Volume  \n"<<std::endl;
	IBasicAudio *pBasicAudio = (IBasicAudio *) getPeerPtr(pEnv, o);
	int hr = pBasicAudio->put_Volume(volume);
	//std::cout<<"IBasicAudio_put_1Volume complete \n"<<std::endl;
	return hr;

}

JNIEXPORT jint JNICALL Java_net_sf_jdshow_IBasicAudio_get_1Volume
  (JNIEnv *pEnv, jobject o, jlongArray a)
{
	//std::cout<<"IBasicAudio_get_1Volume  \n"<<std::endl;
	jlong *p = GetLongArr(pEnv, a);

	IBasicAudio *pBasicAudio = (IBasicAudio *) getPeerPtr(pEnv, o);
	int hr = pBasicAudio->get_Volume((long *)p);
	ReleaseLongArr(pEnv, a, p);
	//std::cout<<"IBasicAudio_get_1Volume complete \n"<<std::endl;
	return hr;
}


JNIEXPORT jobject JNICALL Java_net_sf_jdshow_IBasicAudio_Init_1IID
  (JNIEnv *pEnv, jclass, jobject jGuid)
{
	//std::cout<<"IBasicAudio_Init_1IID  \n"<<std::endl;
	fromGUID(pEnv, IID_IBasicAudio, jGuid);
	//std::cout<<"IBasicAudio_Init_1IID complete \n"<<std::endl;
	return jGuid;
}


