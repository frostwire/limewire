#include <jni.h>
#include <windows.h>
#include <dshow.h>
#include "net_sf_jdshow_IGraphBuilder.h"
#include "jniutils.h"
#include "utils.h"
#include <iostream>


JNIEXPORT jint JNICALL Java_net_sf_jdshow_IGraphBuilder_RenderFile
  (JNIEnv *pEnv, jobject o, jstring jFileStr, jstring playlistStr)
{
	//std::cout<<"IGraphBuilder RenderFile\n"<<std::endl;
	// TODO: handle null strings
	const jchar *jFileStrArray = pEnv->GetStringChars(jFileStr, NULL);
	const wchar_t *szFileStr = jchar_to_wchar_t_array_alloc(jFileStrArray);	// TODO: delete

	IGraphBuilder *pGraphBuilder = (IGraphBuilder *) getPeerPtr(pEnv, o);
	jint result = pGraphBuilder->RenderFile(szFileStr, NULL);	// TODO: playlist param

	pEnv->ReleaseStringChars(jFileStr, jFileStrArray);

	//std::cout<<"IGraphBuilder RenderFile complete\n"<<std::endl;

	return result;
}

JNIEXPORT jint JNICALL Java_net_sf_jdshow_IGraphBuilder_Abort
  (JNIEnv *pEnv, jobject o)
{
	//std::cout<<"IGraphBuilder_Abort  \n"<<std::endl;
	IGraphBuilder *pGraphBuilder = (IGraphBuilder *) getPeerPtr(pEnv, o);
	jint result = pGraphBuilder->Abort();

	//std::cout<<"IGraphBuilder_Abort complete \n"<<std::endl;
	return result;
}


JNIEXPORT jobject JNICALL Java_net_sf_jdshow_IGraphBuilder_Init_1IID
  (JNIEnv *pEnv, jclass, jobject jGuid)
{
	//std::cout<<"IGraphBuilder_Init_1IID  \n"<<std::endl;
	fromGUID(pEnv, IID_IGraphBuilder, jGuid);
	//std::cout<<"IGraphBuilder_Init_1IID complete \n"<<std::endl;
	return jGuid;
}