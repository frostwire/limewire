// StubInstallerDlg.cpp : implementation file
//

#include "stdafx.h"
#include "StubInstaller.h"
#include "StubInstallerDlg.h"
#include ".\stubinstallerdlg.h"

#ifdef _DEBUG
#define new DEBUG_NEW
#endif

//----------------------------------------------------
#define TIMER_DELAY		((1000))
//----------------------------------------------------
#include "GnuPackets.h"

//--------------------------------------------------------
// CStubInstallerDlg dialog


#pragma warning(disable:4355)	//	this used in base member init
CStubInstallerDlg::CStubInstallerDlg(CWnd* pParent /*=NULL*/)
	: CDialog(CStubInstallerDlg::IDD, pParent)
	, m_handler(*this,m_parser,*this,*this,*this,*this,*this,*this,*this)
{
	m_hIcon = AfxGetApp()->LoadIcon(IDR_MAINFRAME);
	m_szBufStatus[0]=0;
	m_szTitle[0]=0;
	m_nStrLenStatus=0;
	m_bDoTimer=true;
	m_delay=TIMER_DELAY;
}
#pragma warning(default:4355)	//	this used in base member init

//--------------------------------------------------------
#define WM_DO_START		((WM_USER+0x45))
//--------------------------------------------------------

BEGIN_MESSAGE_MAP(CStubInstallerDlg, CDialog)
	ON_WM_PAINT()
	ON_WM_QUERYDRAGICON()
	ON_WM_TIMER()
	ON_MESSAGE(WM_DO_START,DoStart)
	//}}AFX_MSG_MAP
END_MESSAGE_MAP()

//--------------------------------------------------------

void CStubInstallerDlg::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_PROGRESS, m_progress);
}

//--------------------------------------------------------
// CStubInstallerDlg message handlers

BOOL CStubInstallerDlg::OnInitDialog()
{
	CDialog::OnInitDialog();

	// Set the icon for this dialog.  The framework does this automatically
	//  when the application's main window is not a dialog
	SetIcon(m_hIcon, TRUE);			// Set big icon
	SetIcon(m_hIcon, FALSE);		// Set small icon

	SetDlgItemText( IDC_STATUS, "" );

	PostMessage( WM_DO_START );

	return TRUE;  // return TRUE  unless you set the focus to a control
}

LRESULT CStubInstallerDlg::DoStart( WPARAM wp, LPARAM lp )
{
	// TODO: Add extra initialization here
	m_handler.init();

	int retVal=SetTimer(
		0x1234,
		500,	//	Only wait 1/2 second for initial delay, use TIMER_DELAY thereafter
		NULL );

	return NULL;
}

// If you add a minimize button to your dialog, you will need the code below
//  to draw the icon.  For MFC applications using the document/view model,
//  this is automatically done for you by the framework.

void CStubInstallerDlg::OnPaint() 
{
	if (IsIconic())
	{
		CPaintDC dc(this); // device context for painting

		SendMessage(WM_ICONERASEBKGND, reinterpret_cast<WPARAM>(dc.GetSafeHdc()), 0);

		// Center icon in client rectangle
		int cxIcon = GetSystemMetrics(SM_CXICON);
		int cyIcon = GetSystemMetrics(SM_CYICON);
		CRect rect;
		GetClientRect(&rect);
		int x = (rect.Width() - cxIcon + 1) / 2;
		int y = (rect.Height() - cyIcon + 1) / 2;

		// Draw the icon
		dc.DrawIcon(x, y, m_hIcon);
	}
	else
	{
		CDialog::OnPaint();
	}
}

// The system calls this function to obtain the cursor to display while the user drags
//  the minimized window.
HCURSOR CStubInstallerDlg::OnQueryDragIcon()
{
	return static_cast<HCURSOR>(m_hIcon);
}

void CStubInstallerDlg::OnTimer(  UINT_PTR nIDEvent )
{
	//	No duplicate timer messages if we are already processing one
	KillTimer(0x1234);
	m_handler.doTimer();

	if( m_bDoTimer )
		int retVal=SetTimer(
			0x1234,
			m_delay,
			NULL );
}

//--------------------------------------------------------

void CStubInstallerDlg::UpdateWindowText( const char * szNewText )
{}
void CStubInstallerDlg::AppendWindowText( const char * szNewText )
{}
void CStubInstallerDlg::UpdateStatusText( const char * szNewText )
{
	int size=strlen(szNewText);
	if( size<sizeof(m_szBufStatus)-1 )
	{
		strcpy( m_szBufStatus, szNewText );
		m_nStrLenStatus=size;

		SetDlgItemText( IDC_STATUS, m_szBufStatus );
	}
}

//--------------------------------------------------------

void CStubInstallerDlg::UpdateStatusBar( const char * szNewText )
{}

//--------------------------------------------------------

void CStubInstallerDlg::UpdateProgressBar( long posNow, long posMax )
{
	m_progress.SetRange32( 0, posMax );
	m_progress.SetPos( posNow );
}

//--------------------------------------------------------

void CStubInstallerDlg::setTimerDelay( unsigned int millis )
{
	m_delay=millis;
	if( !m_bDoTimer )
		int retVal=SetTimer(
			0x1234,
			m_delay,
			NULL );
	m_bDoTimer=true;
}
void CStubInstallerDlg::setTimerDelayDefault( void )
{
	setTimerDelay( TIMER_DELAY );
}
void CStubInstallerDlg::killTimer( void )
{
	KillTimer(0x1234);
	m_bDoTimer=false;
}

//--------------------------------------------------------

void CStubInstallerDlg::setTitleText( const char * szText )
{
	if( strlen(szText) < sizeof(m_szTitle) )
	{
		strcpy( m_szTitle, szText );
		SetWindowText( m_szTitle );
	}
}
bool CStubInstallerDlg::queryUser( const char * szMsg )
{
	return (IDYES == AfxMessageBox( szMsg, MB_YESNO, 0 ));
}
void CStubInstallerDlg::informUser( const char * szMsg )
{
	LimeWireDlg dlg( szMsg );
	dlg.DoModal();
}
void CStubInstallerDlg::hideWindow( void )
{
	ShowWindow( SW_HIDE );
}

//--------------------------------------------------------

void CStubInstallerDlg::copyTextToClipboard( const char * szText )
{
	//	Duplicate the string in global memory
	HGLOBAL hMem = GlobalAlloc( NULL, strlen(szText)+1 );
	
	if( !hMem )
		return;

	strcpy( (char*) hMem, szText );

	//	Open the clipboard if possible
	if( !OpenClipboard() )
	{
		GlobalFree( hMem );
		return;
	}

	//	Empty clipboard
	EmptyClipboard();

	//	Copy string to clipboard
	SetClipboardData( CF_TEXT, hMem );

	//	Close clipboard
	CloseClipboard();
}

//--------------------------------------------------------

void CStubInstallerDlg::killApp( void )
{
	PostMessage( WM_CLOSE );
}

//--------------------------------------------------------

RuntimeMethodLoader::pfnGetProfilesDirectory 
  CStubInstallerDlg::getGetProfilesDirectoryMethod( void )
{
	//	GetProfilesDirectory() is in UserEnv.dll, and only exists on Win2K and above (basically)
	pfnGetProfilesDirectory pfn=NULL;

	if( GetVersion() < 0x80000000 )
	{
		HMODULE hmod=LoadLibrary( "UserEnv" );
		if( hmod )
			pfn=(pfnGetProfilesDirectory)GetProcAddress( hmod, "GetProfilesDirectoryA" );
	}

	return pfn;
}

//--------------------------------------------------------
//--------------------------------------------------------
//--------------------------------------------------------
//--------------------------------------------------------

LimeWireDlg::LimeWireDlg( const char * szCaption )
:CDialog( IDD_FIREWALL_WARNING )
{
	m_szCaption=szCaption;
}

//--------------------------------------------------------

BOOL LimeWireDlg::OnInitDialog( void )
{
	SetDlgItemText( IDC_CAPTION, m_szCaption );

	return CDialog::OnInitDialog();
}







