#include <jni.h>
#include <windows.h>
#include <dshow.h>
#include "net_sf_jdshow_IMediaSeeking.h"
#include "jniutils.h"
#include "utils.h"
#include <iostream>


JNIEXPORT jint JNICALL Java_net_sf_jdshow_IMediaSeeking_GetPositions
  (JNIEnv *pEnv, jobject o, jlongArray a1, jlongArray a2)
{
	//std::cout<<"IMediaSeeking_GetPositions  \n"<<std::endl;
	jlong *p1 = GetLongArr(pEnv, a1);
	jlong *p2 = GetLongArr(pEnv, a2);
	
	IMediaSeeking *pMediaSeeking = (IMediaSeeking *) getPeerPtr(pEnv, o);
	int hr = pMediaSeeking->GetPositions(p1, p2);
	
	ReleaseLongArr(pEnv, a1, p1);
	ReleaseLongArr(pEnv, a2, p2);
	//std::cout<<"IMediaSeeking_GetPositions complete \n"<<std::endl;

	return hr;
}

JNIEXPORT jint JNICALL Java_net_sf_jdshow_IMediaSeeking_SetPositions
  (JNIEnv *pEnv, jobject o, jlongArray a1, jint f1, jlongArray a2, jint f2)
{
	//std::cout<<"IMediaSeeking_SetPositions  \n"<<std::endl;
	jlong *p1 = GetLongArr(pEnv, a1);
	jlong *p2 = GetLongArr(pEnv, a2);
	
	IMediaSeeking *pMediaSeeking = (IMediaSeeking *) getPeerPtr(pEnv, o);
	int hr = pMediaSeeking->SetPositions(p1, f1, p2, f2);
	
	ReleaseLongArr(pEnv, a1, p1);
	ReleaseLongArr(pEnv, a2, p2);
	//std::cout<<"IMediaSeeking_SetPositions complete \n"<<std::endl;

	return hr;

}

JNIEXPORT jint JNICALL Java_net_sf_jdshow_IMediaSeeking_SetRate
  (JNIEnv *pEnv, jobject o, jdouble rate)
{
	//std::cout<<"IMediaSeeking_SetRate  \n"<<std::endl;
	IMediaSeeking *pMediaSeeking = (IMediaSeeking *) getPeerPtr(pEnv, o);
	int hr = pMediaSeeking->SetRate(rate);
	//std::cout<<"IMediaSeeking_SetRate complete \n"<<std::endl;

	return hr;

}

JNIEXPORT jint JNICALL Java_net_sf_jdshow_IMediaSeeking_GetRate
  (JNIEnv *pEnv, jobject o, jdoubleArray a)
{
	//std::cout<<"IMediaSeeking_GetRate  \n"<<std::endl;
	jdouble *p = GetDoubleArr(pEnv, a);
	
	IMediaSeeking *pMediaSeeking = (IMediaSeeking *) getPeerPtr(pEnv, o);
	int hr = pMediaSeeking->GetRate(p);
	ReleaseDoubleArr(pEnv, a, p);
	//std::cout<<"IMediaSeeking_GetRate  complete\n"<<std::endl;

	return hr;	
}  


JNIEXPORT jint JNICALL Java_net_sf_jdshow_IMediaSeeking_GetDuration
  (JNIEnv *pEnv, jobject o, jlongArray a1)
{
	//std::cout<<"IMediaSeeking_GetDuration  \n"<<std::endl;
	jlong *p1 = GetLongArr(pEnv, a1);
	
	IMediaSeeking *pMediaSeeking = (IMediaSeeking *) getPeerPtr(pEnv, o);
	int hr = pMediaSeeking->GetDuration(p1);
	
	ReleaseLongArr(pEnv, a1, p1);

	//std::cout<<"IMediaSeeking_GetDuration complete \n"<<std::endl;
	return hr;
}
  
JNIEXPORT jint JNICALL Java_net_sf_jdshow_IMediaSeeking_GetCurrentPosition
  (JNIEnv *pEnv, jobject o, jlongArray a1)
{
	//std::cout<<"IMediaSeeking_GetCurrentPosition  \n"<<std::endl;
	jlong *p1 = GetLongArr(pEnv, a1);
	
	IMediaSeeking *pMediaSeeking = (IMediaSeeking *) getPeerPtr(pEnv, o);
	int hr = pMediaSeeking->GetCurrentPosition(p1);
	
	ReleaseLongArr(pEnv, a1, p1);

	//std::cout<<"IMediaSeeking_GetCurrentPosition complete \n"<<std::endl;
	return hr;
}
  
  

JNIEXPORT jobject JNICALL Java_net_sf_jdshow_IMediaSeeking_Init_1IID
  (JNIEnv *pEnv, jclass, jobject jGuid)
{
	//std::cout<<"IMediaSeeking_Init_1IID  \n"<<std::endl;
	fromGUID(pEnv, IID_IMediaSeeking, jGuid);
	//std::cout<<"IMediaSeeking_Init_1IID complete \n"<<std::endl;
	return jGuid;
}


