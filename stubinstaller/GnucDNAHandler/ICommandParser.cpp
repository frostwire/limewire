#include "StdAfx.h"

#ifndef __ICOMMANDPARSER__
#include "icommandparser.h"
#endif

//*********************************************************
#undef ASSERT
#ifndef NDEBUG
	#define ASSERT(x) if( !((x)) ) __asm int 03h;		//	Debug
#else
	#define ASSERT(x) if( !((x)) ) exit(__LINE__);		//	Release
#endif
//---------------------------------------------------------
#define PATH_TOKEN "$"
#define FILE_TOKEN "%"
//*********************************************************

ICommandParser::ICommandParser(
	const char			*	szFilename )
{
	m_szFilename=0;
	m_szPath=0;
	m_nFilenameLen=0;
	m_nPathLen=0;

	Parse( szFilename );
}

//---------------------------------------------------------

ICommandParser::~ICommandParser(void)
{
	if( m_szFilename )
	{
		delete [] m_szFilename;
		m_szFilename=0;
	}

	if( m_szPath )
	{
		delete [] m_szPath;
		m_szPath=0;
	}
}

//---------------------------------------------------------

void ICommandParser::Parse(
	const char			*	szFileAndPath )	//	Requires last char != '\\'
{
	ASSERT( m_szFilename==NULL && m_szPath==NULL );

	const char * ptr=0;

	ptr=strrchr( szFileAndPath, '\\' );

	ASSERT(ptr);

	size_t pathlen=ptr-szFileAndPath+1;	//	at least 1 char in path
	size_t filenamelen=strlen(ptr+1);

    char * szPath = new char[pathlen+1];
	char * szFilename = new char[filenamelen+1];

	strncpy( szPath, szFileAndPath, pathlen );
	strncpy( szFilename, ptr+1, filenamelen );

	szPath[pathlen]=0;
	szFilename[filenamelen]=0;

	m_szPath = (const char *)szPath;
	m_szFilename = (const char *)szFilename;

	m_nFilenameLen=filenamelen;
	m_nPathLen=pathlen;

#ifndef NDEBUG
	{
		char * szBuf = new char[ pathlen + filenamelen + 1 ];
		sprintf( szBuf, "%s%s", m_szPath, m_szFilename );

		ASSERT( strcmp(szBuf,szFileAndPath)==NULL );

		delete [] szBuf;
	}
#endif
}

//*********************************************************
#pragma message( __FILE__ ":### FINISH ReplaceTokens()" )

const char * ICommandParser::ReplaceTokens(
	const char			*	szICommand )
{ 
	ASSERT( m_szFilename!=NULL && m_szPath!=NULL );

	size_t finalLen=0;

	const char * ptr=szICommand;

	while( *ptr != 0 )
	{
		if( *ptr==PATH_TOKEN[0] )
			finalLen+=m_nPathLen;
		else if( *ptr==FILE_TOKEN[0] )
			finalLen+=m_nFilenameLen;
		else
			finalLen++;

		ptr++;
	}

	char * szPtr = new char[finalLen+1];
	const char * szReturn = (const char *)szPtr;

	ptr=szICommand;

	while( *ptr != 0 )
	{
		if( *ptr==PATH_TOKEN[0] )
			szPtr+=sprintf( szPtr, "%s", m_szPath );
		else if( *ptr==FILE_TOKEN[0] )
			szPtr+=sprintf( szPtr, "%s", m_szFilename );
		else
			*szPtr++=*ptr;

		ptr++;
	}

	*szPtr=0;

	return szReturn;
}

//*********************************************************
#pragma message( __FILE__ ":### FINISH UnitTest()" )

// static
bool ICommandParser::UnitTest( void )
{	
	const char * file1 = "c:\\path1\\path2\\file1.ext";
	const char * file2 = "\\\\machine\\share\\file2.ext";
	const char * file3 = "abc\\def";
	const char * file4 = "abc\\def\\ghi.jkl";

	ICommandParser p1( file1 );
	ICommandParser p2( file2 );
	ICommandParser p3( file3 );
	ICommandParser p4( file4 );

	const char * parsed1 = p1.ReplaceTokens( PATH_TOKEN FILE_TOKEN );
	const char * parsed2 = p2.ReplaceTokens( PATH_TOKEN FILE_TOKEN );
	const char * parsed3 = p3.ReplaceTokens( PATH_TOKEN FILE_TOKEN " /S foobar" );
	const char * parsed4 = p4.ReplaceTokens( "123 - " PATH_TOKEN " - " FILE_TOKEN " - Other stuff" );

	bool success=( strcmp(parsed1,file1)==NULL
				&& strcmp(parsed2,file2)==NULL
				&& strcmp(parsed3,"abc\\def /S foobar")==NULL
				&& strcmp(parsed4,"123 - abc\\def\\ - ghi.jkl - Other stuff")==NULL
				);

	ASSERT( success );

	delete [] parsed1;
	delete [] parsed2;
	delete [] parsed3;
	delete [] parsed4;

	return success;
}

//*********************************************************
//*********************************************************

//*********************************************************
//---------------------------------------------------------









