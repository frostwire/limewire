#include "StdAfx.h"

#include "gnucdnahandler.h"

#include <direct.h>
#include <process.h>

#include "Lmcons.h"
//#include "Userenv.h"

#ifndef __ICOMMANDPARSER__
#include "ICommandParser.h"
#endif
//	To register callbacks
//#include "GnuProtocol.h"
//#include "GnuCache.h"

//	Callbacks moved into DnaCore.h
#include "DnaCore.h"
#include "DnaDownload.h"
#include "DnaPrefs.h"
#include "DnaCache.h"
#include "DnaNetwork.h"

#include "GnuPackets.h"

#include "Hash/Sha1.h"

//----------------------------------------------------
#define STUB_VERSION			26
//----------------------------------------------------
#define DOWNLOAD_PATH		"C:\\My Downloads\\"
#define PARTIALS_PATH		"C:\\My Downloads\\Incomplete\\"
//-----
#define APP_FOLDER			".limewire\\"
#define SHARE_FOLDER		".NetworkShare\\"
#define PARTIAL_FOLDER		"Incomplete\\"
#define HOST_FILE			"gnutella.net"
#define INSTALLER_FILE		"LimeWireWinInstaller.exe"
//----------------------------------------------------
//#define FAIL_IMMEDIATELY		//	used to skip the download, and go right to the full installer web page
//#define FAIL_FROM_WEBSITE		//	used to simulate a failure from the LW website
//#define FAIL_LAUNCHING_URL  	//	used to skip the external browser launch
//----------------------------------------------------
#define DO_GET_HOSTS
#define DO_GET_UPDATE
#define DO_DOWNLOAD
#define DO_SUCCESS_REPORTING
//----------------------------------------------------
#define USE_BETA_UPDATE
//#define LOAD_XML_FILE_FROM_DISK		//	INCOMPLETE
//----------------------------------------------------
//#define INCLUDE_FAKE_UHC
#define INCLUDE_LW_UHCS
//#define INCLUDE_ZLATIN_UHC
#define INCLUDE_NON_LW_UHCS
//----------------------------------------------------
#define INCLUDE_POTENTIAL_NONLW_SOURCES
#define INCLUDE_OFFLINE_SOURCES_IN_HOSTS_FILE
//----------------------------------------------------
//#define GIVE_USER_OPTION_TO_INSTALL
//----------------------------------------------------
#define MAX_UHC_ATTEMPTS		25		//	Normal
#define HIT_UHC_EVERY_N_SECONDS	2
#define MAX_WEBCACHE_ATTEMPTS	6
#define WEBSITE_FAILURE_TIME	10
#define FINAL_WAIT_SECONDS		60
//----------------------------------------------------
#pragma message( __FILE__ ":### FINISH adding a failure timer for host UpdateRequests" )
//#define UPDATE_FROM_LOCALHOST
//#define UPDATE_ALTERNATE_FROM_LOCALHOST_AND_UHC_RCVD_IPS
//#define UPDATE_FROM_ZLATIN
//----------------------------------------------------
#define MAX_SIMULTANEOUS_UPDATE_REQUESTS	10
#define MIN_DESIRED_HOSTS					125
//----------------------------------------------------
//#define DOWNLOAD_CHUNK_SIZE					1024*64
//----------------------------------------------------
#define EXTERNAL_TEST_ADDRESS	"www.google.com"
#define EXTERNAL_TEST_PORT		80
#define SUCCESS_REPORTING_ADDR	"www9.limewire.com"
//----------------------------------------------------


char * szAddrUHCs[] = {
#ifdef INCLUDE_LW_UHCS
	"uhc3.limewire.com",
	"uhc2.limewire.com",
#endif
#ifdef INCLUDE_FAKE_UHC
	"66.66.66.66",			//	FAKE one
#endif
#ifdef INCLUDE_NON_LW_UHCS
	"uhc.udp-host-cache.com",
	"gwc.jooz.net",
	"kisama.ath.cx",
	"krill.shacknet.nu"		
#endif
};

unsigned short ports[] = {
#ifdef INCLUDE_LW_UHCS
	51180,
	20181,
#endif
#ifdef INCLUDE_FAKE_UHC
	32767,					//	FAKE one
#endif
#ifdef INCLUDE_NON_LW_UHCS
	9999,
	8010,
	8080,
	20095					//	returns invalid data
#endif
};

size_t uhcSize=sizeof(ports)/sizeof(ports[0]);

//----------------------------------------------------

bool verifyLimeUpdateResponsePacket( packet_UpdateResponse * packet );

//----------------------------------------------------

CGnucDNAHandler::CGnucDNAHandler(
	DebugDisplayer		&	display, 
	XmlParser			&	parser, 
	StatusDisplayer		&	status,
	ProgressDisplayer	&	progress,
	TimerControl		&	timer,
	UserInterface		&	ui,
	ClipboardHandler	&	clipboard,
	AppKiller			&	killer,
	RuntimeMethodLoader	&	loader )
:m_display(display)
,m_parser(parser)
,m_progress(progress)
,m_status(status)
,m_timer(timer)
,m_ui(ui)
,m_clipboard(clipboard)
,m_killer(killer)
,m_loader(loader)
,m_lockIP1(&m_critIP)
,m_lockIP2(&m_critIP)
,m_lockResp1(&m_critResp)
,m_lockResp2(&m_critResp)
,m_lockExtern1(&m_critExtern)
,m_lockExtern2(&m_critExtern)
//,m_lockLookup1(&m_critLookup)
//,m_lockLookup2(&m_critLookup)
{
	m_bGotUpdateMessage=false;
	m_bDownloadFinished=false;

	m_bConnectCompleted=false;
	m_bConnectSucceeded=false;

	m_bDoneWithSuccessReporting=false;
	m_bWaitSuccessReporting=false;
	m_bLaunchedDownload=false;
	m_bBeginReportingSuccess=false;

	m_nConnectErrorCode=0;

	memset( m_addrBuf, 0, sizeof(m_addrBuf) );
	m_bAddrUpdatedCompleted=false;
	m_bAddrUpdatedSuccess=false;

	m_nDownloadIndex=-1;
	m_nWebDownloadIndex=-1;

	m_nCountUHCHits=0;
	m_nCountWebCache=0;

	m_pCore=0;

	m_bDynamicFileStrings=false;
	m_szHostsFile=0;
	m_szApplicationFolder=0;
	m_szInstallerFolder=0;
	m_szPartialFolder=0;
}

void CGnucDNAHandler::init( void )
{
	CDnaCore::setProtocolCallback( this );
	CDnaCore::setWebCacheCallback( this );

	m_bDynamicFileStrings=getAllocatingFileInfo(
		&m_szHostsFile,
		&m_szApplicationFolder,
		&m_szInstallerFolder,
		&m_szPartialFolder );

	//	Show the msgBox to the user before creating GnucDNA's listening socket (by creating core)
	m_ui.informUser(
		"The LimeWire Online Installer will now try to download files necessary for installation. "
		"If you have a firewall and see a warning, please allow the installer access to the internet. "
		"Press OK to continue." );

	//	OK, go ahead & create, & launch the external connection testing thread while we are at it.
	m_pCore=new CDnaCore;
	launchExternalConnection( EXTERNAL_TEST_ADDRESS, EXTERNAL_TEST_PORT );

	//	OK, go ahead and pre-lookup the success reporting address if possible...
	lookupAddress( 
		 SUCCESS_REPORTING_ADDR, 
		 m_addrBuf, 
		&m_bAddrUpdatedCompleted, 
		&m_bAddrUpdatedSuccess, 
//		&m_lockLookup1
		 NULL
		);

	bool bSuccess=true;

	//	Don't create the .limewire folder if we can't find the user's home folder.  =)
	if( m_bDynamicFileStrings )
	{
		int res0=m_bDynamicFileStrings?_mkdir( m_szApplicationFolder ):_mkdir( DOWNLOAD_PATH );
		assert( res0==0 || (res0==-1 && errno==EEXIST) );

		bSuccess &= ( res0==0 || (res0==-1 && errno==EEXIST) );
	}

	int res1=m_bDynamicFileStrings?_mkdir( m_szInstallerFolder ):_mkdir( DOWNLOAD_PATH );
	assert( res1==0 || (res1==-1 && errno==EEXIST) );

	bSuccess &= ( res1==0 || (res1==-1 && errno==EEXIST) );

	int res2=m_bDynamicFileStrings?_mkdir( m_szPartialFolder ):_mkdir( PARTIALS_PATH );
	assert( res2==0 || (res2==-1 && errno==EEXIST) );
	bSuccess &= ( res2==0 || (res2==-1 && errno==EEXIST) );

	if( bSuccess )
	{
		m_pCore->SetClientName( "LwSwarmedInstaller" );
		m_pCore->SetClientCode( "LWSI" );

		//	Clear old downloads...
		m_pCore->GetPrefs()->SetDownloadPath( m_bDynamicFileStrings?m_szInstallerFolder:DOWNLOAD_PATH );
		m_pCore->GetPrefs()->SetPartialsDir( m_bDynamicFileStrings?m_szPartialFolder:PARTIALS_PATH );
		
		//	Remove older downloads in the INCOMPLETE folder
		std::vector< int > downloadIds=m_pCore->GetDownload()->GetDownloadIDs();
		for( int i=0; i<downloadIds.size(); i++ )
		{
			m_pCore->GetDownload()->Stop( downloadIds[i] );
			m_pCore->GetDownload()->Remove( downloadIds[i] );
		}

		m_ui.setTitleText( "LimeWire Online Installer" );
	}
	else
		FailAndLaunchWebsite(__LINE__);
}

CGnucDNAHandler::~CGnucDNAHandler(void)
{
	if( m_pCore )
	{
		delete m_pCore;
		m_pCore=0;
	}

	m_lockIP2.Lock();
	while( m_ips.size() )
	{
		PackedIPPort * ptr=m_ips.back();
		m_ips.pop_back();
		delete ptr;
	}
	m_lockIP2.Unlock();

	m_lockResp2.Lock();
	while( m_resps.size() )
	{
		PacketSource *ptr=m_resps.back();
		m_resps.pop_back();
		delete ptr->packet;
		delete ptr;
	}
	m_lockResp2.Unlock();

	while( m_verified.size() )
	{
		PacketSource *ptr=m_verified.back();
		m_verified.pop_back();
		delete ptr->packet;
		delete ptr;
	}


	if( m_szHostsFile )
	{
		delete m_szHostsFile;
		m_szHostsFile=0;
	}
	if( m_szApplicationFolder )
	{
		delete m_szApplicationFolder;
		m_szApplicationFolder=0;
	}
	if( m_szInstallerFolder )
	{
		delete m_szInstallerFolder;
		m_szInstallerFolder=0;
	}
	if( m_szPartialFolder )
	{
		delete m_szPartialFolder;
		m_szPartialFolder=0;
	}
}

bool CGnucDNAHandler::SHA1( BYTE * input, size_t inputSize, BYTE * output, size_t outputSize )
{
	if( outputSize>=20 )
	{
		sha_ctx     Sha1_Context;
		byte		Sha1_Digest[20];

		sha_init(&Sha1_Context);
		memset( Sha1_Digest,  0, sizeof(Sha1_Digest) );

		sha_update(&Sha1_Context, (byte*) input,  inputSize);

		sha_final(&Sha1_Context);
		sha_digest(&Sha1_Context,	Sha1_Digest);

		memcpy( output, Sha1_Digest, sizeof(Sha1_Digest) );
		return true;
	}
	else
		return false;
}

//-------------------------------

void CGnucDNAHandler::gotPongUHC( PackedIPPort * ips, size_t count )
{
	//	Display received IPs
	int numIPs = count;
	int  offset=0, maxOffset=40;

	//	This will occur on GnucDNA UDP receiver's thread
	m_lockIP1.Lock();
	CDnaDownload * pdl=m_pCore->GetDownload();
	for( int i=0; i<count; i++ )
	{
		m_ips.push_back( new PackedIPPort(ips[i]) );

#ifdef INCLUDE_POTENTIAL_NONLW_SOURCES
		//	While we are actively downloading, add any new nodes we learn 
		//		about as potential sources for the installer DL
		if( m_nDownloadIndex )
		{
#ifdef USE_BETA_UPDATE
			CString hashBitprint=m_parser.getBetaBitprint()+13;
#else
			CString hashBitprint=m_parser.getReleaseBitprint()+13;
#endif
			hashBitprint=hashBitprint.Left(32 + 39 + 1);
			CString hashSha1=hashBitprint.Left(32);

			CBUF(szBuf,512);
			sprintf( szBuf, "http://%s:%d/uri-res/N2R?urn:sha1:%s",
				ips[i].stringIP(), ips[i].port,	hashSha1 );

			pdl->AddSource(
				m_nDownloadIndex,
				NETWORK_WEB,
				szBuf );
		}
#endif
	}
	size_t ipSize=m_ips.size();
	m_lockIP1.Unlock();

	//	While we know of fewer than MIN_DESIRED_HOSTS IP&Ports, try UHCPinging some of the hosts we are just learning about
	if( ipSize<MIN_DESIRED_HOSTS )
	{
		packet_Ping_Static_GGEP ping;
		CDnaNetwork *pNetwork=m_pCore->GetNetwork();
		CAsyncSocket *socket=pNetwork->GetUdpSocket();
		BOOL success=TRUE;

		if( socket )
			for( int i=0; success && i<count && i+ipSize<MIN_DESIRED_HOSTS; i++ )
				//	Send this host a UHC Ping
				success=socket->SendTo(
					&ping,
					sizeof(ping),
					ips[i].port,
					ips[i].stringIP() );					
	}

}


void CGnucDNAHandler::gotUpdateResponse( packet_UpdateResponse * response, PackedIPPort &src )
{
	m_lockResp1.Lock();
	//	The below line allocates some memory for a duplicate of the message.
	m_resps.push_back( new PacketSource(response->allocatingDuplicate(),src) );
	m_lockResp1.Unlock();


	m_ipsToPrioritize.push_back( src );
}

void CGnucDNAHandler::gotWebCacheHost( PackedIPPort * host )
{
	//	Add each host received to the host list
	m_lockIP1.Lock();
	m_ips.push_back( new PackedIPPort(*host) );
	m_lockIP1.Unlock();
}

void CGnucDNAHandler::gotPongUDP( PackedIPPort *source )
{
#pragma message( __FILE__ ":### Add a specific GGEP to udp ping/pong pairs to identify the client" )
	//	Add the source to the priority download source queue
	m_ipsToPrioritize.push_back( *source );
}

//-------------------------------

void CGnucDNAHandler::doTimer( void )
{
	static bool bFirst=true;
	if( bFirst )
	{
#pragma message( __FILE__ ":### DAVE finish loading the XML update file from disk here..." )
		bFirst=false;

#ifdef LOAD_XML_FILE_FROM_DISK
		m_lockResp2.Lock();
		//	The below line allocates some memory for a duplicate of the message.
//		m_resps.push_back( new PacketSource(response->allocatingDuplicate(),src) );
		m_lockResp2.Unlock();
#endif
	}

	{
		if( m_nWebDownloadIndex >= 0 )
			doWebDownloadTimer();
		else if( !m_bGotUpdateMessage )
			doHostsAndUpdateMessageTimer();
		else if( !m_bDownloadFinished )
			doDownloadTimer();
		else if( !m_bLaunchedDownload )
			doFinishedTimer();
		else 
		{
			static time_t msecs=time(NULL);
			time_t now=time(NULL);

			if( (now-msecs)>1000*FINAL_WAIT_SECONDS || !m_bWaitSuccessReporting || m_bDoneWithSuccessReporting )
			{
				m_timer.killTimer();
				m_killer.killApp();
			}
		}
	}
}

//-------------------------------

void CGnucDNAHandler::doHostsAndUpdateMessageTimer( void )
{
	if( !m_pCore->GetNetwork()->GetUdpSocket() )
		 m_pCore->GetNetwork()->StartUdpListener();

#ifndef FAIL_IMMEDIATELY
	//---------------------------------------
	//	First, try to verify any remaining UpdateResponses we have floating around.

	//	Begin locked section for UPDATE_RESPONSEs
	m_lockResp2.Lock();
	int sizeB=m_resps.size();
	PacketSource*ps;
	packet_UpdateResponse * pur;
	if( sizeB )
	{
		ps=m_resps.back();
		pur=ps->packet;
		m_resps.pop_back();
	}
	m_lockResp2.Unlock();
	//	End locked section

	if( sizeB )
	{
		//	Record a Update attempt in the stats..
		m_stats.RetrievingUpdate();

#ifdef DO_DOWNLOAD
		bool success=verifyLimeUpdateResponsePacket( pur );

		//	Only hold onto the packet if it has passed DSA verification.
		if( success )
		{
			m_verified.push_back( ps );
			m_parser.parseUpdateResponse( (const char *)pur->getPayload(), pur->getPayloadLength() );
		}
		else
			delete pur;

		m_bGotUpdateMessage=success?m_parser.receivedEnoughUpdates():false;

		//	Begin a download for the installer
		if( m_bGotUpdateMessage )
		{
			m_display.UpdateStatusText( "Beginning download" );
			m_status.UpdateStatusBar("Attempting download");
			CDnaDownload *pDL=m_pCore->GetDownload();
#ifdef USE_BETA_UPDATE
			CString hashBitprint=m_parser.getBetaBitprint()+13;
#else
			CString hashBitprint=m_parser.getReleaseBitprint()+13;
#endif
			hashBitprint=hashBitprint.Left(32 + 39 + 1);
			CString hashSha1=hashBitprint.Left(32);
			CString hashTiger=hashBitprint.Mid(33,39);

			m_nDownloadIndex=pDL->DownloadFile(
				 INSTALLER_FILE,
				 NULL,
				 HASH_BITPRINT,
				 hashBitprint
#ifdef DOWNLOAD_CHUNK_SIZE
				 ,DOWNLOAD_CHUNK_SIZE
#endif
				 );	//	"urn:bitprint:" needs to be skipped

			//	Not needed, calling DownloadFile() triggers ReSearch() anyway.
			//m_pCore->GetDownload()->ReSearch( m_nDownloadIndex );

			//	Add some initial LW sources, hopefully they will have the installer
			{
				int i;
				CBUF(szBuf,512);
				
				for( i=0; i<m_verified.size(); i++ )
				{
					PacketSource *ps=m_verified[i];
					sprintf( szBuf, "http://%d.%d.%d.%d:%d/uri-res/N2R?urn:sha1:%s",
						ps->source.octet[0], ps->source.octet[1], 
						ps->source.octet[2], ps->source.octet[3],
						ps->source.port,	 hashSha1 );

					pDL->AddSource( 
						m_nDownloadIndex, 
						NETWORK_WEB,		//	Required by GnucDNA
						szBuf );
				}

				m_lockResp2.Lock();
				for( i=0; i<m_resps.size(); i++ )
				{
					PacketSource *ps=m_resps[i];
					sprintf( szBuf, "http://%d.%d.%d.%d:%d/uri-res/N2R?urn:sha1:%s",
						ps->source.octet[0], ps->source.octet[1], 
						ps->source.octet[2], ps->source.octet[3],
						ps->source.port,	 hashSha1 );

					pDL->AddSource( 
						m_nDownloadIndex, 
						NETWORK_WEB,		//	Required by GnucDNA
						szBuf );
				}
				m_lockResp2.Unlock();

#ifdef INCLUDE_POTENTIAL_NONLW_SOURCES
				m_lockIP2.Lock();
				for( i=0; i<m_ips.size(); i++ )
				{
					PackedIPPort *ps=m_ips[i];
					sprintf( szBuf, "http://%d.%d.%d.%d:%d/uri-res/N2R?urn:sha1:%s",
						ps->octet[0],	ps->octet[1], 
						ps->octet[2],	ps->octet[3],
						ps->port,		hashSha1 );

					pDL->AddSource( 
						m_nDownloadIndex, 
						NETWORK_WEB,		//	Required by GnucDNA
						szBuf );
				}
				m_lockIP2.Unlock();
#endif
			}
		}

		//	Begin speeding timer up so status text updates faster
		//m_timer.setTimerDelay( 1000 );
		m_timer.setTimerDelay( 333 );

		return;
#endif
	}

#ifndef LOAD_XML_FILE_FROM_DISK
	//---------------------------------------
	//	Second, try to solicit more UpdateResponses from known IP&Ports
	//	Delegate this out...
	{
		bool bSuccess=false;
		CDnaNetwork *pNetwork=m_pCore->GetNetwork();
		CAsyncSocket *socket=pNetwork->GetUdpSocket();

		if( !socket )
		{
			pNetwork->StartUdpListener();
			socket=pNetwork->GetUdpSocket();
		}

		m_lockIP2.Lock();
		if( m_ips.size() )
		{
			m_display.UpdateStatusText( "Retrieving signed update message" );
			m_timer.setTimerDelay( 500 );
#ifdef DO_GET_UPDATE
			bSuccess=true;

			//	Record a Update attempt in the stats..
			m_stats.RetrievingUpdate();
#endif
		}
		m_lockIP2.Unlock();

#ifdef MAX_SIMULTANEOUS_UPDATE_REQUESTS
		for( int i=0; bSuccess && i<MAX_SIMULTANEOUS_UPDATE_REQUESTS; i++ )
#endif
		{
			CString addr;
			unsigned short port;

#ifdef UPDATE_FROM_LOCALHOST
			port=6346;
			addr="127.0.0.1";
#elif defined(UPDATE_FROM_ZLATIN)
			port=6346;
			addr="10.254.0.44";
#elif defined(UPDATE_ALTERNATE_FROM_LOCALHOST_AND_UHC_RCVD_IPS)
			{
				static bool bToggle=false;
				if( bToggle )
				{
					addr="127.0.0.1";
					port=6346;
				}
				else
					bSuccess=getUpdateAddress( &addr, &port );

				bToggle = !bToggle;
			}
#else
			bSuccess=getUpdateAddress( &addr, &port );
#endif
			if( bSuccess )
			{
				packet_UpdateRequestV2 req;

				if( socket )
					//	Send the UDP ping
					bSuccess=socket->SendTo(
						&req, 
						sizeof(req),
						port, 
						addr
						);
				else
					FailAndLaunchWebsite(__LINE__);
			}
		}

		if( bSuccess )
			return;	
	}

	if( m_nCountUHCHits++ <= MAX_UHC_ATTEMPTS )
	//---------------------------------------
	//	Third, try to locate more IP&Ports
	{
		//	Record a UHC/WebCache attempt in the stats..
		m_stats.ContactingUHCs();

		static int countUHC=HIT_UHC_EVERY_N_SECONDS-1;

TRACE1( "Timer says %d\n", countUHC+1 );
		if( 0==(++countUHC)%HIT_UHC_EVERY_N_SECONDS )
		{
TRACE1( "Got inside on timer %d\n", countUHC );

			m_display.UpdateStatusText( "Locating installer sources" );
#ifdef DO_GET_HOSTS
			//	We need to send a UHC pong, since we have no ips
			CDnaNetwork *pNetwork=m_pCore->GetNetwork();
			CAsyncSocket *socket=pNetwork->GetUdpSocket();
			if( !socket )
			{
				pNetwork->StartUdpListener();
				socket=pNetwork->GetUdpSocket();
			}
		

			BOOL succeeded=TRUE;

			//	Try to send a UDP packet via the GnucDNA core
			packet_Ping_Static_GGEP 
							packet;

			int index=rand()%uhcSize;

			if( socket )
			{
				//	Send some UDP UHC pings
				succeeded=socket->SendTo( 
					&packet, 
					sizeof(packet),
					ports[index],
					szAddrUHCs[index]
					);
			}
			else
				FailAndLaunchWebsite(__LINE__);

#endif
		}
	}
	else if( m_nCountWebCache++ <= MAX_WEBCACHE_ATTEMPTS )
	{
		//	Record a UHC/WebCache attempt in the stats..
		m_stats.ContactingWebcache();

		CDnaCache * ptr=m_pCore->GetCache();
		static bool bFirst=true;

		if( bFirst )
		{
			m_display.UpdateStatusText( "Locating installer sources via GWebCache" );

			ptr->AddWebCache(
				"http://crab2.dyndns.org:30002/gwc/" 
				);

			ptr->AddWebCache(
				"http://gwc.nonamer.ath.cx:8080/"
				);

			ptr->AddWebCache(
				"http://gwcrab.sarcastro.com:8001/"
				);

			ptr->AddWebCache(
				"http://kisama.ath.cx:8080/"
				);

			ptr->AddWebCache(
				"http://loot.alumnigroup.org/"
				);

			ptr->AddWebCache(
				"http://overbeer.ghostwhitecrab.de/"
				);

			ptr->AddWebCache(
				"http://toadface.bishopston.net:3558/"
				);

		}
		bFirst=false;

		ptr->TryWebCache( NULL );
	}
	else 
#endif
#endif
		if( m_nWebDownloadIndex==-1 )
	//	We have attempted to contact UHCs & WebCaches too many times, and don't appear to be getting anywhere,
	//		so try starting a web download from the LimeWire website...
	{
		m_display.UpdateStatusText( "Beginning download from website" );
		CDnaDownload *pDL=m_pCore->GetDownload();

		m_nWebDownloadIndex=pDL->DownloadFile(
			"LimeWireWinInstaller.exe",
				NULL,
				HASH_UNKNOWN,
				"",
				1024 * 1024 * 50 );		//	This should ask for the entire file in one chunk.

		OSVERSIONINFO osv;
		memset(&osv,0,sizeof(osv));
		osv.dwOSVersionInfoSize=sizeof(osv);

		//	Produce the external connection string
		const char * strExt=0;
		DWORD socketError=-1;
		if( !m_lockExtern2.Lock( 1000 ) )
			strExt="xtc=NULL&xts=NULL";
		else
		{
			socketError=m_nConnectErrorCode;

			if( !m_bConnectCompleted )
				strExt="xtc=FALSE&xts=NULL";
			else
			{
				if( m_bConnectSucceeded )
					strExt="xtc=TRUE&xts=TRUE";
				else
					strExt="xtc=TRUE&xts=FALSE";
			}

			m_lockExtern2.Unlock();
		}

		assert( strExt );

		//	Prepare the FULL INSTALLER url
		CBUF( szBuf, 512 );
		if( GetVersionEx( &osv ) )
			//	Add a Web source from the website
			sprintf( szBuf, "http://www9.limewire.com/download/LimeWireWin-full.exe"
				"?ver=%d&%s&ser=%d&gotUPD=%d&cntUHC=%d&dlID=%d&ipSize=%d&rSize=%d&winver=%d.%d",
				STUB_VERSION,
				strExt,
				socketError,
				(m_bGotUpdateMessage?1:0),
				(m_nCountUHCHits),
				(int)m_nDownloadIndex,
				(int)m_ips.size(),
				(int)m_resps.size(),
				(int)osv.dwMajorVersion,
				(int)osv.dwMinorVersion
			);
		else
			//	Add a Web source from the website
			sprintf( szBuf, "http://www9.limewire.com/download/LimeWireWin-full.exe"
				"?ver=%d&%s&ser=%d&gotUPD=%d&cntUHC=%d&dlID=%d&ipSize=%d&rSize=%d&winver=failed",
				STUB_VERSION,
				strExt,
				socketError,
				(m_bGotUpdateMessage?1:0),
				(m_nCountUHCHits),
				(int)m_nDownloadIndex,
				(int)m_ips.size(),
				(int)m_resps.size()
			);

		pDL->AddSource(
			m_nWebDownloadIndex,
			NETWORK_WEB,
#ifdef FAIL_FROM_WEBSITE
			"http://localhost:39999/fubar.html"
#else
			szBuf 
#endif
			);

		m_timer.setTimerDelay( 333 );	//	Speed up updates during download
	}
	//	We have attempted to contact UHCs & WebCaches too many times, and don't appear to be getting anywhere
	else
		FailAndLaunchWebsite(__LINE__);
}

//-------------------------------

void CGnucDNAHandler::doWebDownloadTimer( void )
{
	if( m_nWebDownloadIndex>0 )
	{
		CDnaDownload *dlPtr = m_pCore->GetDownload();

		LONG activeSources		=	dlPtr->GetActiveSourceCount( m_nWebDownloadIndex );
		LONG totalSources		=	dlPtr->GetSourceCount( m_nWebDownloadIndex );
		LONG bytesCompleted		=	dlPtr->GetBytesCompleted( m_nWebDownloadIndex );
		LONG fileLength			=	dlPtr->GetFileLength( m_nWebDownloadIndex );
		LONG bytesPerSec		=	dlPtr->GetBytesPerSec( m_nWebDownloadIndex );

		CBUF(szBuf,1023);
		sprintf( szBuf, "Downloading installer from LimeWire website, saved %d KB of %d KB @ %d kbps",
			 bytesCompleted>>10, fileLength>>10, 
			 bytesPerSec>>7 );


		if(  dlPtr->GetXferSecs(m_nWebDownloadIndex) > WEBSITE_FAILURE_TIME
		  && dlPtr->GetBytesCompleted(m_nWebDownloadIndex) < 1024
		  )
		{
			//	Failure...

			m_nWebDownloadIndex=-2;
			return;
		}

		m_display.UpdateStatusText( szBuf );

		if( fileLength )
			m_progress.UpdateProgressBar( bytesCompleted, fileLength );

		if( fileLength && fileLength==bytesCompleted )
		{
			m_bDownloadFinished=true;
			m_bGotUpdateMessage=true;					//	HACK so we avoid attempting to re-retrieve the update message
            m_nDownloadIndex = m_nWebDownloadIndex;		//	So we run the right file in doFinishedTimer()
			m_nWebDownloadIndex = -3;					//	Done downloading

			m_status.UpdateStatusBar( "Download completed" );
			m_display.UpdateStatusText( "Download completed" );
			m_display.UpdateWindowText( "" );
		}
	}
}

//-------------------------------

void CGnucDNAHandler::saveHostsFile( void )
{
	FILE * fptr = fopen( m_szHostsFile, "a+" );
	if( fptr )
	{
		CDnaDownload *dl=m_pCore->GetDownload();
		std::vector< int > src=dl->GetSourceIDs( m_nDownloadIndex );
		std::vector< int > goodSrc, badSrc;
		std::vector< int >::const_iterator it;

		for( it=src.begin(); it!=src.end(); it++ )
		{
#if 0
			if( dl->GetSourceSpeed(m_nDownloadIndex,*it)>0 )
#else
			if( dl->GetSourceVendor(m_nDownloadIndex,*it).MakeLower().Find("limewire")!=-1 )
#endif
				goodSrc.push_back(*it);
			else
				badSrc.push_back(*it);
		}

		for( it=goodSrc.begin(); it!=goodSrc.end(); it++ )
		{
			ULONG ip = dl->GetSourceIP( m_nDownloadIndex, *it );
			USHORT port = dl->GetSourcePort( m_nDownloadIndex, *it );
			fprintf( fptr, "%d.%d.%d.%d:%d,,,,,en,\n", 
				(ip      )&0xFF,
				(ip>>0x08)&0xFF,
				(ip>>0x10)&0xFF,
				(ip>>0x18)&0xFF,
				port );
		}
#ifdef INCLUDE_OFFLINE_SOURCES_IN_HOSTS_FILE
#ifndef NDEBUG
		fprintf( fptr, "0.0.0.0:0000,,,,,en,\n" );
#endif
		for( it=badSrc.begin(); it!=badSrc.end(); it++ )
		{
			ULONG ip = dl->GetSourceIP( m_nDownloadIndex, *it );
			USHORT port = dl->GetSourcePort( m_nDownloadIndex, *it );
			fprintf( fptr, "%d.%d.%d.%d:%d,,,,,en,\n",
				(ip      )&0xFF,
				(ip>>0x08)&0xFF,
				(ip>>0x10)&0xFF,
				(ip>>0x18)&0xFF,
				port );
		}
#endif
		fclose( fptr );
	}
}

//-------------------------------

void CGnucDNAHandler::priorityLoop( void )
{
	std::vector< PackedIPPort > m_failed;
	CDnaDownload *dlPtr = m_pCore->GetDownload();

	while( m_ipsToPrioritize.size() )
	{
		PackedIPPort addr=m_ipsToPrioritize.back();
		LONG ip=addr.ip;

		m_ipsToPrioritize.pop_back();

		if( !dlPtr->PrioritizeSource(m_nDownloadIndex,ip) )
			m_failed.push_back( addr );
	}

	//	Keep storing IPs which aren't sources yet
	while( m_failed.size() )
	{
		PackedIPPort addr=m_failed.back();

#ifndef NDEBUG
{
TRACE1( "Failed prioritizing host %s\n", addr.stringIP() );
}
#endif
		//	Don't forget IPs that aren't sources yet
		m_ipsToPrioritize.push_back( addr );
		m_failed.pop_back();

		//	And add them as download sources while we are at it
		PackedIPPort *ps=&addr;

#ifdef USE_BETA_UPDATE
		CString hashBitprint=m_parser.getBetaBitprint()+13;
#else
		CString hashBitprint=m_parser.getReleaseBitprint()+13;
#endif
		hashBitprint=hashBitprint.Left(32 + 39 + 1);
		CString hashSha1=hashBitprint.Left(32);

		CBUF(szBuf,512);
		sprintf( szBuf, "http://%d.%d.%d.%d:%d/uri-res/N2R?urn:sha1:%s",
			ps->octet[0],	ps->octet[1], 
			ps->octet[2],	ps->octet[3],
			ps->port,		hashSha1 );

		dlPtr->AddSource( m_nDownloadIndex, NETWORK_WEB, szBuf );
	}
}

//-------------------------------

void CGnucDNAHandler::doDownloadTimer( void )
{
static bool bFirst=true;

bFirst=false;

	assert( m_verified.size()>0 );

	if( m_nDownloadIndex!=-1 )
	{
priorityLoop();

		CDnaDownload *dlPtr = m_pCore->GetDownload();

		LONG activeSources		=	dlPtr->GetActiveSourceCount( m_nDownloadIndex );
		LONG totalSources		=	dlPtr->GetSourceCount( m_nDownloadIndex );
		LONG bytesCompleted		=	dlPtr->GetBytesCompleted( m_nDownloadIndex );
		LONG fileLength			=	dlPtr->GetFileLength( m_nDownloadIndex );
		LONG bytesPerSec		=	dlPtr->GetBytesPerSec( m_nDownloadIndex );

		//	Record Download status in the stats..
		m_stats.DownloadingFile(
			totalSources,
			activeSources );

		CBUF(szBuf,1023);
		sprintf( szBuf, "Downloading installer from %d of %d sourc%s, saved %d KB of %d KB @ %d kbps",
			 activeSources, totalSources, 
			( (totalSources==1)?("e"):("es") ),
			 bytesCompleted>>10, fileLength>>10, 
			 bytesPerSec>>7 );

		m_display.UpdateStatusText( szBuf );

		if( fileLength )
			m_progress.UpdateProgressBar( bytesCompleted, fileLength );

#ifdef DO_SUCCESS_REPORTING
		if( fileLength && fileLength==bytesCompleted )
		{
			m_bDownloadFinished=true;
			m_status.UpdateStatusBar( "Download completed" );
			m_display.UpdateStatusText( "Download completed - Verifying download success, please wait" );
			m_display.UpdateWindowText( "" );

			//	Produce success string for LW server
			int a=m_stats.getUHCTimeSecs();
			int b=m_stats.getWebcacheTimeSecs();
			int c=m_stats.getUpdateTimeSecs();
			int d=m_stats.getDownloadTimeSecs();
			int e=m_stats.getSourcesActiveMax();
			int f=m_stats.getSourcesTotal();
			int g=fileLength / d >> 7;

			CBUF( szBuf, 256 );
			sprintf( szBuf, "ver=%d&tUHC=%d&tWEB=%d&tUPD=%d&tDLF=%d&cSAM=%d&cSTM=%d&aKbps=%d",
				STUB_VERSION, a, b, c, d, e, f, g );

			//	Prepare success request for LW server
			CBUF( szRequest, 2048 );
			const char * szHost=SUCCESS_REPORTING_ADDR;

			if( m_bAddrUpdatedCompleted && m_bAddrUpdatedSuccess )
//			{
//				m_lockLookup2.Lock();
				szHost=m_addrBuf;
//				m_lockLookup2.Unlock();
//			}

			sprintf( 
				 szRequest, 
				"GET /download/StubSuccess?%s HTTP/1.1\r\n"
				"Host: %s\r\n"
				"User-Agent: LwSwarmedInstaller\r\n"
				"Connection: Close\r\n"
				"Range: bytes=0-\r\n"
				"\r\n",
				 szBuf,
				 SUCCESS_REPORTING_ADDR );


			//	This call will launch a worker thread to do actual connecting.
			//		it has the side effect of setting a flag which causes the stub 
			//		to sit around waiting for that thread to complete.
			launchSuccessReporting(
				szHost,
				szRequest );
		}
#endif
	}
} 

//-------------------------------

void CGnucDNAHandler::doFinishedTimer( void )
{
	//	Be sure we didn't download from webservers directly
	if( m_nWebDownloadIndex==-1 && m_szHostsFile )
		saveHostsFile();

	CDnaDownload *pdl=m_pCore->GetDownload();
	CString fname=pdl->GetFilePath( m_nDownloadIndex );

#ifdef GIVE_USER_OPTION_TO_INSTALL
	CBUF(szBuf2,1024);
	sprintf( szBuf2, 
		"LimeWire Stub Installer has finished downloading the LimeWire installer:\n\"%s\"\n\nShould the installer be launched now?",
		(const char *)fname );

	bool launch=m_ui.queryUser( szBuf2 );
	if( launch )
#endif
	{
		CString str=pdl->GetFilePath(m_nDownloadIndex);
		ICommandParser p((const char *)str);

		//	ICommand
#ifdef USE_BETA_UPDATE
		const char * icmd = m_parser.getBetaICommand();
#else
		const char * icmd = m_parser.getReleaseICommand();
#endif

		if( icmd && icmd[0] )
		{
			//	Allocating call
			const char * command = p.ReplaceTokens( icmd );

			if( command )
			{
				//	OK we have a command to execute...  Now we need to break 
				//		apart the command into a seperate command and arguments portion.
				//		To do this, locate the first whitespace that does NOT occur inside
				//		a set of quotes.  This space should be the seperator between the
				//		command itself and the arguments to that command.
				const char * ptr=command;
				bool bInQuotes=false;

				while( ptr && *ptr )
				{
#pragma message( __FILE__ ":### FINISH deciding if single-quotes should work as well" )
					if(  *ptr=='\"' 
//					  || *ptr=
					  )
					{
						bInQuotes=!bInQuotes;
						ptr++;
						continue;
					}

					if( *ptr==' ' && !bInQuotes )
						break;

					ptr++;
					if( *ptr==0 )
						ptr=0;
				}

				//	At this point, one of XXX things has happened:
				//		1) We didn't find a space, so ptr==NULL
				//			Therefore, the entire command is the command to execute
				//		2) We found a space, and the next char is NULL
				//			Therefore, the entire command is the command to execute
				//		3) We found a space, and the next char is not NULL, so ptr is pointing to the space
				//			Therefore, command length is ptr-command+1, and param length
				//			is strlen(ptr+1)

				intptr_t res=0;

                if( ptr==NULL )			//	#1
					res=spawnl( _P_NOWAIT, command, NULL );
				else if( ptr[1]==NULL )	//	#2
					res=spawnl( _P_NOWAIT, command, NULL );
				else if( ptr[1]!=NULL )	//	#3
				{
					size_t paramLen = strlen(ptr+1);
					char * szCmd=0;	
					char * szParam = new char[ paramLen+1 ];
					if( command[0]=='\"' )
					{
						szCmd= new char[ptr-command+1-2];//	+1 is for the NULL, -2 for missing quotes
						strncpy( szCmd, command+1, ptr-command-2 );	//	Skip the quotes
						szCmd[ptr-command-2]=0;
					}
					else
					{
						szCmd= new char[ptr-command+1];//	+1 is for the NULL
						strncpy( szCmd, command, ptr-command );
						szCmd[ptr-command]=0;
					}
					strncpy( szParam, ptr+1, paramLen );
					szParam[paramLen]=0;
					res=spawnl( _P_NOWAIT, szCmd, szParam, NULL );
					delete [] szCmd;
					delete [] szParam;
				}
				else
					ASSERT( false );	//	????  WTF

				if( res==-1 )		//	Failed to launch!  Yuck!
				{
					DWORD error = GetLastError();
					pdl->RunFile( m_nDownloadIndex );
					assert( error==NULL );
				}
			}
			else
				pdl->RunFile( m_nDownloadIndex );

			delete [] command;
		}
		else
			pdl->RunFile( m_nDownloadIndex );
	}
#ifdef GIVE_USER_OPTION_TO_INSTALL
	else
	{
		CBUF(szBuf3,1024);
		sprintf( szBuf3, "To install LimeWire at a later date, run the following installer:\n\"%s\"", (const char *)fname );
		m_ui.informUser( szBuf3 );	
	}
#endif

	m_bBeginReportingSuccess=true;
	m_bLaunchedDownload=true;

	m_ui.hideWindow();

#if 0
	while( !m_bDoneWithSuccessReporting &&
			m_bWaitSuccessReporting )
		Sleep( 1000 );

	m_killer.killApp();
#endif
}

//-------------------------------

bool CGnucDNAHandler::getUpdateAddress( 
	CString			*	addr, 
	unsigned short	*	port )
{
	bool bReturn = false;
	PackedIPPort * pip = NULL;

	//	Grap an IP under lock if possible
	m_lockIP2.Lock();
	int sizeA = m_ips.size();
	if( sizeA )
	{
		pip = m_ips.back();
		m_ips.pop_back();
		bReturn=true;
	}
	m_lockIP2.Unlock();

	//	If we got an IP, then package it up...
	if( bReturn )
	{
		assert( pip );

		addr->Format("%d.%d.%d.%d",pip->octet[0],pip->octet[1],pip->octet[2],pip->octet[3]);
		*port = pip->port;

		delete pip;
	}

	return bReturn;
}

//-------------------------------

void CGnucDNAHandler::FailAndLaunchWebsite( int line )
{
	m_ui.informUser( 
		"The LimeWire Online Installer was unable to download the necessary files automatically. "
		"You will need to download them manually - press OK to continue"
		);

	CBUF( szBuf,512 );
	OSVERSIONINFO osv;
	memset(&osv,0,sizeof(osv));
	osv.dwOSVersionInfoSize=sizeof(osv);

	//	Produce the external connection string
	const char * strExt=0;
	DWORD socketError=-1;
	if( !m_lockExtern2.Lock( 1000 ) )
		strExt="xtc=NULL&xts=NULL";
	else
	{
		socketError=m_nConnectErrorCode;

		if( !m_bConnectCompleted )
			strExt="xtc=FALSE&xts=NULL";
		else
		{
			if( m_bConnectSucceeded )
				strExt="xtc=TRUE&xts=TRUE";
			else
				strExt="xtc=TRUE&xts=FALSE";
		}

		m_lockExtern2.Unlock();
	}

	assert( strExt );


	//	NOTE: MUST leave the URL in extensible condition (ie, must be able to add a '&FOO=BAR' after it)
	if( GetVersionEx( &osv ) )
		sprintf( szBuf, "http://www.limewire.com/english/content/stubfailure.shtml?ver=%d&lfail=%d&winver=%d.%d&%s&ser=%d", 
			STUB_VERSION, 
			line, 
			(int)osv.dwMajorVersion, 
			(int)osv.dwMinorVersion,
			strExt,
			socketError );
	else
		sprintf( szBuf, "http://www.limewire.com/english/content/stubfailure.shtml?ver=%d&lfail=%d&winver=failed&%s&ser=%d", 
			STUB_VERSION, 
			line,
			strExt,
			socketError	);

	int ires = -1;
#ifndef FAIL_LAUNCHING_URL

#pragma warning( disable: 4311 )	//	Cast from HRESULT to int
	ires = (int)ShellExecute( 
		 NULL,
		"open",
		 szBuf,
		 NULL,
		 "\\",
		 SW_SHOW
		);
#pragma warning( default: 4311 )	//	Cast from HRESULT to int

	//	F***in' Microsoft... 
	//		From MSDN:  Returns a value greater than 32 if successful, 
	//		or an error value that is less than or equal to 32 otherwise.
	if( ires<=32 )	//	Failed!
#endif
	{
		CBUF(szBuf2,1024);
		CBUF(szBuf3,512);

		sprintf( szBuf3, "%s&ires=%d", szBuf, ires );

		sprintf( szBuf2, 
			"Failed launching external browser, perhaps some popup-stopping program is preventing it?\n"
			"Try pointing your browser to the following address:\n\"%s\"\nNOTE: This address has been copied "
			"to the clipboard, so you can merely paste it into the address bar of your favorite browser.",
			szBuf3 );

		m_clipboard.copyTextToClipboard( szBuf3 );

		//m_ui.informUser( szBuf2 );
		MessageBox( NULL, szBuf2, "LimeWire Online Installer", NULL );
	}

	m_killer.killApp();
}

//-------------------------------
//#define APP_FOLDER		".limewire\\"
//#define SHARE_FOLDER		".NetworkShare\\"
//#define PARTIAL_FOLDER	".NetworkPartial\\"
//#define HOST_FILE			"gnutella.net"
//#define INSTALLER_FILE	"LimeWireWinInstaller.exe"

bool CGnucDNAHandler::getAllocatingFileInfo( 
	char			**	pszHosts,
	char			**	pszApplication,
	char			**	pszInstaller,
	char			**	pszPartial )
{
	BOOL bSuccess=TRUE;
	char * szPtrHosts=0;
	char * szPtrAppl=0;
	char * szPtrInst=0;
	char * szPtrPart=0;

	CBUF(szUser,UNLEN+1);
	CBUF(szProfileBase,MAX_PATH);

	DWORD nSizeUser=sizeof(szUser);
	DWORD nSizeProfile=sizeof(szProfileBase);

	if( bSuccess )
		bSuccess=GetVersion()<0x80000000;

#if 1
	//	Try to grab a runtime version of Win32's GetProfilesDirectory()
	if( bSuccess )
	{
		RuntimeMethodLoader::pfnGetProfilesDirectory pfn;
		pfn=m_loader.getGetProfilesDirectoryMethod();

		if( pfn )
			bSuccess=pfn( szProfileBase, &nSizeProfile );
		else
			bSuccess=false;
	}
#else
	//	Get user profile base folder
	if( bSuccess )
		bSuccess=GetProfilesDirectory( szProfileBase, &nSizeProfile );
#endif

	//	Get user name
	if( bSuccess )
		bSuccess=GetUserName( szUser, &nSizeUser );

	//	Concatinate with file name
	if( bSuccess )
	{
		DWORD nSizeApplication = 1		//	Term. null
			+ nSizeProfile-1			//	Remove null
			+ 1							//	"\"
			+ nSizeUser-1				//	Remove null
			+ strlen( APP_FOLDER )
			+ 1							//	"\"
			;

		DWORD nSizeHosts = 1			//	Term. null
			+ nSizeApplication-1		//	Remove null
			+ strlen( HOST_FILE )
			;

		DWORD nSizeInstaller = 1		//	Term. null
			+ nSizeApplication-1		//	Remove null
			+ strlen( SHARE_FOLDER )
			;

		DWORD nSizePartial = 1			//	Term. null
			+ nSizeInstaller-1			//	Remove null
			+ strlen( PARTIAL_FOLDER )
			;

		szPtrAppl = new char[nSizeApplication];
		szPtrHosts = new char[nSizeHosts];
		szPtrInst = new char[nSizeInstaller];
		szPtrPart = new char[nSizePartial];

		sprintf( szPtrAppl , "%s\\%s\\%s"  ,   szProfileBase, szUser, APP_FOLDER );
		sprintf( szPtrHosts, "%s\\%s\\%s%s",   szProfileBase, szUser, APP_FOLDER, HOST_FILE );
		sprintf( szPtrInst , "%s\\%s\\%s%s",   szProfileBase, szUser, APP_FOLDER, SHARE_FOLDER );
		sprintf( szPtrPart , "%s\\%s\\%s%s%s", szProfileBase, szUser, APP_FOLDER, SHARE_FOLDER , PARTIAL_FOLDER );
	}

	*pszHosts=szPtrHosts;
	*pszApplication=szPtrAppl;
	*pszInstaller=szPtrInst;
	*pszPartial=szPtrPart;
	return bSuccess?true:false;
}

//-------------------------------

struct ThreadParam
{
	CGnucDNAHandler	*	handler;
	const char		*	address;
	unsigned short		port;
};

//-------------------------------

void CGnucDNAHandler::launchExternalConnection( 
	const char		*	address,
	unsigned short		port )
{
	ThreadParam *p=new ThreadParam;
	char * sptr = new char[strlen(address)+1];
	strcpy( sptr, address );

	p->address=(const char *)sptr;
	p->handler=this;
	p->port=port;

	_beginthread( ThreadProc, 0, p );		//	Let the new thread destroy p->address & p at leisure
}

//-------------------------------

void CGnucDNAHandler::doExternalConnection(
	const char		*	address,
	unsigned short		port )
{
	BOOL bCompleted=FALSE;
	DWORD error=0;

	bCompleted=AfxSocketInit();

	if( bCompleted )
	{
		CSocket sock;
		if( bCompleted )
			bCompleted=sock.Create();

		if( bCompleted )
		{
			bCompleted=sock.Connect(
				address,
				port );				//	Socket destructor calls Close()

			if( !bCompleted )
				error=sock.GetLastError();

			sock.Close();
		}
	}

	m_lockExtern1.Lock();
	m_bConnectCompleted=true;
	m_bConnectSucceeded=bCompleted?true:false;
	m_nConnectErrorCode=error;
	m_lockExtern1.Unlock();
}

//-------------------------------

void ThreadProc( void * ptr )
{
	ThreadParam *p = (ThreadParam *)ptr;
	const char * pstr = p->address;
	unsigned short port=p->port;
	CGnucDNAHandler * ph = p->handler;
	delete p;

	ph->doExternalConnection( pstr, port );

	delete pstr;
}

//*******************************

struct ThreadParam2
{
	CGnucDNAHandler	*	handler;
	const char		*	address;
	const char		*	request;
};

//-------------------------------

void CGnucDNAHandler::launchSuccessReporting(
	const char		*	host,
	const char		*	request )
{
	ThreadParam2 *p=new ThreadParam2;

	char * sptr1 = new char[strlen(host)+1];
	char * sptr2 = new char[strlen(request)+1];
	strcpy( sptr1, host );
	strcpy( sptr2, request );

	p->handler=this;
	p->address=(const char *)sptr1;
	p->request=(const char *)sptr2;

	m_bWaitSuccessReporting=true;			//	make doFinishedTimer() wait for this thread to finish

	int res=_beginthread( ThreadProc2, 0, p );		//	Let the new thread destroy p->address, p->request, & p at leisure

	if( res==-1 )
		m_bDoneWithSuccessReporting=true;			//	Just in case...
}

//-------------------------------

void CGnucDNAHandler::doSuccessReporting(
	const char		*	host,
	const char		*	request )
{
	while( !m_bBeginReportingSuccess )
		Sleep(250);

	CSocket sock;

	BOOL success=true;

	success=AfxSocketInit();
	
	if( success )
		success=sock.Create();

	if( success )
	{
		success=sock.Connect(
			host,
			80 );

		if( success )
			int success=sock.Send(			
				request,
				strlen(request),
				NULL );

		sock.Close();
	}

	delete [] host;
	delete [] request;

	m_bDoneWithSuccessReporting=true;
}

//-------------------------------

void ThreadProc2( void * ptr )
{
	ThreadParam2 *p = (ThreadParam2 *)ptr;
	const char * pstr1 = p->address;
	const char * pstr2 = p->request;
	CGnucDNAHandler * ph = p->handler;

	delete p;

	ph->doSuccessReporting( pstr1, pstr2 );
}

//*******************************

struct ThreadParam3
{
	CGnucDNAHandler	*	handler;
	const char		*	address;
	char			*	stringIPptr;
	bool			*	completed;
	bool			*	success;
	CSingleLock		*	synch;
};

//-------------------------------

void CGnucDNAHandler::lookupAddress(
	const char		*	address,
	char			*	stringIPptr,	//	This pointer is updated if we are successful
	bool			*	completed,
	bool			*	success,
	CSingleLock		*	synchobj )		//	The synchronization object to use while setting strIPptr
{
	ThreadParam3 *p=new ThreadParam3;

	char * sptr = new char[strlen(address)+1];
	strcpy( sptr, address );
	p->address=(const char *)sptr;
	p->stringIPptr=stringIPptr;
	p->synch=synchobj;
	p->handler=this;
	p->completed=completed;
	p->success=success;

	_beginthread( ThreadProc3, 0, p );		//	Let the new thread destroy p->address & p at leisure
}

//-------------------------------

void CGnucDNAHandler::doLookupAddress(
	const char		*	address,
	char			*	stringIPptr,	//	This pointer is updated if we are successful
	bool			*	completed,
	bool			*	success,
	CSingleLock		*	synchobj )		//	The synchronization object to use while setting strIPptr
{
	if( AfxSocketInit() )
	{
		hostent * phe=gethostbyname( address );

		if( phe )
		{
			in_addr* p = (in_addr*) phe->h_addr_list[0];
			char * dottedIpAddr = inet_ntoa(*p);

			if( dottedIpAddr )
			{
				if( synchobj )
					synchobj->Lock();

				//	Copy the string...
				strcpy( stringIPptr, dottedIpAddr );

				if( synchobj )
					synchobj->Unlock();

				if( success )
					*success=true;
			}
		}
		else
		{
			int error = WSAGetLastError();
			assert( !error );
		}
	}

	if( completed )
		*completed=true;
}

//-------------------------------

void ThreadProc3( void * ptr )
{
	ThreadParam3 *p = (ThreadParam3 *)ptr;
	const char * pstr = p->address;
	char * pret = p->stringIPptr;
	bool * pcompleted=p->completed;
	bool * psuccess=p->success;
	CSingleLock * plock = p->synch;
	CGnucDNAHandler * ph = p->handler;

	delete p;

	ph->doLookupAddress( pstr, pret, pcompleted, psuccess, plock );

	delete pstr;
}

//-------------------------------
//-------------------------------










