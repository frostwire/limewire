#pragma once

#ifndef __ICOMMANDPARSER__
#define __ICOMMANDPARSER__

class ICommandParser
{
public:
	ICommandParser(
		const char * szFileAndPath );
	~ICommandParser(void);

private:
	void Parse(							//	Allocates memory for strings
		const char * szFilename );

	//	API function & helper
public:
	const char * ReplaceTokens(
		const char * szICommand );

	//	Unit testing & helper
public:
	static bool UnitTest( void );

private:
	size_t			m_nPathLen;
	size_t			m_nFilenameLen;

	const char *	m_szPath;
	const char *	m_szFilename;
};


#endif






















