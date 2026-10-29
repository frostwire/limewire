#pragma once

#ifndef __SUCCESSSTATS__
#define __SUCCESSSTATS__



class SuccessStats
{
public:
	SuccessStats(void);
	~SuccessStats(void);

public:
	void ContactingUHCs( void );
	void ContactingWebcache( void );
	void RetrievingUpdate( void );
	void DownloadingFile(
		int			nSourcesTotal,
		int			nSourcesActive );

public:
	int	getUHCTimeSecs( void );
	int	getWebcacheTimeSecs( void );
	int	getUpdateTimeSecs( void );
	int getDownloadTimeSecs( void );
	int getSourcesTotal( void );
	int	getSourcesActiveMax( void );

private:
	int			m_nTimeContactStart;
	int			m_nTimeContactStop;
	int			m_nTimeContactWStart;
	int			m_nTimeContactWStop;
	int			m_nTimeUpdateStart;
	int			m_nTimeUpdateStop;
	int			m_nTimeDownloadStart;
	int			m_nTimeDownloadStop;
	int			m_nSourcesTotal;
	int			m_nSourcesActiveMax;
};



#endif



