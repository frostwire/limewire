// GnucDNATest2.h : main header file for the GnucDNATest2 application
//
#pragma once

#ifndef __AFXWIN_H__
	#error include 'stdafx.h' before including this file for PCH
#endif

#include "resource.h"       // main symbols
#include "assert.h"


// CGnucDNATest2App:
// See GnucDNATest2.cpp for the implementation of this class
//
class CChildView;

class CGnucDNATest2App : public CWinApp
{
public:
	CGnucDNATest2App();

// Overrides
public:
	virtual BOOL InitInstance();
	virtual int ExitInstance( );

// Implementation
public:
	afx_msg void OnAppAbout();
	DECLARE_MESSAGE_MAP()

};

extern CGnucDNATest2App theApp;

