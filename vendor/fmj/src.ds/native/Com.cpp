#include <jni.h>
#include <windows.h>
#include <dshow.h>
#include "net_sf_jdshow_Com.h"
#include "jniutils.h"
#include "utils.h"
#include <iostream>



JNIEXPORT void JNICALL Java_net_sf_jdshow_Com_CoInitialize
  (JNIEnv *, jclass)
{
	//std::cout<<"Com_CoInitialize  \n"<<std::endl;
	CoInitializeEx(NULL, COINIT_MULTITHREADED);
	//CoInitialize(NULL);	// TODO: return value, parameter value
	//std::cout<<"Com_CoInitialize complete \n"<<std::endl;
}


JNIEXPORT void JNICALL Java_net_sf_jdshow_Com_CoUninitialize
  (JNIEnv *, jclass)
{
	//std::cout<<"Com_CoUninitialize  \n"<<std::endl;
	CoUninitialize();
	//std::cout<<"Com_CoUninitialize complete \n"<<std::endl;
}


JNIEXPORT jint JNICALL Java_net_sf_jdshow_Com_CoCreateInstance
  (JNIEnv *pEnv, jclass, jobject jrclsid, jlong jpUnkOuter, jint jdwClsContext, jobject jriid, jlongArray a)
{
	//std::cout<<"Com_CoCreateInstance  \n"<<std::endl;
	void *pResult = 0;

	int hr = CoCreateInstance(toGUID(pEnv, jrclsid), (LPUNKNOWN) jlong2ptr(jpUnkOuter), jdwClsContext, toGUID(pEnv, jriid),
							(void**)&pResult);

	jlong *p = GetLongArr(pEnv, a);
	p[0] = ptr2jlong(pResult);
	ReleaseLongArr(pEnv, a, p);

	//std::cout<<"Com_CoCreateInstance complete \n"<<std::endl;
	return hr;
}
