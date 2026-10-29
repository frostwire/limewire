#include "StdAfx.h"

#ifndef __SUCCESSSTATS__
#include "successstats.h"
#endif

//----------------------------------------

SuccessStats::SuccessStats(void)
{
	m_nTimeContactStart		=	-1;
	m_nTimeContactStop		=	-1;
	m_nTimeContactWStart	=	-1;
	m_nTimeContactWStop		=	-1;
	m_nTimeUpdateStart		=	-1;
	m_nTimeUpdateStop		=	-1;
	m_nTimeDownloadStart	=	-1;
	m_nTimeDownloadStop		=	-1;
	m_nSourcesTotal			=	-1;
	m_nSourcesActiveMax		=	-1;
}

SuccessStats::~SuccessStats(void)
{
}


//****************************************

void SuccessStats::ContactingUHCs( void )
{
	if( m_nTimeContactStart == -1 )
		m_nTimeContactStart = GetTickCount()/1000-1;

	m_nTimeContactStop = GetTickCount()/1000;
}

//----------------------------------------

void SuccessStats::ContactingWebcache( void )
{
	if( m_nTimeContactWStart == -1 )
		m_nTimeContactWStart = GetTickCount()/1000-1;

	m_nTimeContactWStop = GetTickCount()/1000;
}

//----------------------------------------

void SuccessStats::RetrievingUpdate( void )
{
	if( m_nTimeUpdateStart == -1 )
		m_nTimeUpdateStart = GetTickCount()/1000-1;

	m_nTimeUpdateStop = GetTickCount()/1000;
}

//----------------------------------------

void SuccessStats::DownloadingFile(
	int			nSourcesTotal,
	int			nSourcesActive )
{
	if( m_nTimeDownloadStart == -1 )
		m_nTimeDownloadStart = GetTickCount()/1000-1;

	m_nTimeDownloadStop = GetTickCount()/1000;

	if( m_nSourcesTotal<nSourcesTotal )
		m_nSourcesTotal=nSourcesTotal;

	if( m_nSourcesActiveMax<nSourcesActive )
		m_nSourcesActiveMax=nSourcesActive;
}

//****************************************

int	SuccessStats::getUHCTimeSecs( void )
{
	if( m_nTimeContactStart == -1 || 
		m_nTimeContactStop == -1 )
		return 0;
	else
	{
		int secs = m_nTimeContactStop - m_nTimeContactStart;

		if( secs < 0 || secs > 60*60*24 )
			secs=0;

		return secs;
	}
}

//----------------------------------------

int	SuccessStats::getWebcacheTimeSecs( void )
{
	if( m_nTimeContactWStart == -1 || 
		m_nTimeContactWStop == -1 )
		return 0;
	else
	{
		int secs = m_nTimeContactWStop - m_nTimeContactWStart;

		if( secs < 0 || secs > 60*60*24 )
			secs=0;

		return secs;
	}
}

//----------------------------------------

int	SuccessStats::getUpdateTimeSecs( void )
{
	if( m_nTimeUpdateStart == -1 || 
		m_nTimeUpdateStop == -1 )
		return 0;
	else
	{
		int secs = m_nTimeUpdateStop - m_nTimeUpdateStart;

		if( secs < 0 || secs > 60*60*24 )
			secs=0;

		return secs;
	}
}

//----------------------------------------

int SuccessStats::getDownloadTimeSecs( void )
{
	if( m_nTimeDownloadStart == -1 || 
		m_nTimeDownloadStop == -1 )
		return 0;
	else
	{
		int secs = m_nTimeDownloadStop - m_nTimeDownloadStart;

		if( secs < 0 || secs > 60*60*24 )
			secs=0;

		return secs;
	}
}

//----------------------------------------

int SuccessStats::getSourcesTotal( void )
{
	return m_nSourcesTotal;
}

//----------------------------------------

int	SuccessStats::getSourcesActiveMax( void )
{
	return m_nSourcesActiveMax;
}

//----------------------------------------
//----------------------------------------




