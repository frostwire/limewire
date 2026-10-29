// NsisInstallerStub.h : main header file for the NsisInstallerStub DLL
//

#pragma once

#ifndef __AFXWIN_H__
	#error include 'stdafx.h' before including this file for PCH
#endif

#include "resource.h"		// main symbols
#include "exdll.h"


//*****************************************************************************
enum
{
	eSINGLE		=0,
	eMULTIPLE	=1,

	eBITPRINT
};
//*****************************************************************************
// CNsisInstallerStubApp
// See NsisInstallerStub.cpp for the implementation of this class
//
class CProgressWnd;
class CNsisInstallerStubApp : public CWinApp
{
public:
	CNsisInstallerStubApp();

// Overrides
public:
	virtual BOOL InitInstance();

	CProgressWnd * m_wndProgress;

	DECLARE_MESSAGE_MAP()
};

//*****************************************************************************
class CDnaCore;

class CProgressWnd : public CProgressCtrl
{
public:
	CProgressWnd( HWND ctrlId, CWnd *pParent, int type );
	~CProgressWnd();

public:
	void OnTimer( UINT_PTR nIdEvent );
	virtual void PreSubclassWindow( );

public:
	int m_nType;
	char m_szBuf[8192];			//	Output buffer
	char m_szBuf2[8192];		//	Output buffer 2
	LONG *m_nDownloadId;		//	Download ID (array) pointer
	LONG m_nDownloadIdHolder;
	CDnaCore *m_pCore;
	UINT_PTR m_nTimerId;

	DECLARE_MESSAGE_MAP()
};

//*****************************************************************************

extern "C" __declspec(dllexport) void downloadFromSha1WithSource(
	HWND   parent,
    int    string_size,
    char   *variables,
    stack_t **stacktop );

//*****************************************************************************

extern "C" __declspec(dllexport) void downloadFromBitprintWithSource(
	HWND   parent,
    int    string_size,
    char   *variables,
    stack_t **stacktop );

//*****************************************************************************

extern "C" __declspec(dllexport) void addToDownloadManifest(
	HWND   parent,
    int    string_size,
    char   *variables,
    stack_t **stacktop );

//-----------------------------------------------------------------------------

extern "C" __declspec(dllexport) void downloadFromManifest(
	HWND   parent,
    int    string_size,
    char   *variables,
    stack_t **stacktop );

//*****************************************************************************















