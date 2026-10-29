// ChildView.cpp : implementation of the CChildView class
//

#include "stdafx.h"
#include "GnucDNATest2.h"
#include "ChildView.h"
#include "GnuPackets.h"

#ifdef _DEBUG
#define new DEBUG_NEW
#endif

//----------------------------------------------------

#define TIMER_DELAY		((1000*3))
#define PROGRESS_ID		((0xBABE))
#define ERASE_WND		((TRUE))
//#define DO_UPDATE_WINDOW

//----------------------------------------------------

#pragma message( __FILE__ ":### FINISH using Zlatin's object for the XML parser" )
struct XmlParserTemp : public XmlParser
{
	const char * szBitprint;

	XmlParserTemp() { szBitprint=0; };

	virtual void parseUpdateResponse( packet_UpdateResponse *packet )
	{
		szBitprint=(const char *)(packet->getPayload()+0xd0);
	};

	virtual bool receivedEnoughUpdates( void ){return szBitprint!=0;};
	virtual const char * getBitprint( void ){return szBitprint;}
} parser;

//----------------------------------------------------

// CChildView
#pragma warning(disable:4355)	//	this used in base member init
CChildView::CChildView(StatusDisplayer & status, AppKiller & killer)
:m_handler(*this, parser, status, *this, *this, *this, killer)
{
	m_szBuf[0]=0;
	m_szBufStatus[0]=0;
	m_nStrLen=0;
	m_nStrLenStatus=0;

	m_delay=TIMER_DELAY;

	m_bDoTimer=true;
}
#pragma warning(default:4355)	//	this used in base member init

CChildView::~CChildView()
{
}

//----------------------------------------------------

BEGIN_MESSAGE_MAP(CChildView, CWnd)
	ON_WM_PAINT()
	ON_WM_TIMER()
	ON_WM_CREATE()
	ON_WM_CLOSE()
	ON_WM_SIZE()
END_MESSAGE_MAP()

//----------------------------------------------------


// CChildView message handlers

BOOL CChildView::PreCreateWindow(CREATESTRUCT& cs) 
{
	m_handler.init();

	if (!CWnd::PreCreateWindow(cs))
		return FALSE;

	cs.dwExStyle |= WS_EX_CLIENTEDGE;
	cs.style &= ~WS_BORDER;
	cs.lpszClass = AfxRegisterWndClass(CS_HREDRAW|CS_VREDRAW|CS_DBLCLKS, 
		::LoadCursor(NULL, IDC_ARROW), reinterpret_cast<HBRUSH>(COLOR_WINDOW+1), NULL);


	LOGFONT lf;
	memset(&lf,0,sizeof(lf));

	//lf.lfHeight=100;	//	font size 10
	lf.lfHeight=90;		//	font size 9

	sprintf(lf.lfFaceName,"Courier New");

	BOOL success=m_font.CreatePointFontIndirect(&lf);
	assert( success==TRUE );

	return TRUE;
}

void CChildView::OnPaint() 
{
	CPaintDC dc(this); // device context for painting

	CDC * pdc = &dc;

	CDC memDC;
	memDC.CreateCompatibleDC( &dc );

	CRect clrect;	//	Client area rect (not counting progress bar area)
	GetClientRect(clrect);

	clrect.DeflateRect(2,22,2,2);	//	Deflate +20 from top for progress bar

	CFont *temp=pdc->SelectObject(&m_font);

	CRect srect;	//	Status text rect (single line)
	srect=clrect;
	srect.bottom=srect.top; 	
	pdc->DrawText( "X", 1, srect, DT_LEFT | DT_CALCRECT );	//	Reserve one row of text minimum
	srect.right=clrect.right;
	pdc->DrawText( m_szBufStatus, m_nStrLenStatus, srect, DT_LEFT | DT_END_ELLIPSIS );
	//pdc->DrawText( "FOOBAR", 6, srect, DT_LEFT );

	CRect trect;	//	Text area rect (multi-line)
	trect=clrect;
	trect.top=srect.bottom+2+srect.Height();
	pdc->DrawText( m_szBuf, m_nStrLen, trect, DT_LEFT | DT_WORDBREAK );
	pdc->SelectObject(temp);
}

//----------------------------------------------------

void CChildView::UpdateWindowText( const char * szNewText )
{
	int size=strlen(szNewText);
	if( size<sizeof(m_szBuf)-1 )
	{
		strcpy( m_szBuf, szNewText );
		m_nStrLen=size;

		//	Redraw window with new text
		if( IsWindowEnabled() )
		{
			Invalidate(ERASE_WND);
#ifdef DO_UPDATE_WINDOW
			UpdateWindow();
#endif
		}
	}
}

void CChildView::AppendWindowText( const char * szNewText )
{
	int size=strlen(szNewText);
	if( size+m_nStrLen<sizeof(m_szBuf)-1 )
	{
		strcat( m_szBuf, szNewText );
		m_nStrLen+=size;

		//	Redraw window with new text
		Invalidate(ERASE_WND);
#ifdef DO_UPDATE_WINDOW
		UpdateWindow();
#endif
	}
}

void CChildView::UpdateStatusText( const char * szNewText )
{
	int size=strlen(szNewText);
	if( size<sizeof(m_szBufStatus)-1 )
	{
		strcpy( m_szBufStatus, szNewText );
		m_nStrLenStatus=size;

		//	Redraw window with new text
		if( IsWindowEnabled() )
		{
			Invalidate(ERASE_WND);
#ifdef DO_UPDATE_WINDOW
			UpdateWindow();
#endif
		}
	}
}

//----------------------------------------------------

void CChildView::UpdateProgressBar( long pos, long max )
{
	m_progress.SetRange32(0, max);
	m_progress.SetPos(pos);
}

//----------------------------------------------------

void CChildView::setTimerDelay( unsigned int millis )
{
	m_delay=millis;
	if( !m_bDoTimer )
		int retVal=SetTimer(
			0x1234,
			m_delay,
			NULL );
	m_bDoTimer=true;
}

void CChildView::setTimerDelayDefault( void )
{
	setTimerDelay( TIMER_DELAY );
}

void CChildView::killTimer( void )
{
	KillTimer(0x1234);
	m_bDoTimer=false;
}

//----------------------------------------------------

bool CChildView::queryUser( const char * szMsg )
{
	return (IDYES == AfxMessageBox( szMsg, MB_YESNO, 0 ));
}

void CChildView::informUser( const char * szMsg )
{
	AfxMessageBox( szMsg, MB_OK, 0 );
}

//----------------------------------------------------

void CChildView::OnTimer(  UINT_PTR nIDEvent )
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

int CChildView::OnCreate( LPCREATESTRUCT lpCreateStruct )
{
	int result=CWnd::OnCreate( lpCreateStruct );

	int retVal=SetTimer(
		0x1234,
		500,	//	Only wait 1/2 second for initial delay, use TIMER_DELAY thereafter
		NULL );

	//	Client area doesn't appear to be setup here
	CRect rect;
	GetClientRect( rect );
	rect.DeflateRect(2, 2, 2, 2);
	rect.bottom=rect.top+20;
	BOOL success=m_progress.Create(
		PBS_SMOOTH | WS_CHILD | WS_BORDER | WS_VISIBLE,
		rect,
		this,
		PROGRESS_ID );
	assert(success==TRUE);
	m_progress.SetRange( 0, 1 );
	m_progress.SetPos( 1 );

	return result;
}

void CChildView::OnClose( )
{
	KillTimer( 0x1234 );
}


void CChildView::OnSize( UINT nType, int cx, int cy )
{
	CRect rect;
	GetClientRect( rect );
	rect.DeflateRect(2, 2, 2, 2);
	rect.bottom=rect.top+20;
	m_progress.MoveWindow(rect);
	m_progress.SetBkColor(RGB(240, 240, 240));
}

//----------------------------------------------------
//----------------------------------------------------
//----------------------------------------------------
//----------------------------------------------------
//----------------------------------------------------













