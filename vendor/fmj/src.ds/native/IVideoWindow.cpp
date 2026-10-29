#include <jni.h>
#include <windows.h>
#include <dshow.h>
#include "net_sf_jdshow_IVideoWindow.h"
#include "jniutils.h"
#include "utils.h"
#include <iostream>



JNIEXPORT jint JNICALL Java_net_sf_jdshow_IVideoWindow_put_1Owner
  (JNIEnv *pEnv, jobject o, jlong hwnd)
{
	//std::cout<<"IVideoWindow_put_1Owner \n"<<std::endl;
	IVideoWindow *pVideoWindow = (IVideoWindow *) getPeerPtr(pEnv, o);
	jint result =  pVideoWindow->put_Owner(hwnd);
	//std::cout<<"IVideoWindow_put_1Owner complete \n"<<std::endl;
	return result;
}


JNIEXPORT jint JNICALL Java_net_sf_jdshow_IVideoWindow_put_1WindowStyle
  (JNIEnv *pEnv, jobject o, jint style)
{
	//std::cout<<"IVideoWindow_put_1WindowStyle \n"<<std::endl;
	IVideoWindow *pVideoWindow = (IVideoWindow *) getPeerPtr(pEnv, o);
	jint result = pVideoWindow->put_WindowStyle(style);
	//std::cout<<"IVideoWindow_put_1WindowStyle complete \n"<<std::endl;
	return result;
}  


JNIEXPORT jint JNICALL Java_net_sf_jdshow_IVideoWindow_put_1MessageDrain
  (JNIEnv *pEnv, jobject o, jlong hwnd)
{
	//std::cout<<"IVideoWindow_put_1MessageDrain  \n"<<std::endl;
	IVideoWindow *pVideoWindow = (IVideoWindow *) getPeerPtr(pEnv, o);
	jint result =  pVideoWindow->put_MessageDrain(hwnd);
	//std::cout<<"IVideoWindow_put_1MessageDrain complete \n"<<std::endl;
	return result;
}

JNIEXPORT jint JNICALL Java_net_sf_jdshow_IVideoWindow_put_1Top
  (JNIEnv *pEnv, jobject o, jint value)
{
	//std::cout<<"IVideoWindow_put_1Top  \n"<<std::endl;
	IVideoWindow *pVideoWindow = (IVideoWindow *) getPeerPtr(pEnv, o);
	jint result =   pVideoWindow->put_Top(value);
	//std::cout<<"IVideoWindow_put_1Top complete \n"<<std::endl;
	return result;
}

JNIEXPORT jint JNICALL Java_net_sf_jdshow_IVideoWindow_put_1Left
  (JNIEnv *pEnv, jobject o, jint value)
{
	//std::cout<<"IVideoWindow_put_1Left  \n"<<std::endl;
	IVideoWindow *pVideoWindow = (IVideoWindow *) getPeerPtr(pEnv, o);
	jint result =   pVideoWindow->put_Left(value);
	//std::cout<<"IVideoWindow_put_1Left complete \n"<<std::endl;
	return result;
}  

JNIEXPORT jint JNICALL Java_net_sf_jdshow_IVideoWindow_put_1Width
  (JNIEnv *pEnv, jobject o, jint value)
{
	//std::cout<<"IVideoWindow_put_1Width  \n"<<std::endl;
	IVideoWindow *pVideoWindow = (IVideoWindow *) getPeerPtr(pEnv, o);
	jint result =   pVideoWindow->put_Width(value);
	//std::cout<<"IVideoWindow_put_1Width complete \n"<<std::endl;
	return result;
}

JNIEXPORT jint JNICALL Java_net_sf_jdshow_IVideoWindow_put_1Height
  (JNIEnv *pEnv, jobject o, jint value)
{
	//std::cout<<"IVideoWindow_put_1Height  \n"<<std::endl;
	IVideoWindow *pVideoWindow = (IVideoWindow *) getPeerPtr(pEnv, o);
	jint result =   pVideoWindow->put_Height(value);
	//std::cout<<"IVideoWindow_put_1Height complete \n"<<std::endl;
	return result;
}

JNIEXPORT jint JNICALL Java_net_sf_jdshow_IVideoWindow_setWindowPosition
  (JNIEnv *pEnv, jobject o, jint x, jint y, jint width, jint height)
{
	//std::cout<<"IVideoWindow_setWindowPosition  \n"<<std::endl;
	IVideoWindow *pVideoWindow = (IVideoWindow *) getPeerPtr(pEnv, o);
	jint result =   pVideoWindow->SetWindowPosition(x, y, width, height);
	//std::cout<<"IVideoWindow_setWindowPosition complete \n"<<std::endl;
	return result;
}


JNIEXPORT jint JNICALL Java_net_sf_jdshow_IVideoWindow_get_1Width
  (JNIEnv *pEnv, jobject o, jlongArray a1)
{
	//std::cout<<"IVideoWindow_get_1Width  \n"<<std::endl;
	jlong *p1 = GetLongArr(pEnv, a1);
	
	IVideoWindow *pVideoWindow = (IVideoWindow *) getPeerPtr(pEnv, o);
	int hr = pVideoWindow->get_Width((long *) p1);
	
	ReleaseLongArr(pEnv, a1, p1);
	//std::cout<<"IVideoWindow_get_1Width complete \n"<<std::endl;

	return hr;
}   


JNIEXPORT jint JNICALL Java_net_sf_jdshow_IVideoWindow_get_1Height
  (JNIEnv *pEnv, jobject o, jlongArray a1)
{
	//std::cout<<"IVideoWindow_get_1Height  \n"<<std::endl;
	jlong *p1 = GetLongArr(pEnv, a1);
	
	IVideoWindow *pVideoWindow = (IVideoWindow *) getPeerPtr(pEnv, o);
	int hr = pVideoWindow->get_Height((long *) p1);
	
	ReleaseLongArr(pEnv, a1, p1);
	//std::cout<<"IVideoWindow_get_1Height complete \n"<<std::endl;

	return hr;
}   
