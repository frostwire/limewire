// MainFrm.h : interface of the CMainFrame class
//


#pragma once

#include "ChildView.h"

#ifndef __INTERFACES__
#include "interfaces.h"
#endif

class CMainFrame : public CFrameWnd, public StatusDisplayer, public AppKiller
{
	
public:
	CMainFrame();
protected: 
	DECLARE_DYNAMIC(CMainFrame)

// Attributes
public:

// Operations
public:

// Overrides
public:
	virtual BOOL PreCreateWindow(CREATESTRUCT& cs);
	virtual BOOL OnCmdMsg(UINT nID, int nCode, void* pExtra, AFX_CMDHANDLERINFO* pHandlerInfo);

// Implementation
public:
	virtual ~CMainFrame();
#ifdef _DEBUG
	virtual void AssertValid() const;
	virtual void Dump(CDumpContext& dc) const;
#endif

	//	StatusDisplayer virtuals
public:
	virtual void UpdateStatusBar( const char * szNewText );

	//	AppKiller virtuals
public:
	virtual void killApp( void );

private:
	char m_szBuf[1024];

protected:  // control bar embedded members
	CStatusBar  m_wndStatusBar;
	CToolBar    m_wndToolBar;
	CChildView    m_wndView;

// Generated message map functions
protected:
	afx_msg int OnCreate(LPCREATESTRUCT lpCreateStruct);
	afx_msg void OnSetFocus(CWnd *pOldWnd);
	afx_msg void OnUpdateStatusText(CCmdUI *pCmdUI);
	DECLARE_MESSAGE_MAP()
};


