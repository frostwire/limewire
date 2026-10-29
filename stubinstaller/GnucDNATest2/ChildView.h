// ChildView.h : interface of the CChildView class
//


#pragma once
#include "GnucDNAHandler.h"
#ifndef __INTERFACES__
#include "interfaces.h"
#endif

// CChildView window

class CChildView 
	: public CWnd
	, public DebugDisplayer
	, public ProgressDisplayer
	, public TimerControl
	, public UserInterface
{
// Construction
public:
	CChildView(
		StatusDisplayer		&	status,
		AppKiller			&	killer );

// Attributes
public:

// Operations
public:

// Overrides
	protected:
	virtual BOOL PreCreateWindow(CREATESTRUCT& cs);

// Implementation
public:
	virtual ~CChildView();

	//	DebugDisplayer overrides
public:
	virtual void UpdateWindowText( const char * szNewText );
	virtual void AppendWindowText( const char * szNewText );
	virtual void UpdateStatusText( const char * szNewText );

	//	ProgressDisplayer overrides
public:
	virtual void UpdateProgressBar( long pos, long max );

	//	TimerControl overrides
public:
	virtual void setTimerDelay( unsigned int millis );
	virtual void setTimerDelayDefault( void );
	virtual void killTimer( void );

	//	UserInterface overrides
public:
	virtual bool queryUser( const char * szMsg );
	virtual void informUser( const char * szMsg );

private:
	char				m_szBuf[32768];
	char				m_szBufPrev[32768];
	char				m_szBufStatus[1024];
	int					m_nStrLen;
	int					m_nStrLenStatus;

	bool				m_bDoTimer;

	unsigned int		m_delay;				

	CFont				m_font;
	CGnucDNAHandler 	m_handler;

	CProgressCtrl		m_progress;

	// Generated message map functions
protected:
	afx_msg void OnPaint();
	afx_msg void OnTimer(  UINT_PTR nIDEvent );
	afx_msg int OnCreate( LPCREATESTRUCT lpCreateStruct );
	afx_msg void OnClose( );
	afx_msg void OnSize( UINT nType, int cx, int cy );

	DECLARE_MESSAGE_MAP()
};

