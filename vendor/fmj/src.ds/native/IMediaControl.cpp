#include <jni.h>
#include <windows.h>
#include <dshow.h>
#include "net_sf_jdshow_IMediaControl.h"
#include "jniutils.h"
#include "utils.h"
#include <iostream>


JNIEXPORT jint JNICALL Java_net_sf_jdshow_IMediaControl_Run
  (JNIEnv *pEnv, jobject o)
{
	//std::cout<<"IMediaControl_Run  \n"<<std::endl;
	IMediaControl *p = (IMediaControl *) getPeerPtr(pEnv, o);
	int hr = p->Run();
	//std::cout<<"IMediaControl_Run complete \n"<<std::endl;
	return hr;
}

/*
 * Class:     net_sf_jdshow_IMediaControl
 * Method:    Stop
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_net_sf_jdshow_IMediaControl_Stop
  (JNIEnv *pEnv, jobject o)
{
	//std::cout<<"IMediaControl_Stop  \n"<<std::endl;
	IMediaControl *pMediaControl = (IMediaControl *) getPeerPtr(pEnv, o);
	int hr =  pMediaControl->Stop();
	//std::cout<<"IMediaControl_Stop complete \n"<<std::endl;
	return hr;
}
