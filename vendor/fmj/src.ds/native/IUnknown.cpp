#include <jni.h>
#include <windows.h>
#include <dshow.h>
#include "net_sf_jdshow_IUnknown.h"
#include "jniutils.h"
#include "utils.h"
#include <iostream>
#include <jni.h>




JNIEXPORT jlong JNICALL Java_net_sf_jdshow_IUnknown_Release
  (JNIEnv *pEnv, jobject o)
{
	//std::cout<<"IUnknown_Release \n"<<std::endl;
	IUnknown *pUnknown = (IUnknown *) getPeerPtr(pEnv, o);
	int hr = pUnknown->Release();
	//std::cout<<"IUnknown_Release complete \n"<<std::endl;
	return hr;

}


JNIEXPORT jint JNICALL Java_net_sf_jdshow_IUnknown_QueryInterface
  (JNIEnv *pEnv, jobject o, jobject jGuid, jlongArray a, jstring name)
{
	//const char *str = pEnv->GetStringUTFChars(name, 0);


	//std::cout<<"IUnknown_QueryInterface "<<str<<std::endl;
	//std::cout<<"jGuid "<<jGuid<<std::endl;
	//std::cout<<o<<std::endl;

	void *pResult = 0;
	//std::cout<<1<<std::endl;
	
	IUnknown *pUnknown = (IUnknown *) getPeerPtr(pEnv, o);
	//std::cout<<"*pUnknown = ";
	//std::cout<<pUnknown<<std::endl;
	//std::cout<<2<<std::endl;
	int hr = pUnknown->QueryInterface(toGUID(pEnv, jGuid), (void**)&pResult);
	//std::cout<<3<<std::endl;

	jlong *p = GetLongArr(pEnv, a);
	//std::cout<<4<<std::endl;
	p[0] = ptr2jlong(pResult);
	//std::cout<<5<<std::endl;
	ReleaseLongArr(pEnv, a, p);
	//std::cout<<"IUnknown_QueryInterface complete \n"<<std::endl;

	return hr;
}