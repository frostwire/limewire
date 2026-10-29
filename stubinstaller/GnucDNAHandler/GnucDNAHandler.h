#pragma once

#ifndef __GNUCDNAHANDLER__
#define __GNUCDNAHANDLER__


//#include "GnuProtocol.h"
#ifndef __INTERFACES__
#include "interfaces.h"
#endif

#ifndef __SUCCESSSTATS__
#include "SuccessStats.h"
#endif

#include "PackedIPPort.h"

struct packet_UpdateResponse;
struct DebugDisplayer;
class CDnaCore;

struct PacketSource
{
	PacketSource( packet_UpdateResponse * pur, PackedIPPort &src ){packet=pur;source=src;}
	packet_UpdateResponse *	packet;
	PackedIPPort			source;
};

class CGnucDNAHandler : public ProtocolTestingCallbacks, public WebCacheCallbacks
{
public:
	CGnucDNAHandler(
		DebugDisplayer		&	display, 
		XmlParser			&	parser, 
		StatusDisplayer 	&	status,
		ProgressDisplayer	&	progress,
		TimerControl		&	timer,
		UserInterface		&	ui,
		ClipboardHandler	&	clipboard,
		AppKiller			&	killer,
		RuntimeMethodLoader	&	loader );
	~CGnucDNAHandler(void);

	//	Should be called ASAP after construction...
public:
	void init( void );

	//	This is the workhorse of this class.  It is called by CChildView's OnTimer() function
public:
	void doTimer( void );
	
private:
	void doHostsAndUpdateMessageTimer( void );
	void doDownloadTimer( void );
	void doWebDownloadTimer( void );
	void doFinishedTimer( void );

	//	Utility function
private:
	void saveHostsFile( void );
	void priorityLoop( void );
	bool getUpdateAddress( CString * addr, unsigned short * port );
	void FailAndLaunchWebsite( int line );
	bool getAllocatingFileInfo( 	
		char			**	pszHosts,
		char			**	pszApplication,
		char			**	pszInstaller,
		char			**	pszPartial );

	//	Test outgoing connection is possible
	void launchExternalConnection( 
		const char		*	address,
		unsigned short		port );
	void doExternalConnection( 
		const char		*	address,
		unsigned short		port );
	friend void ThreadProc( void * );

	//	Submit success reporting stat
	void launchSuccessReporting(
		const char		*	host,
		const char		*	request );
	void doSuccessReporting(
		const char		*	host,
		const char		*	request );
	friend void ThreadProc2( void * );

	//	Address lookup thread operations...
	void lookupAddress(
		const char		*	address,
		char			*	stringIPptr,	//	This buffer is updated if successful
		bool			*	completed,
		bool			*	success,
		CSingleLock		*	synchobj );		//	The synchronization object to use while setting strIPptr
	void doLookupAddress(
		const char		*	address,
		char			*	stringIPptr,	//	This buffer is updated if successful
		bool			*	completed,
		bool			*	success,
		CSingleLock		*	synchobj );		//	The synchronization object to use while setting strIPptr
	friend void ThreadProc3( void * );

	//	Synch crap...
private:
	CCriticalSection				m_critIP;
	CCriticalSection				m_critResp;
	CMutex							m_critExtern;

	CSingleLock						m_lockIP1;
	CSingleLock						m_lockIP2;
	CSingleLock						m_lockResp1;
	CSingleLock						m_lockResp2;
	CSingleLock						m_lockExtern1;
	CSingleLock						m_lockExtern2;

	//	Other private stuff
private:
	DebugDisplayer				&	m_display;
	ProgressDisplayer			&	m_progress;
	StatusDisplayer				&	m_status;
	XmlParser					&	m_parser;
	TimerControl				&	m_timer;
	UserInterface				&	m_ui;
	ClipboardHandler			&	m_clipboard;
	AppKiller					&	m_killer;
	RuntimeMethodLoader			&	m_loader;

	CDnaCore					*	m_pCore;

	SuccessStats					m_stats;

	std::vector<PackedIPPort*>		m_ips;
	std::vector<PacketSource*>
									m_resps;
	std::vector<PacketSource*>
									m_verified;

	std::vector< PackedIPPort >		m_ipsToPrioritize;

	bool							m_bDynamicFileStrings;
	char						*	m_szHostsFile;
	char						*	m_szApplicationFolder;
	char						*	m_szInstallerFolder;
	char						*	m_szPartialFolder;

	char							m_addrBuf[256];
	bool							m_bAddrUpdatedCompleted;
	bool							m_bAddrUpdatedSuccess;

	bool							m_bGotUpdateMessage;
	bool							m_bDownloadFinished;

	bool							m_bConnectCompleted;
	bool							m_bConnectSucceeded;

	bool							m_bDoneWithSuccessReporting;
	bool							m_bWaitSuccessReporting;
	bool							m_bBeginReportingSuccess;
	bool							m_bLaunchedDownload;

	DWORD							m_nConnectErrorCode;

	int								m_nCountUHCHits;
	int								m_nCountWebCache;
	LONG							m_nDownloadIndex;
	LONG							m_nWebDownloadIndex;	//	-1 == not downloaded yet, -2 == failed, -3 == done


	//	ProtocolTestingCallbacks virtuals
public:
	virtual void gotPongUHC( PackedIPPort * ips, size_t count );
	virtual void gotUpdateResponse( packet_UpdateResponse * response, PackedIPPort &src );

	virtual void gotPongUDP( PackedIPPort * source );

	//	WebCacheCallbacks virtuals
public:
	virtual void gotWebCacheHost( PackedIPPort * host );

	//	Printing to the display
private:
	int PrintHex8Line( BYTE * p, char * szDest );
	int PrintHex16Line( BYTE * p, char * szDest );
	void PrintHexDump( char * szHeader, BYTE *ptr, int bytes, bool b16BytesPerLine=true );

public:
	static bool SHA1( BYTE * input, size_t inputSize, BYTE * output, size_t outputSize );
};



#endif