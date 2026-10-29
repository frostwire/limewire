#ifndef __PACKEDIPPORT__
#define __PACKEDIPPORT__

#pragma pack (push, 1)

//	This is the format for the IPP GGEP block to be found inside LW UDP UHC pongs
struct PackedIPPort
{
	union{
		unsigned long ip;
		BYTE octet[4];
	};

	unsigned short port;
	CString stringIP( void ){CString foo; foo.Format("%d.%d.%d.%d", 
		octet[0], octet[1], octet[2], octet[3] ); return foo;}
};

#pragma pack (pop)

#endif

