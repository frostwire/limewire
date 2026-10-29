// StubInstallerDlg.h : header file
//

#pragma once

#ifndef __INTERFACES__
#include "interfaces.h"
#endif

#ifndef __GNUCDNAHANDLER__
#include "GnucDnaHandler.h"
#endif

#ifndef __XML_PARSER__
#include "xml_parser.h"
#endif

#include "afxcmn.h"

// CStubInstallerDlg dialog
class CStubInstallerDlg 
	: public CDialog
	, public DebugDisplayer
	, public StatusDisplayer
	, public ProgressDisplayer
	, public TimerControl
	, public UserInterface
	, public ClipboardHandler
	, public AppKiller
	, public RuntimeMethodLoader
{
// Construction
public:
	CStubInstallerDlg(CWnd* pParent = NULL);	// standard constructor

// Dialog Data
	enum { IDD = IDD_STUBINSTALLER_DIALOG };

	protected:
	virtual void DoDataExchange(CDataExchange* pDX);	// DDX/DDV support


// Implementation
protected:
	HICON m_hIcon;

	// Generated message map functions
	virtual BOOL OnInitDialog();
	afx_msg void OnPaint();
	afx_msg void OnTimer(  UINT_PTR nIDEvent );
	afx_msg LRESULT DoStart( WPARAM wp, LPARAM lp );
	afx_msg HCURSOR OnQueryDragIcon();
	DECLARE_MESSAGE_MAP()

private:
	bool					m_bDoTimer;
	unsigned int			m_delay;				
	char					m_szBufStatus[2048];
	size_t					m_nStrLenStatus;
	CGnucDNAHandler			m_handler;
	CProgressCtrl			m_progress;
	Parser					m_parser;

	char					m_szTitle[256];

	//
	//	Interfaces functions
	//
public:
	virtual void UpdateWindowText( const char * szNewText );
	virtual void AppendWindowText( const char * szNewText );
	virtual void UpdateStatusText( const char * szNewText );

	virtual void UpdateStatusBar( const char * szNewText );

	virtual void UpdateProgressBar( long posNow, long posMax );

	virtual void setTimerDelay( unsigned int millis );
	virtual void setTimerDelayDefault( void );
	virtual void killTimer( void );

	virtual void setTitleText( const char * szTitle );
	virtual bool queryUser( const char * szMsg );
	virtual void informUser( const char * szMsg );
	virtual void hideWindow( void );

	virtual void copyTextToClipboard( const char * szText );

	virtual void killApp( void );

	virtual pfnGetProfilesDirectory getGetProfilesDirectoryMethod( void );
};



class LimeWireDlg : public CDialog
{
public:
	LimeWireDlg( const char * szCaption );

public:
	BOOL OnInitDialog( void );

private:
	const char * m_szCaption;
};


