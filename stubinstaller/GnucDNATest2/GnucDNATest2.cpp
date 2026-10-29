// GnucDNATest2.cpp : Defines the class behaviors for the application.
//

#include "stdafx.h"
#include "GnucDNATest2.h"
#include "MainFrm.h"


#ifdef _DEBUG
#define new DEBUG_NEW
#endif

//-------------------------------------------------
#define USE_GNUC_DNA_CORE
//-------------------------------------------------


//-------------------------------------------------

// CGnucDNATest2App

BEGIN_MESSAGE_MAP(CGnucDNATest2App, CWinApp)
	ON_COMMAND(ID_APP_ABOUT, OnAppAbout)
END_MESSAGE_MAP()

// CGnucDNATest2App construction

CGnucDNATest2App::CGnucDNATest2App()
{
	// TODO: add construction code here,
	// Place all significant initialization in InitInstance
	srand( (unsigned int)time(NULL) );
}

int CGnucDNATest2App::ExitInstance( )
{
	return 0;
}

// The one and only CGnucDNATest2App object

CGnucDNATest2App theApp;

// CGnucDNATest2App initialization

BOOL CGnucDNATest2App::InitInstance()
{
	// InitCommonControls() is required on Windows XP if an application
	// manifest specifies use of ComCtl32.dll version 6 or later to enable
	// visual styles.  Otherwise, any window creation will fail.
	InitCommonControls();

	CWinApp::InitInstance();

	if (!AfxSocketInit())
	{
		AfxMessageBox(IDP_SOCKETS_INIT_FAILED);
		return FALSE;
	}

	// Initialize OLE libraries
	if (!AfxOleInit())
	{
		AfxMessageBox(IDP_OLE_INIT_FAILED);
		return FALSE;
	}
	AfxEnableControlContainer();
	// Standard initialization
	// If you are not using these features and wish to reduce the size
	// of your final executable, you should remove from the following
	// the specific initialization routines you do not need
	// Change the registry key under which our settings are stored
	// TODO: You should modify this string to be something appropriate
	// such as the name of your company or organization
	SetRegistryKey(_T("Local AppWizard-Generated Applications"));
	// To create the main window, this code creates a new frame window
	// object and then sets it as the application's main window object
	CMainFrame* pFrame = new CMainFrame;
	if (!pFrame)
		return FALSE;
	m_pMainWnd = pFrame;
	// create and load the frame with its resources
	pFrame->LoadFrame(IDR_MAINFRAME,
		WS_OVERLAPPEDWINDOW | FWS_ADDTOTITLE, NULL,
		NULL);
	// The one and only window has been initialized, so show and update it
	pFrame->ShowWindow(SW_SHOW);
	pFrame->UpdateWindow();
	// call DragAcceptFiles only if there's a suffix
	//  In an SDI app, this should occur after ProcessShellCommand


	return TRUE;
}


// CGnucDNATest2App message handlers



// CAboutDlg dialog used for App About

class CAboutDlg : public CDialog
{
public:
	CAboutDlg();

// Dialog Data
	enum { IDD = IDD_ABOUTBOX };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

// Implementation
protected:
	DECLARE_MESSAGE_MAP()
};

CAboutDlg::CAboutDlg() : CDialog(CAboutDlg::IDD)
{
}

void CAboutDlg::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
}

BEGIN_MESSAGE_MAP(CAboutDlg, CDialog)
END_MESSAGE_MAP()

// App command to run the dialog
void CGnucDNATest2App::OnAppAbout()
{
	CAboutDlg aboutDlg;
	aboutDlg.DoModal();
}


//--------------------------------------------------

#include <afxsock.h>
#include "GnuProtocol.h"
/*
void CGnucDNATest2App::DoUsefulStuff( void )
{
	CSocket			socket;
	BYTE			recvBuffer[4096];
	CString			srcAddr;
	UINT			srcPort;


	memset( recvBuffer, 0, sizeof(recvBuffer) );

	BOOL succeeded=socket.Create( 
		4000,
		SOCK_DGRAM );

	assert( SUCCEEDED(succeeded) );


	getChildView()->UpdateWindowText( "About to send pings...\n" );


//	Arrays better be the same size, dammit!  =)     -DN
assert( sizeof(szAddrUHCs)/sizeof(szAddrUHCs[0]) == sizeof(ports)/sizeof(ports[0]) );

	int arraySize=sizeof(szAddrUHCs)/sizeof(szAddrUHCs[0]);
	int index=rand()%arraySize;
	int i=0;

beforeInitialPings:

	//	Send UHC pings
	{
		//	Cycle through UHCs
		(++index)%=arraySize;
		
		packet_Ping_Static_GGEP 
						packet;

		//	Send some UDP UHC pings
		succeeded=socket.SendTo( 
			&packet, 
				sizeof(packet),
				ports[index],
				szAddrUHCs[index]
			);


		i++;	//	Increment whether or not we are looping

		//	Update screen with ping info
		{
			CBUF(szBuf,256);
			sprintf(szBuf, "Done sending ping #%d to host %s:%d, waiting for replies\n", 
				i, szAddrUHCs[index], ports[index] );
			getChildView()->AppendWindowText( szBuf );
		}

		Sleep(10);
	}

	assert( SUCCEEDED(succeeded) );

	Sleep(1000);

	//	Block while waiting for response UDP packet
	int bytes=socket.ReceiveFrom( 
		&recvBuffer, 
		 sizeof(recvBuffer),
		 srcAddr,
		 srcPort
		);

	if( bytes==SOCKET_ERROR )
	{
		DWORD error= GetLastError();

		//	If there was noone listening at the address & port we pinged, try again
		if( error==WSAECONNRESET )
		{
			getChildView()->UpdateWindowText( "Noone listening, trying again\n" );
			goto beforeInitialPings;
		}

		assert( false );
	}

	//	Parse IPs
	if( bytes>0 )
	{
		CBUF(szBuf,64);
		sprintf( szBuf, "Got a packet, length: %d\n", bytes );
		getChildView()->UpdateWindowText( szBuf );

		packet_Pong *pong=(packet_Pong*)recvBuffer;
        
        BYTE * payload=( (BYTE*) (pong+1) );

		packet_GGEPBlock block;
		uint32 length=bytes-23;
		GGEPReadResult parse_result;

		//	Skip magic GGEP byte
		BYTE temp = *payload++;
		assert( temp == 0xC3 );

		BYTE* payload2=payload;
		do 
		{
			memset( &block, 0, sizeof(block) );

			parse_result= CGnuProtocol::Decode_GGEPBlock(block, (byte*)payload2, length);
		} 
		while ( parse_result == BLOCK_GOOD && 
			0!=strcmp(block.Name, "IPP"));
		
		assert(parse_result == BLOCK_GOOD);
		assert(strcmp(block.Name,"IPP") == 0);

		if (parse_result != BLOCK_GOOD)
			return;

		//	At this point, we should have a valid IPP GGEP block.  Hopefully the data
		//		in it is valid!  =)
		int numIPs=block.PayloadSize/6;
		
		PackedIPPort *ips = new PackedIPPort[numIPs];
		PackedIPPort * curIP = (PackedIPPort*) block.Payload;

		memcpy( ips, curIP, sizeof(PackedIPPort)*numIPs );

		//	Display received IPs
		{
			int  offset=0, maxOffset=40;
			CBUF(szBuf,1024);
			offset+=sprintf( szBuf       , "Received a reply with the following %d IPs:\n", numIPs );
		for( int i=0; i<maxOffset && i<numIPs; i++ )
			offset+=sprintf( szBuf+offset, "%d) %d.%d.%d.%d:%d\n", 
				i+1,				ips[i].octet[0],	ips[i].octet[1],	
				ips[i].octet[2],	ips[i].octet[3],	ips[i].port() );

		if( numIPs>=maxOffset )
			offset+=sprintf( szBuf+offset, "... (more excluded) ...\n" );

			getChildView()->UpdateWindowText( szBuf );
			Sleep(1000);
		}

		//	Try to send a UpdateRequest to the first host in the list, and see what happens
		packet_UpdateRequest req;


		unsigned short portToUse=0;
		CString address;

#define UPDATE_FROM_LOCALHOST
//#define UPDATE_FROM_ZLATIN

#ifdef UPDATE_FROM_LOCALHOST
		portToUse=6346;
		address="127.0.0.1";
#elif UPDATE_FROM_ZLATIN
		portToUse=6346;
		address="10.254.0.44";
#else
		portToUse=ips[0].port();
		address=ips[0].stringIP();
#endif

		//	Send the UDP ping
		succeeded=socket.SendTo(
			&req, sizeof(req),
			 portToUse, address
			);

		assert( SUCCEEDED(succeeded) );
		{
			CBUF( szBuf, 1024 );
			sprintf( szBuf, "Sending LimeUpdateRequest UDP VendorMessage to %s:%d, awaiting reply...\n",
				address.GetBuffer(), portToUse );

			getChildView()->UpdateWindowText( szBuf );
		}
		Sleep(1000);


waitForPacket:
		//	Block while waiting for response UDP packet
		int bytes=socket.ReceiveFrom( 
			&recvBuffer, 
			 sizeof(recvBuffer),
			 srcAddr,
			 srcPort
			);

		if( bytes==SOCKET_ERROR )
		{
			DWORD error= GetLastError();

			if( error==WSAECONNRESET )
				goto waitForPacket;
				
			assert( false );
		}

		bool appearsValid=( bytes == sizeof(packet_Header) + ((packet_Header*)&recvBuffer)->Payload );
		assert( appearsValid );
		//	We have a winner!!!
		if( bytes>0 && appearsValid )
		{
			IPv4 addr;
			getChildView()->UpdateWindowText( "Got a response UDP packet...\n" );

#pragma message( __FILE__ ":### Finish parsing the source IP here" )
			addr.Host.S_addr=0;
			addr.Port=srcPort;

			//	Create one of these stupid GnucDNA packets
			Gnu_RecvdPacket rcvPacket( addr, (packet_Header*) recvBuffer, bytes);
			packet_VendMsg* vendMsg = (packet_VendMsg*) rcvPacket.Header;

			//	This was not the droid we were looking for...   ;-)
			if( vendMsg->Ident != packet_VendIdent("LIME", 27, 1) )
				goto waitForPacket;

			packet_UpdateResponse *resp=(packet_UpdateResponse *)vendMsg;

			BYTE * ptrPayload = resp->getPayload();
			int	payloadBytes = resp->getPayloadLength();

			assert( payloadBytes>0 && payloadBytes<2048 );

			//	Copy the payload into a buffer...
			{
				BYTE payloadCopy[2048];	//	Assume big enough...  lol
				memset( payloadCopy, 0, sizeof(payloadCopy) );

				memcpy( payloadCopy, ptrPayload, payloadBytes );
				CBUF(szBuf,256);
				sprintf( szBuf, "Received a packet from host: %s:%d\n\n", srcAddr, srcPort );
				PrintHexDumpToChild( szBuf, payloadCopy, payloadBytes );
			}
		}



//		__asm int 03h;
		delete[] ips;
	}
	else
		__asm int 03h;




//exit(0);

}



void CGnucDNATest2App::DoUsefulStuffUsingGnucDNA( void )
{
	pCore=new CDnaCore;

	CDnaNetwork *pNetwork=pCore->GetNetwork();
	pNetwork->StartUdpListener();

	CAsyncSocket *socket=pNetwork->GetUdpSocket();

	BOOL succeeded=TRUE;

	//	Try to send a UDP packet via the GnucDNA core
	packet_Ping_Static_GGEP 
					packet;

	int index=1;
	getChildView()->AppendWindowText( "About to send a UHC ping using the GnucDNA socket...\n" );
	//	Send some UDP UHC pings
	succeeded=socket->SendTo( 
		&packet, 
		 sizeof(packet),
		 ports[index],
		 szAddrUHCs[index]
		);


	//	Update screen with ping info
	{
		CBUF(szBuf,256);
		sprintf(szBuf, "Done sending ping #%d to host %s:%d, waiting for replies\n", 
			1, szAddrUHCs[index], ports[index] );
		getChildView()->AppendWindowText( szBuf );
	}

}
*/

















