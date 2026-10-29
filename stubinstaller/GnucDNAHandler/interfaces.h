
#ifndef __INTERFACES__
#define __INTERFACES__

#include "stdafx.h"

struct PackedIPPort;
struct packet_UpdateResponse;

struct DebugDisplayer
{
	virtual void UpdateWindowText( const char * szNewText ){}
	virtual void AppendWindowText( const char * szNewText ){}
	virtual void UpdateStatusText( const char * szNewText ){}
};

struct StatusDisplayer
{
	virtual void UpdateStatusBar( const char * szNewText ){}
};

struct ProgressDisplayer
{
	virtual void UpdateProgressBar( long posNow, long posMax ){}
};

struct ProtocolTestingCallbacks
{
	virtual void gotPongUHC( PackedIPPort * ips, size_t count ){}
	virtual void gotUpdateResponse( packet_UpdateResponse * response, PackedIPPort &src ){}

	//	Used to prioritize hosts known to be up.
	virtual void gotPongUDP( PackedIPPort * source ){}
};

struct WebCacheCallbacks
{
	virtual void gotWebCacheHost( PackedIPPort * host ){}
};

struct XmlParser
{
	virtual void parseUpdateResponse( const char * src, size_t len ){}
	virtual bool receivedEnoughUpdates( void ){ return true; }

	//	These are expected to return a pointer to a string which does NOT
	//		need to be freed.  IE, it points into the buffer of one of the
	//		earlier packets which were passed into parseUpdateResponse()
	virtual const char * getReleaseBitprint( void ){ return NULL; }
	virtual const char * getReleaseFileName( void ){ return NULL; }
	virtual const char * getReleaseICommand( void ){ return NULL; }

	virtual const char * getBetaBitprint( void ){ return NULL; }
	virtual const char * getBetaFileName( void ){ return NULL; }
	virtual const char * getBetaICommand( void ){ return NULL; }

};

struct TimerControl
{
	virtual void setTimerDelay( unsigned int millis ){}
	virtual void setTimerDelayDefault( void ){}
	virtual void killTimer( void ){}
};

struct UserInterface
{
	virtual void setTitleText( const char * szTitle ){}
	virtual bool queryUser( const char * szMsg ){ return false; }
	virtual void informUser( const char * szMsg ){}
	virtual void hideWindow( void ){}
};

struct ClipboardHandler
{
	virtual void copyTextToClipboard( const char * szText ){}
};

struct AppKiller
{
	virtual void killApp( void ){}
};

struct RuntimeMethodLoader
{
	typedef BOOL (WINAPI *pfnGetProfilesDirectory)(IN LPSTR, IN OUT LPDWORD);
	virtual pfnGetProfilesDirectory getGetProfilesDirectoryMethod( void ){return NULL;}
};

#endif




