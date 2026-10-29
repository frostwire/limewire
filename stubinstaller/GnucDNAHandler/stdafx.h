// stdafx.h : include file for standard system include files,
// or project specific include files that are used frequently, but
// are changed infrequently
//

#pragma once

#define WINVER 0x0410

#define WIN32_LEAN_AND_MEAN		// Exclude rarely-used stuff from Windows headers
#define _ATL_CSTRING_EXPLICIT_CONSTRUCTORS	// some CString constructors will be explicit

#ifndef VC_EXTRALEAN
#define VC_EXTRALEAN		// Exclude rarely-used stuff from Windows headers
#endif

#include <afx.h>
#include <afxwin.h>         // MFC core and standard components
#include <afxmt.h>
#include <vector>
#include <assert.h>

// TODO: reference additional headers your program requires here
#include "..\\GnucDNAR3\\stdafx.h"


#define CBUF(x,y) char x[y]; x[0]=0;
#define CBUF0(x,y) char x[y]; memset(x,0,y);

