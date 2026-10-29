#include <mfplay.h>
#include <Mfapi.h>
#include <shlwapi.h>
#include <windows.h>
#include <jni.h>
#include "net_sf_fmj_mf_media_content_unknown_MFPlayer.h"
#include <iostream>
#include <strsafe.h>
#include <mferror.h>
#include <Objbase.h>
//I am leaving in all of the commented out std::couts for simple debugging if necessary.

//Everything in PlayerCallback is boilerplate
class PlayerCallback : public IMFPMediaPlayerCallback {
public:

    PlayerCallback() : m_refcount(1)
    {
    }

    STDMETHODIMP QueryInterface(REFIID riid, void** ppv)
    {
        static const QITAB qit[] = 
        {
            QITABENT(PlayerCallback, IMFPMediaPlayerCallback),
            { 0 },
        };
        return QISearch(this, qit, riid, ppv);
    }

    STDMETHODIMP_(ULONG) AddRef() 
    { 
        return InterlockedIncrement(&m_refcount); 
    }
    STDMETHODIMP_(ULONG) Release() 
    {
        ULONG count = InterlockedDecrement(&m_refcount);
        if (count == 0)
        {
            delete this;
            return 0;
        }
        return count;
    }


    // implemented below
    void STDMETHODCALLTYPE OnMediaPlayerEvent(MFP_EVENT_HEADER *pEventHeader);

private:
    long m_refcount;
};   




//The actual player
IMFPMediaPlayer *pPlayer = NULL;
//Callback for the player
PlayerCallback *pCallback = NULL;
JavaVM *pJavaVM = NULL;
//the instance of MFPlayer.java
static jobject jMFPlayer;
//callback method in the instance of MFPlayer.java
static jmethodID statusChangedMethodID;
//constant in MFPlayer.java used in the callback
static jint MF_ERROR;
//constant in MFPlayer.java used in the callback
static jint LOADED;
//constant in MFPlayer.java used in the callback
static jint STARTED;
//constant in MFPlayer.java used in the callback
static jint ENDED;

//kills and nullifies the player if there is one.  
void killPlayer(){	
	//std::cout<<"null check"<<std::endl;
	if(pPlayer){
	//std::cout<<"shutdown"<<std::endl;
		pPlayer->Shutdown();
	//std::cout<<"release"<<std::endl;
        pPlayer->Release();
	//std::cout<<"nullify"<<std::endl;
		pPlayer = NULL;
	}
	//std::cout<<"kill done"<<std::endl;

	if(pCallback){
		pCallback->Release();
		pCallback = NULL;
	}
}

//initializes MFPlayer for use with the instance of MFPlayer.java (o).
JNIEXPORT jint JNICALL Java_net_sf_fmj_mf_media_content_unknown_MFPlayer_initialize(JNIEnv *pEnv, jobject o){		
	//std::cout<<"Java_net_sf_fmj_mf_media_content_unknown_MFPlayer_initialize"<<std::endl;

	int result = pEnv->GetJavaVM(&pJavaVM);
	if(result < 0){
		return result;
	}

	//The pointer to the JVM requires NewGlobalRef so that it does not become stale when this method completes.
	jMFPlayer = pEnv->NewGlobalRef(o);

	//load the jclass
	jclass mfPlayerClass = pEnv->GetObjectClass(jMFPlayer);
	if(!mfPlayerClass){
		return E_FAIL;
	}

	//get the constants from MFPlayer.java
	MF_ERROR = pEnv->GetStaticIntField(mfPlayerClass, pEnv->GetStaticFieldID(mfPlayerClass, "ERROR", "I"));
	LOADED = pEnv->GetStaticIntField(mfPlayerClass, pEnv->GetStaticFieldID(mfPlayerClass, "LOADED", "I"));
	STARTED = pEnv->GetStaticIntField(mfPlayerClass, pEnv->GetStaticFieldID(mfPlayerClass, "STARTED", "I"));
	ENDED = pEnv->GetStaticIntField(mfPlayerClass, pEnv->GetStaticFieldID(mfPlayerClass, "ENDED", "I"));

	statusChangedMethodID = pEnv->GetMethodID(pEnv->GetObjectClass(jMFPlayer), "statusChanged", "(I)V");
     
	if (statusChangedMethodID == NULL) {	
         return E_FAIL;  // method not found 
     }
    
	return CoInitializeEx(NULL, COINIT_MULTITHREADED);
}

//converts a jstring to a wchar_t.  The pointer returned MUST be deleted by the caller.
wchar_t * jstringToWchar_t(JNIEnv* pEnv, jstring string){ 		
	//std::cout<<"jstringToWchar_t"<<std::endl;
	int stringSize = pEnv->GetStringLength(string);
    const jchar* jChars = pEnv->GetStringChars(string, NULL);

    wchar_t *wString = new wchar_t[stringSize+1];
    memcpy(wString, jChars, stringSize*2);
    wString[stringSize] = 0;
	pEnv->ReleaseStringChars(string, jChars);

	return wString;
}



JNIEXPORT jint JNICALL Java_net_sf_fmj_mf_media_content_unknown_MFPlayer_startPlayer(JNIEnv *pEnv, jobject o, jstring url, jlong jHwnd){		
	//std::cout<<"Java_net_sf_fmj_mf_media_content_unknown_MFPlayer_startPlayer"<<std::endl;

	HWND hwnd = (HWND)jHwnd;

	//destroy any existing player
	killPlayer();

	wchar_t * wUrl = jstringToWchar_t(pEnv, url);

	pCallback = new (std::nothrow) PlayerCallback();

	HRESULT hr = MFPCreateMediaPlayer(
            wUrl,
            TRUE,//start playback
			MFP_OPTION_FREE_THREADED_CALLBACK, // embedding only works with this flag (no event loop here)
            pCallback,    
            hwnd,           
            &pPlayer //pointer to the new player
            ); 

	delete[] wUrl;
	return hr;			
}


JNIEXPORT jint JNICALL Java_net_sf_fmj_mf_media_content_unknown_MFPlayer_setVolume(JNIEnv *pEnv, jobject o, jfloat volume){		
	//std::cout<<"Java_net_sf_fmj_mf_media_content_unknown_MFPlayer_setVolume"<<std::endl;

	if(pPlayer){	
		return pPlayer->SetVolume((float)volume);
	}
	return E_FAIL;
}

JNIEXPORT jint JNICALL Java_net_sf_fmj_mf_media_content_unknown_MFPlayer_getVolume(JNIEnv *pEnv, jobject o, jfloatArray volume){		
	//std::cout<<"Java_net_sf_fmj_mf_media_content_unknown_MFPlayer_getVolume"<<std::endl;	

	if (pPlayer == NULL){			
		return E_FAIL;		
	}

	jfloat *pVolume = pEnv->GetFloatArrayElements(volume, NULL);
	HRESULT hr = pPlayer->GetVolume((float *)pVolume);
	pEnv->ReleaseFloatArrayElements(volume, pVolume, 0);
	return hr;
}


JNIEXPORT jint JNICALL Java_net_sf_fmj_mf_media_content_unknown_MFPlayer_pause(JNIEnv *, jobject){			
	//std::cout<<"Java_net_sf_fmj_mf_media_content_unknown_MFPlayer_pause"<<std::endl;
	if(pPlayer){	
		return pPlayer->Pause();
	}
	return E_FAIL;
}


JNIEXPORT jint JNICALL Java_net_sf_fmj_mf_media_content_unknown_MFPlayer_resume(JNIEnv *, jobject){			
	//std::cout<<"Java_net_sf_fmj_mf_media_content_unknown_MFPlayer_resume"<<std::endl;
	if(pPlayer){	
		return pPlayer->Play();
	}
	return E_FAIL;
}


JNIEXPORT void JNICALL Java_net_sf_fmj_mf_media_content_unknown_MFPlayer_killPlayer(JNIEnv *, jobject){		
	//std::cout<<"Java_net_sf_fmj_mf_media_content_unknown_MFPlayer_killPlayer"<<std::endl;
	killPlayer();
}


	// Calling update ensures that the video is rendered at the proper size.
	//This is sometimes necessary when a video is first displayed.
JNIEXPORT void JNICALL Java_net_sf_fmj_mf_media_content_unknown_MFPlayer_update(JNIEnv *, jobject){		
	//std::cout<<"Java_net_sf_fmj_mf_media_content_unknown_MFPlayer_killPlayer"<<std::endl;
	if (pPlayer){			
		pPlayer->UpdateVideo();		
	}
}


JNIEXPORT jint JNICALL Java_net_sf_fmj_mf_media_content_unknown_MFPlayer_setPosition(JNIEnv *, jobject, jlong position){		
	//std::cout<<"Java_net_sf_fmj_mf_media_content_unknown_MFPlayer_setPosition"<<std::endl;		
	if (pPlayer == NULL){			
		return E_FAIL;		
	}

	//Although the position is ultimately a LONG, the api requires PROPVARIANT 
    PROPVARIANT propVariant;
    PropVariantInit(&propVariant);
        
	propVariant.vt = VT_I8;        
	propVariant.hVal.QuadPart = position;
        
	HRESULT hr = pPlayer->SetPosition(MFP_POSITIONTYPE_100NS, &propVariant);

    PropVariantClear(&propVariant);
	return hr;
}


JNIEXPORT jint JNICALL Java_net_sf_fmj_mf_media_content_unknown_MFPlayer_getPosition(JNIEnv * pEnv, jobject, jlongArray position){		
	//std::cout<<"Java_net_sf_fmj_mf_media_content_unknown_MFPlayer_getPosition"<<std::endl;

	if (pPlayer == NULL){			
		return E_FAIL;		
	}		
	//std::cout<<"getPos player not null"<<std::endl;	

	jlong *pPosition = pEnv->GetLongArrayElements(position, NULL);

	//Although the position is ultimately a LONG, the api requires PROPVARIANT 
    PROPVARIANT propVariant;
    PropVariantInit(&propVariant);
      
		
	HRESULT hr = pPlayer->GetPosition(MFP_POSITIONTYPE_100NS, &propVariant);

        
	if (SUCCEEDED(hr)){            
		*pPosition = propVariant.hVal.QuadPart;        
	}

    PropVariantClear(&propVariant);

	pEnv->ReleaseLongArrayElements(position, pPosition, 0);
	return hr;
}


JNIEXPORT jint JNICALL Java_net_sf_fmj_mf_media_content_unknown_MFPlayer_getNativeSize(JNIEnv *pEnv, jobject o, jlongArray size){		
	//std::cout<<"Java_net_sf_fmj_mf_media_content_unknown_MFPlayer_getNativeSize"<<std::endl;	

	if (pPlayer == NULL){			
		return E_FAIL;		
	}

	SIZE *pSize = new SIZE;
	pSize->cx = 0;
	pSize->cy = 0;

	//std::cout<<"GetNativeVideoSize"<<std::endl;	
	
	HRESULT hr = pPlayer->GetNativeVideoSize(pSize, NULL);	

	if(hr >= 0){
		//std::cout<<"setValues"<<std::endl;	
		//std::cout<<pSize->cx<<std::endl;	
		//std::cout<<pSize->cy<<std::endl;	
		//std::cout<<"setX"<<std::endl;	
		jlong x = pSize->cx;
		//set the first array element to x
		pEnv->SetLongArrayRegion(size, 0, 1, &x);
		//std::cout<<"setY"<<std::endl;	
		jlong y = pSize->cy;
		//set the second array element to y
		pEnv->SetLongArrayRegion(size, 1, 1, &y);
	}

	//std::cout<<"delete"<<std::endl;	
	delete pSize;

	//std::cout<<"pSize deleted"<<std::endl;	

	return hr;
}



JNIEXPORT jint JNICALL Java_net_sf_fmj_mf_media_content_unknown_MFPlayer_getDuration(JNIEnv * pEnv, jobject, jlongArray duration){		
	//std::cout<<"Java_net_sf_fmj_mf_media_content_unknown_MFPlayer_getDuration"<<std::endl;	

	if (pPlayer == NULL){			
		return E_FAIL;		
	}

	jlong *pDuration = pEnv->GetLongArrayElements(duration, NULL);	    

	//Although the position is ultimately a LONG, the api requires PROPVARIANT 
    PROPVARIANT propVariant;
    PropVariantInit(&propVariant);
        
	HRESULT hr = pPlayer->GetDuration(MFP_POSITIONTYPE_100NS, &propVariant);

        
	if (SUCCEEDED(hr)){            
		*pDuration = propVariant.uhVal.QuadPart;        
	}

    PropVariantClear(&propVariant);

	pEnv->ReleaseLongArrayElements(duration, pDuration, 0);
	return hr;
}


//Callback to the java class 
void statusChanged(int status){ 		
	//std::cout<<"status changed"<<std::endl;
	JNIEnv * pEnv = NULL;	 
	//attach to the JVM and create a new JNIEnv since this is called in a native thread
	jint result = pJavaVM->AttachCurrentThread((void**)&pEnv, NULL);	 
	if (result < 0) {   
		//attaching to the jvm failed
		return;     
	}     

	pEnv->CallVoidMethod(jMFPlayer, statusChangedMethodID, status);
	 
	pJavaVM->DetachCurrentThread();
}

//Implementation of IMFPMediaPlayerCallback::OnMediaPlayerEvent.  This receives native player events in a native thread.
void STDMETHODCALLTYPE PlayerCallback::OnMediaPlayerEvent(MFP_EVENT_HEADER * pEventHeader){		
	//std::cout<<"OnMediaPlayerEvent"<<std::endl;
	if(!pPlayer){
		//std::cout<<"this shouldn't happen"<<std::endl;
		return;
	}

	if (FAILED(pEventHeader->hrEvent)) {
		//Playback failed.	   
		statusChanged(MF_ERROR);
		return;
    }

    switch (pEventHeader->eEventType) {
    case MFP_EVENT_TYPE_PLAY: 	
		statusChanged(STARTED);
        break;

    case MFP_EVENT_TYPE_MEDIAITEM_SET: 
		statusChanged(LOADED);
        break;

    case MFP_EVENT_TYPE_PLAYBACK_ENDED: 	
		statusChanged(ENDED);
        break;
	}
}




