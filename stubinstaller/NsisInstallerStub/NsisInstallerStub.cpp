// NsisInstallerStub.cpp : Defines the initialization routines for the DLL.
//

#include "stdafx.h"
#include "assert.h"

#include "NsisInstallerStub.h"
#include "exdll.h"

#include "DnaCore.h"
#include "DnaPrefs.h"
#include "DnaDownload.h"

#ifdef _DEBUG
#define new DEBUG_NEW
#endif

#pragma warning(push)
#pragma warning(disable:4311)

//#define BREAK_INTO_DEBUGGER
//#define BREAK_INTO_DEBUGGER_IMMEDIATELY



//#define BANDWIDTH_LIMIT  	(256)		//	256 bytes per second == 2048 kbps
//#define BANDWIDTH_LIMIT  	(128)		//	128 bytes per second == 1024 kbps
//#define BANDWIDTH_LIMIT  	(64)		//	 64 bytes per second ==  512 kbps
//#define BANDWIDTH_LIMIT 	(48)		//	 48 bytes per second ==  384 kbps
//#define BANDWIDTH_LIMIT  	(32)		//	 32 bytes per second ==  256 kbps
//#define BANDWIDTH_LIMIT  	(28)		//	 28 bytes per second ==  224 kbps
//#define BANDWIDTH_LIMIT  	(24)		//	 24 bytes per second ==  192 kbps
//#define BANDWIDTH_LIMIT  	(20)		//	 20 bytes per second ==  160 kbps
//#define BANDWIDTH_LIMIT  	(16)		//	 16 bytes per second ==  128 kbps
//#define BANDWIDTH_LIMIT  	(12)		//	 12 bytes per second ==   96 kbps
//#define BANDWIDTH_LIMIT  	(8)     	//	  8 bytes per second ==   64 kbps
//#define BANDWIDTH_LIMIT  	(5)     	//	  5 bytes per second ==   40 kbps
//#define BANDWIDTH_LIMIT  	(3)     	//	  3 bytes per second ==   24 kbps

//
//	Note!
//
//		If this DLL is dynamically linked against the MFC
//		DLLs, any functions exported from this DLL which
//		call into MFC must have the AFX_MANAGE_STATE macro
//		added at the very beginning of the function.
//
//		For example:
//
//		extern "C" BOOL PASCAL EXPORT ExportedFunction()
//		{
//			AFX_MANAGE_STATE(AfxGetStaticModuleState());
//			// normal function body here
//		}
//
//		It is very important that this macro appear in each
//		function, prior to any calls into MFC.  This means that
//		it must appear as the first statement within the 
//		function, even before any object variable declarations
//		as their constructors may generate calls into the MFC
//		DLL.
//
//		Please see MFC Technical Notes 33 and 58 for additional
//		details.
//

// CNsisInstallerStubApp

BEGIN_MESSAGE_MAP(CNsisInstallerStubApp, CWinApp)
END_MESSAGE_MAP()


HMODULE     hModule;
HWND		g_parent;
HWND        g_hwndProgressBar;
HWND        g_hwndStatic;
static int  g_cancelled;
static int	g_completed;
static void *lpWndProcOld;

static const int		g_nMaxDownloadCount=128;	//	Max multiple files, arrays are prealloced to this
static int				g_nDownloadCount;			//	If non-zero, then this holds the 
static char ** 	g_szDownloadName;			//		depth of the arrays for all 
static char ** 	g_szDownloadPath;			//		the following statics
static char ** 	g_szDownloadIncompletePath;
static char ** 	g_szDownloadSha1Base32;
static char ** 	g_szDownloadSize;
static char ** 	g_szSourceUrl;


static UINT uMsgCreate;

HWND childwnd;
HWND hwndL;
HWND hwndB;

static const char * g_pszBuf="Got this far...";

// CNsisInstallerStubApp construction

CNsisInstallerStubApp::CNsisInstallerStubApp()
{
	// TODO: add construction code here,
	// Place all significant initialization in InitInstance

	m_wndProgress=NULL;
}


// The one and only CNsisInstallerStubApp object

CNsisInstallerStubApp theApp;


// CNsisInstallerStubApp initialization
BOOL CNsisInstallerStubApp::InitInstance()
{
	CWinApp::InitInstance();
	hModule=m_hInstance;

	hModule=NULL;
	g_hwndProgressBar=NULL;
	g_hwndStatic=NULL;
	g_cancelled=0;
	g_completed=0;
	lpWndProcOld=NULL;

	g_nDownloadCount=0;
	g_szDownloadName			=	new char *[g_nMaxDownloadCount];
	g_szDownloadPath			=	new char *[g_nMaxDownloadCount];
	g_szDownloadIncompletePath	=	new char *[g_nMaxDownloadCount];
	g_szDownloadSha1Base32		=	new char *[g_nMaxDownloadCount];
	g_szDownloadSize			=	new char *[g_nMaxDownloadCount];
	g_szSourceUrl				=	new char *[g_nMaxDownloadCount];

	uMsgCreate=NULL;

	childwnd=NULL;
	hwndL=NULL;
	hwndB=NULL;

	return TRUE;
}

static LRESULT CALLBACK ParentWndProc(HWND hwnd, UINT message, WPARAM wParam, LPARAM lParam)
{
  if (uMsgCreate && message == uMsgCreate)
  {
    static HWND hwndPrevFocus;
    static BOOL fCancelDisabled;

    if (wParam&0x0000FFFF)
    {
      childwnd = FindWindowEx((HWND) lParam, NULL, "#32770", NULL);
      hwndL = GetDlgItem(childwnd, 1016);
      hwndB = GetDlgItem(childwnd, 1027);
      HWND hwndP = GetDlgItem(childwnd, 1004);
      HWND hwndS = GetDlgItem(childwnd, 1006);
      if (childwnd && hwndP && hwndS)
      {
        // Where to restore focus to before we disable the cancel button
        hwndPrevFocus = GetFocus();
        if (!hwndPrevFocus)
          hwndPrevFocus = hwndP;

        if (IsWindowVisible(hwndL))
          ShowWindow(hwndL, SW_HIDE);
        else
          hwndL = NULL;
        if (IsWindowVisible(hwndB))
          ShowWindow(hwndB, SW_HIDE);
        else
          hwndB = NULL;

        RECT wndRect, ctlRect;

        GetClientRect(childwnd, &wndRect);

        GetWindowRect(hwndS, &ctlRect);

		HWND s = g_hwndStatic = CreateWindow(
          "STATIC",
          "",
          WS_CHILD | WS_CLIPSIBLINGS | SS_CENTER,
          0,
          wndRect.bottom / 2 - (ctlRect.bottom - ctlRect.top) /* *3 */ / 2,
          wndRect.right,
          ctlRect.bottom - ctlRect.top,
          childwnd,
          NULL,
          hModule,
          NULL
        );

        DWORD dwStyle = WS_CHILD | WS_CLIPSIBLINGS;
        dwStyle |= GetWindowLong(hwndP, GWL_STYLE) & PBS_SMOOTH;

        GetWindowRect(hwndP, &ctlRect);

        HWND pb = g_hwndProgressBar = CreateWindow(
          "msctls_progress32",
          "",
          dwStyle,
          0,
          wndRect.bottom / 2 + (ctlRect.bottom - ctlRect.top) / 2,
          wndRect.right,
          ctlRect.bottom - ctlRect.top,
          childwnd,
          NULL,
          hModule,
          NULL
        );

		theApp.m_wndProgress=new CProgressWnd( 
			pb, 
			CWnd::FromHandlePermanent(childwnd), 
			(wParam&0xFFFF0000)>>0x10 );

        long c;

        c = (long)SendMessage(hwndP, PBM_SETBARCOLOR, 0, 0);
        SendMessage(hwndP, PBM_SETBARCOLOR, 0, c);
        SendMessage(pb, PBM_SETBARCOLOR, 0, c);

        c = (long)SendMessage(hwndP, PBM_SETBKCOLOR, 0, 0);
        SendMessage(hwndP, PBM_SETBKCOLOR, 0, c);
        SendMessage(pb, PBM_SETBKCOLOR, 0, c);

        // set font
        long hFont = (long)SendMessage((HWND) lParam, WM_GETFONT, 0, 0);
        SendMessage(pb, WM_SETFONT, hFont, 0);
        SendMessage(s, WM_SETFONT, hFont, 0);

        ShowWindow(pb, SW_SHOWNA);
        ShowWindow(s, SW_SHOWNA);

        fCancelDisabled = EnableWindow(GetDlgItem(hwnd, IDCANCEL), TRUE);
        SendMessage(hwnd, WM_NEXTDLGCTL, (WPARAM)GetDlgItem(hwnd, IDCANCEL), TRUE);
#ifdef BREAK_INTO_DEBUGGER_IMMEDIATELY
_asm int 03h;
#endif
      }
      else
        childwnd = NULL;
    }
    else if (childwnd)
    {
      if (hwndB)
      {
        ShowWindow(hwndB, SW_SHOWNA);
        hwndB = NULL;
      }
      if (hwndL)
      {
        ShowWindow(hwndL, SW_SHOWNA);
        hwndL = NULL;
      }

      // Prevent wierd stuff happening if the cancel button happens to be
      // pressed at the moment we are finishing and restore the previous focus
      // and cancel button states
      SendMessage(hwnd, WM_NEXTDLGCTL, (WPARAM)hwndPrevFocus, TRUE);
      SendMessage(GetDlgItem(hwnd, IDCANCEL), BM_SETSTATE, FALSE, 0);
      if (fCancelDisabled)
        EnableWindow(GetDlgItem(hwnd, IDCANCEL), FALSE);

	  if (g_hwndStatic)
	  {
		  DestroyWindow(g_hwndStatic);
		  g_hwndStatic=NULL;
	  }
	  if( theApp.m_wndProgress )
	  {
		  theApp.m_wndProgress->UnsubclassWindow();
		  delete theApp.m_wndProgress;
		  theApp.m_wndProgress=0;
	  }
      if (g_hwndProgressBar)
      {
        DestroyWindow(g_hwndProgressBar);
        g_hwndProgressBar = NULL;
      }
      childwnd = NULL;

	  SetWindowLong(g_parent,GWL_WNDPROC,(long)lpWndProcOld);

	delete g_szDownloadName				;
	delete g_szDownloadPath				;
	delete g_szDownloadIncompletePath	;
	delete g_szDownloadSha1Base32		;
	delete g_szDownloadSize				;
	delete g_szSourceUrl				;

	}
  }
  else if (message == WM_COMMAND && LOWORD(wParam) == IDCANCEL)
  {
#ifdef BREAK_INTO_DEBUGGER
#ifndef NDEBUG
  _asm int 03h;
#endif
#else
    g_cancelled = 1;		//	Don't actually cancel when we hit the cancel button 
#endif						//		if we are trying to break into debugger  -DN
  }
  else
  {
    return CallWindowProc(
      (WNDPROC) lpWndProcOld,
      hwnd,
      message,
      wParam,
      lParam
    );
  }
  return 0;
}

//*****************************************************************************
//*****************************************************************************

BEGIN_MESSAGE_MAP(CProgressWnd, CProgressCtrl)
	ON_WM_TIMER()
END_MESSAGE_MAP()

//*****************************************************************************

CProgressWnd::CProgressWnd( HWND ctrlId, CWnd *pParent, int type )
{
	m_nType=type;
	m_nTimerId=0;
	m_szBuf[0]=0;
	m_pCore=0;

	m_nDownloadIdHolder=0;

	if( type==eSINGLE || type==eBITPRINT )
		m_nDownloadId=&m_nDownloadIdHolder;
	else
		m_nDownloadId=new long[g_nDownloadCount];

	BOOL bRet=SubclassWindow( ctrlId );
}

//*****************************************************************************

CProgressWnd::~CProgressWnd()
{
	if( m_pCore )
	{
		delete m_pCore;
		m_pCore=0;
	}
	if( m_nDownloadId && &m_nDownloadIdHolder!=m_nDownloadId )
	{
		//	Pointing to array
		assert( m_nType==eMULTIPLE );
		delete m_nDownloadId;
		m_nDownloadId=0;
	}
}

//*****************************************************************************

#pragma message( __FILE__ ":### DAVE Finish OnTimer() calls to update STATIC text" )
void CProgressWnd::OnTimer( UINT_PTR nIdEvent )
{
	static int nTimer=0;
	bool bUpdatedText=false;

	if( !m_nTimerId || m_nTimerId!=nIdEvent )
		CProgressCtrl::OnTimer( nIdEvent );
	else
	{
		nTimer++;

		//	Create a custom estimate of bandwidth & time remaining
		if( g_hwndStatic && m_pCore && m_nType==eSINGLE )
		{
			LONG bytesComp		=	m_pCore->GetDownload()->GetBytesCompleted( *m_nDownloadId );
			LONG bytesTotal		=	m_pCore->GetDownload()->GetFileLength( *m_nDownloadId );
			LONG secs			=	m_pCore->GetDownload()->GetXferSecs( *m_nDownloadId );

			double bps			=	(double)(bytesComp<<3)/(double)(secs+1);

			LONG bytesLeft		=	bytesTotal-bytesComp;

			double secsLeft		=	(double)(bytesLeft<<3)/bps;

			LONG nActiveSources	=	m_pCore->GetDownload()->GetActiveSourceCount( *m_nDownloadId );

			if( !bUpdatedText )
			{
				sprintf( m_szBuf, "ETA: %d secs @ %.1f kbps, %d active source%s %ld/%ld KB",
					(int)secsLeft,
					 bps/1024,
					 nActiveSources,
					(nActiveSources==1?",":"s,"),
					 bytesComp>>10,
					 bytesTotal>>10
					);
				bUpdatedText=true;
			}

			CWnd::FromHandle( g_hwndStatic )->SetWindowText( m_szBuf );
		}

		//	Update the STATIC control's text with the new GnucDNA download status
		if( g_hwndStatic )
		{
			if( m_pCore )
			{
				if( m_nType==eSINGLE || m_nType==eBITPRINT )
				{
					char szBuf[1024];		szBuf[0]=0;
					LONG nCompletedBytes=	m_pCore->GetDownload()->GetBytesCompleted( *m_nDownloadId );
					LONG nTotalBytes	=	m_pCore->GetDownload()->GetFileLength( *m_nDownloadId );
					LONG nActiveSources	=	m_pCore->GetDownload()->GetActiveSourceCount( *m_nDownloadId );
					LONG nTotalSources	=	m_pCore->GetDownload()->GetSourceCount( *m_nDownloadId );
					LONG nEtaSecs		=	nTotalBytes>0
										?	0
										:	m_pCore->GetDownload()->GetSecETA( *m_nDownloadId );
					LONG nKbps			=	m_pCore->GetDownload()->GetBytesPerSec( *m_nDownloadId )>>7;

					//std::vector< int > sources=m_pCore->GetDownload()->GetSourceIDs( *m_nDownloadId );

					if( nTotalBytes )
					{
						SetRange32( 0, nTotalBytes>>10 );
						SetPos( nCompletedBytes>>10 );
					}

					if( !bUpdatedText )
					{
						sprintf( 
							m_szBuf, 
							"DL @ %ld Kbps: %ld/%ld sources, ETA: %d secs, %ld/%ld KB done", 
							nKbps,
							nActiveSources,
							nTotalSources,
							nEtaSecs,
							nCompletedBytes>>10,
							nTotalBytes>>10 );

						bUpdatedText=true;
					}

					if( nTotalBytes && nCompletedBytes==nTotalBytes )
					{
						g_completed=TRUE;
						std::vector< int > downloadIds=m_pCore->GetDownload()->GetDownloadIDs();
						for( int i=0; i<downloadIds.size(); i++ )
						{
							m_pCore->GetDownload()->Stop( downloadIds[i] );
							m_pCore->GetDownload()->Remove( downloadIds[i] );
						}
					}
					else if( m_pCore->GetDownload()->HaveAllSourcesFailed( *m_nDownloadId ) )
					{
#ifndef NDEBUG
#ifdef BREAK_INTO_DEBUGGER
  _asm int 03h;
  TRACE3( "All Sources have failed for Download %d, Completed %dB of %dB total.\n",
	  *m_nDownloadId,
	   m_pCore->GetDownload()->GetBytesCompleted(*m_nDownloadId),
	   m_pCore->GetDownload()->GetFileLength(*m_nDownloadId) );

  m_pCore->GetDownload()->HaveAllSourcesFailed( *m_nDownloadId );
#endif
#endif
						g_cancelled=TRUE;		//	This happens when all sources have been exhausted.
					}
				}
				else
				{
					assert( m_nType==eMULTIPLE );

					CDnaDownload *download=m_pCore->GetDownload();

					LONG nFilesTotal	=	g_nDownloadCount;	//	Constant
					LONG nFilesCompleted=	0;					//	SUMIF( fileBytes!=0 && fileBytes==completedBytes )
					LONG nCompletedBytes=	0;					//	SUM
					LONG nTotalBytes	=	0;					//	SUM
					LONG nActiveSources	=	0;					//	SUM
					LONG nTotalSources	=	0;					//	SUM
					LONG nEtaSecs		=	0;					//	This is MAXed not SUMmed
					LONG nKbps			=	0;					//	SUM

					bool bAllFilesHaveFailed=true;
					for( int i=0; i<nFilesTotal; i++ )
					{
						bool bFileComplete=false;

						if( download->IsCompleted(m_nDownloadId[i]) )
							bFileComplete=true;
						
						nCompletedBytes	+=	download->GetBytesCompleted( m_nDownloadId[i] );
						nTotalBytes		+=	download->GetFileLength( m_nDownloadId[i] );

						if( bFileComplete )
							nFilesCompleted++;
						else
						{
							nActiveSources	+=	download->GetActiveSourceCount( m_nDownloadId[i] );
							nTotalSources	+=	download->GetSourceCount( m_nDownloadId[i] );
							if( nEtaSecs<download->GetSecETA( m_nDownloadId[i] ))
								nEtaSecs=download->GetSecETA( m_nDownloadId[i] );
							nKbps			+=	download->GetBytesPerSec( m_nDownloadId[i] )>>10;
						}

						if( !download->HaveAllSourcesFailed(m_nDownloadId[i]) )
							bAllFilesHaveFailed=false;
					}

					if( nTotalBytes )
					{
						SetRange32( 0, nTotalBytes>>10 );
						SetPos( nCompletedBytes>>10 );
					}

					if( !bUpdatedText )
					{
						sprintf( 
							m_szBuf, 
							"DL @ %ld Kbps: %ld/%ld sources for %d/%d files, ETA: %d secs, %ld/%ld KB done", 
							nKbps,
							nActiveSources,
							nTotalSources,
							nFilesTotal-nFilesCompleted,
							nFilesTotal,
							nEtaSecs,
							nCompletedBytes>>10,
							nTotalBytes>>10 );

						bUpdatedText=true;
					}

					if( nFilesCompleted==g_nDownloadCount )
					{
						if( nFilesCompleted==g_nDownloadCount )
						{
							g_completed=TRUE;
							std::vector< int > downloadIds=m_pCore->GetDownload()->GetDownloadIDs();
							for( int i=0; i<downloadIds.size(); i++ )
							{
								m_pCore->GetDownload()->Stop( downloadIds[i] );
								m_pCore->GetDownload()->Remove( downloadIds[i] );
							}
						}
					}
					else if( bAllFilesHaveFailed )
					{
#ifndef NDEBUG
#ifdef BREAK_INTO_DEBUGGER
  _asm int 03h;
#endif
#endif
						g_cancelled=TRUE;		//	This happens when all sources have been exhausted.
					}
				}

			}
			else if( !bUpdatedText )
			{
				sprintf( m_szBuf, "Got to the TIMER code (%d time%s)", nTimer,( (nTimer!=1)?("s"):("") ) );
				bUpdatedText=true;
			}

			if( bUpdatedText )
				CWnd::FromHandle( g_hwndStatic )->SetWindowText( m_szBuf );
		}
	}
}

//-----------------------------------------------------------------------------
#pragma message( __FILE__ ":### DAVE Finish using _atoi() on the g_szDownloadSize[] parameter" )

void CProgressWnd::PreSubclassWindow( )
{
	if( !m_nTimerId )
		m_nTimerId=SetTimer( 0x1000, 1000, NULL );

	m_pCore=new CDnaCore;

//	Aesthetics & stuff  -DN
{
m_pCore->SetClientName( "LwSwarmedInstaller" );
m_pCore->SetClientCode( "LWSI" );

#ifdef BANDWIDTH_LIMIT
m_pCore->GetPrefs()->SetBandwidthDown( BANDWIDTH_LIMIT );
#endif
}
	m_pCore->GetPrefs()->SetDownloadPath( g_szDownloadPath[0] );
	m_pCore->GetPrefs()->SetPartialsDir( g_szDownloadIncompletePath[0] );
	
	//	Remove older downloads in the INCOMPLETE folder
	std::vector< int > downloadIds=m_pCore->GetDownload()->GetDownloadIDs();
	for( int i=0; i<downloadIds.size(); i++ )
	{
        m_pCore->GetDownload()->Stop( downloadIds[i] );
		m_pCore->GetDownload()->Remove( downloadIds[i] );
	}

	//	Begin download(s)
	if( m_nType==eSINGLE )
	{
		*m_nDownloadId=m_pCore->GetDownload()->DownloadTrustedFile(
			g_szDownloadName[0],
			0,	//	TODO: FIX THIS
			//( (strlen(g_szDownloadSha1Base32[0]))?(HASH_SHA1):(HASH_UNKNOWN) ),
			HASH_UNKNOWN,
			g_szDownloadSha1Base32[0] );

		m_pCore->GetDownload()->AddTrustedSource(
			*m_nDownloadId,
			 NETWORK_WEB,
			 g_szSourceUrl[0] );
	}
	else if( m_nType==eBITPRINT )
	{
		*m_nDownloadId=m_pCore->GetDownload()->DownloadTrustedFile(
			g_szDownloadName[0],
			0,	//	TODO: FIX THIS
			( (strlen(g_szDownloadSha1Base32[0]))?(HASH_BITPRINT):(HASH_UNKNOWN) ),
			g_szDownloadSha1Base32[0] );

		m_pCore->GetDownload()->AddTrustedSource(
			*m_nDownloadId,
			 NETWORK_WEB,
			 g_szSourceUrl[0] );
	}
	else
	{
		assert( m_nType==eMULTIPLE );

		for( int i=0; i<g_nDownloadCount; i++ )
		{
			m_nDownloadId[i]=m_pCore->GetDownload()->DownloadTrustedFile(
				g_szDownloadName[i],
				0,	//	TODO: FIX THIS
				( (strlen(g_szDownloadSha1Base32[0]))?(HASH_BITPRINT):(HASH_UNKNOWN) ),
				g_szDownloadSha1Base32[i] );

			m_pCore->GetDownload()->AddTrustedSource(
				m_nDownloadId[i],
				NETWORK_WEB,
				g_szSourceUrl[i] );
		}
	}

	CProgressCtrl::PreSubclassWindow();
}

//*****************************************************************************

extern "C" __declspec(dllexport) void downloadFromSha1WithSource(
	HWND   parent,
    int    string_size,
    char   *variables,
    stack_t **stacktop )
{
	EXDLL_INIT();

	static char szDownloadName[1024];
	static char szDownloadPath[MAX_PATH];
	static char szDownloadIncompletePath[MAX_PATH];
	static char szDownloadSha1Base32[64];
	static char szDownloadSize[1024];
	static char szSourceUrl[2048];

	g_nDownloadCount			=	 1;
	g_szDownloadName[0]			=	&szDownloadName[0];
	g_szDownloadPath[0]			=	&szDownloadPath[0];
	g_szDownloadIncompletePath[0]=	&szDownloadIncompletePath[0];
	g_szDownloadSha1Base32[0]	=	&szDownloadSha1Base32[0];
	g_szDownloadSize[0]			=	&szDownloadSize[0];
	g_szSourceUrl[0]			=	&szSourceUrl[0];

	popstring( szDownloadName );
	popstring( szDownloadPath );
	popstring( szDownloadIncompletePath );
	popstring( szDownloadSha1Base32 );
	popstring( szDownloadSize );
	popstring( szSourceUrl );

	g_parent=parent;

    if (parent)
    {
		uMsgCreate = RegisterWindowMessage("nsisdl create");

		lpWndProcOld = (void *)SetWindowLong(parent,GWL_WNDPROC,(long)ParentWndProc);

		SendMessage(parent, uMsgCreate, MAKEWPARAM(TRUE,eSINGLE), (LPARAM) parent);

		// set initial text
		SetWindowText(g_hwndStatic,g_pszBuf);
    }

	//	Waiting loop
	{
		char szBuf[256];
		sprintf( szBuf, "%s", g_pszBuf );

		while (1) 
		{
			const char * error=0;
			const char * completed=0;
			if( g_cancelled )
				error = "cancel";
			if( g_completed )
				completed = "completed";

			if (error || completed)
			{
				if (parent)
				{
					SendMessage(parent, uMsgCreate, MAKEWPARAM(FALSE,eMULTIPLE), (LPARAM) parent);
					SetWindowLong(parent, GWL_WNDPROC, (long)lpWndProcOld);
				}

				if( error )
				{
					char returnVal[32];
					sprintf(returnVal,"failure");
					setuservariable( INST_R0, returnVal );
				}
				else if( completed )
				{
					char returnVal[32];
					sprintf(returnVal,"success");
					setuservariable( INST_R0, returnVal );
				}
				break;
			}
			Sleep(1000);
		}
	}
	SendMessage(parent, uMsgCreate, MAKEWPARAM(FALSE,eSINGLE), (LPARAM) parent);
}

//-----------------------------------------------------------------------------

extern "C" __declspec(dllexport) void downloadFromBitprintWithSource(
	HWND   parent,
    int    string_size,
    char   *variables,
    stack_t **stacktop )
{
	EXDLL_INIT();

	static char szDownloadName[1024];
	static char szDownloadPath[MAX_PATH];
	static char szDownloadIncompletePath[MAX_PATH];
	static char szDownloadSha1Base32[64];
	static char szDownloadSize[1024];
	static char szSourceUrl[2048];

	g_nDownloadCount			=	 1;
	g_szDownloadName[0]			=	&szDownloadName[0];
	g_szDownloadPath[0]			=	&szDownloadPath[0];
	g_szDownloadIncompletePath[0]=	&szDownloadIncompletePath[0];
	g_szDownloadSha1Base32[0]	=	&szDownloadSha1Base32[0];
	g_szDownloadSize[0]			=	&szDownloadSize[0];
	g_szSourceUrl[0]			=	&szSourceUrl[0];

	popstring( szDownloadName );
	popstring( szDownloadPath );
	popstring( szDownloadIncompletePath );
	popstring( szDownloadSha1Base32 );
	popstring( szDownloadSize );
	popstring( szSourceUrl );

	g_parent=parent;

    if (parent)
    {
		uMsgCreate = RegisterWindowMessage("nsisdl create");

		lpWndProcOld = (void *)SetWindowLong(parent,GWL_WNDPROC,(long)ParentWndProc);

		SendMessage(parent, uMsgCreate, MAKEWPARAM(TRUE,eBITPRINT), (LPARAM) parent);

		// set initial text
		SetWindowText(g_hwndStatic,g_pszBuf);
    }

	//	Waiting loop
	{
		char szBuf[256];
		sprintf( szBuf, "%s", g_pszBuf );

		while (1) 
		{
			const char * error=0;
			const char * completed=0;
			if( g_cancelled )
				error = "cancel";
			if( g_completed )
				completed = "completed";

			if (error || completed)
			{
				if (parent)
				{
					SendMessage(parent, uMsgCreate, MAKEWPARAM(FALSE,eMULTIPLE), (LPARAM) parent);
					SetWindowLong(parent, GWL_WNDPROC, (long)lpWndProcOld);
				}

				if( error )
				{
					char returnVal[32];
					sprintf(returnVal,"failure");
					setuservariable( INST_R0, returnVal );
				}
				else if( completed )
				{
					char returnVal[32];
					sprintf(returnVal,"success");
					setuservariable( INST_R0, returnVal );
				}
				break;
			}
			Sleep(1000);
		}
	}
	SendMessage(parent, uMsgCreate, MAKEWPARAM(FALSE,eBITPRINT), (LPARAM) parent);
}

//*****************************************************************************

extern "C" __declspec(dllexport) void addToDownloadManifest(
	HWND   parent,
    int    string_size,
    char   *variables,
    stack_t **stacktop )
{
	EXDLL_INIT();

	size_t i=g_nDownloadCount;
	g_nDownloadCount++;
	g_szDownloadName[i]			=	new char[1024];
	g_szDownloadPath[i]			=	new char[MAX_PATH];
	g_szDownloadIncompletePath[i]=	new char[MAX_PATH];
	g_szDownloadSha1Base32[i]	=	new char[64];
	g_szDownloadSize[i]			=	new char[1024];
	g_szSourceUrl[i]			=	new char[2048];

	popstring( g_szDownloadName[i] );
	popstring( g_szDownloadPath[i] );
	popstring( g_szDownloadIncompletePath[i] );
	popstring( g_szDownloadSha1Base32[i] );
	popstring( g_szDownloadSize[i] );
	popstring( g_szSourceUrl[i] );

	g_parent=parent;
}

//-----------------------------------------------------------------------------

extern "C" __declspec(dllexport) void downloadFromManifest(
	HWND   parent,
    int    string_size,
    char   *variables,
    stack_t **stacktop )
{
	EXDLL_INIT();

	g_parent=parent;

    if (parent)
    {
		uMsgCreate = RegisterWindowMessage("nsisdl create");

		lpWndProcOld = (void *)SetWindowLong(parent,GWL_WNDPROC,(long)ParentWndProc);

		SendMessage(parent, uMsgCreate, MAKEWPARAM(TRUE,eMULTIPLE), (LPARAM) parent);

		// set initial text
		SetWindowText(g_hwndStatic,g_pszBuf);
    }

	//	Waiting loop
	{
		char szBuf[256];
		sprintf( szBuf, "%s", g_pszBuf );

		while (1) 
		{
			const char * error=0;
			const char * completed=0;
			if( g_cancelled )
				error = "cancel";
			if( g_completed )
				completed = "completed";

			if (error || completed)
			{
				if (parent)
				{
					SendMessage(parent, uMsgCreate, MAKEWPARAM(FALSE,eMULTIPLE), (LPARAM) parent);
					SetWindowLong(parent, GWL_WNDPROC, (long)lpWndProcOld);
				}

				if( error )
				{
					char returnVal[32];
					sprintf(returnVal,"failure");
					setuservariable( INST_R0, returnVal );
				}
				else if( completed )
				{
					char returnVal[32];
					sprintf(returnVal,"success");
					setuservariable( INST_R0, returnVal );
				}
				break;
			}
			Sleep(1000);
		}
	}
	SendMessage(parent, uMsgCreate, MAKEWPARAM(FALSE,eMULTIPLE), (LPARAM) parent);
}

#pragma warning(pop)
//*****************************************************************************
























