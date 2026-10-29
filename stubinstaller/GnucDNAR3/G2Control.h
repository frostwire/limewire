#ifndef __G2CONTROL__
#define __G2CONTROL__

#pragma once

#include "G2Packets.h"
#include "GnuShare.h"

class CGnuNetworks;
class CGnuPrefs;
class CGnuCache;
class CGnuShare;
class CGnuTransfers;
class CGnuCore;
class CG2Datagram;
class CG2Protocol;
class CG2Node;


#define G2_UNKNOWN 1
#define G2_CHILD   2
#define G2_HUB     3 

#define PACKETS_PER_SEC  8
#define ALT_HOST_MAX     5

#define ROUTE_EXPIRE	(3 * 60)
#define QA_QUERY_RETRY  (5 * 60) // Each node with 10 connects, 30 secs between search of cluster

#define CLEAN_ROUTES  (3 * 60)
#define CLEAN_KEYS    (1 * 60)
#define CLEAN_GLOBAL  (5 * 60)

#define MAX_ROUTES	 20000
#define MAX_KEYS	 50000
#define MAX_GLOBAL	 30000

#define HIGH_HUB_CAPACITY 90
#define LOW_HUB_CAPACITY  70

#define OPT_BANDWIDTH_DOWN 512 // 512 kilobits // use ping?
#define OPT_BANDWIDTH_UP   512 // 512 kilobits
#define OPT_LEAFMAX		   500   // 500 Children
#define OPT_UPTIME		   (6 * 60 * 60) // 6 hours

#define AVG_DNA   0
#define AVG_TOTAL 1


struct G2_RecvdPacket;

struct G2_Search
{
	G2_Q2 Query;

	int SearchedHubs;
	int SearchedChildren;
	int SearchedDupes;

	std::map<uint32, bool> TriedHubs;

	std::vector<IP> Routers;

	G2_Search()
	{
		SearchedHubs = 0;
		SearchedChildren = 0;
		SearchedDupes = 0;
	};
};

struct G2_QueryKey;
struct G2_Route;
struct G2HubInfo;

class CG2Control
{
public:
	CG2Control(CGnuNetworks* pNet);
	~CG2Control();
/*
	// Events
	virtual void Timer()=0;
	virtual void HourlyTimer()=0;

	virtual void ShareUpdate()=0;

	// Connections
	virtual void ManageNodes()=0;
	
	virtual void TryConnect(bool PrefDna)=0;
	virtual void DropNode(int G2Mode, bool NeedDna)=0;
	virtual bool ConnectFromCache(std::list<Node> &Cache, bool Perm)=0;

	virtual void CreateNode(Node HostInfo)=0;
	virtual void RemoveNode(CG2Node*)=0;

	std::map<uint32, bool> m_TriedConnects;

	time_t m_LastConnect;


	// Hub Balancing
	virtual void HubBalancing()=0;
	virtual int  ScoreNode(CG2Node* pNode)=0;
	virtual void UpdateGlobal(G2NodeInfo &HubNode)=0;

	int    m_HubBalanceCheck;
	uint32 m_NextUpgrade;
	uint32 m_ModeChangeTimeout;
	
	int m_MinsBelow10;
	int m_MinsBelow70;
	int m_NoConnections;

	// Counting
	virtual int CountHubConnects()=0;
	virtual int CountChildConnects()=0;

	
	// Other
	virtual void TrimMaps()=0;
	virtual void CleanDeadSocks()=0;

	virtual CG2Node* FindNode(CString Host, UINT Port, bool Connected=true)=0;
	virtual CG2Node* GetRandHub()=0;
	virtual bool	 GetAltHubs(CString &HostList, CG2Node* NodeExclude, bool DnaOnly)=0;

	virtual void G2NodeUpdate(CG2Node* updated)=0;

	virtual void SwitchG2ClientMode(int G2Mode, bool DownG1=false)=0;

	virtual void TestEncodeDecode()=0;

	virtual void ApplyPatchTable(CG2Node* pNode)=0;
	virtual void SetQHTBit(int &remotePos, double &Factor, CG2Node* pNode)=0;

	

	// Receiving
	virtual void ReceivePacket(G2_RecvdPacket &Packet)=0;

	virtual void Receive_PI(G2_RecvdPacket &PacketPI)=0;
	virtual void Receive_PO(G2_RecvdPacket &PacketPO)=0;
	virtual void Receive_LNI(G2_RecvdPacket &PacketLNI)=0;
	virtual void Receive_KHL(G2_RecvdPacket &PacketKHL)=0;
	virtual void Receive_QHT(G2_RecvdPacket &PacketQHT)=0;
	virtual void Receive_QKR(G2_RecvdPacket &PacketQKR)=0;
	virtual void Receive_QKA(G2_RecvdPacket &PacketQKA)=0;
	virtual void Receive_Q1(G2_RecvdPacket &PacketQ1)=0;
	virtual void Receive_Q2(G2_RecvdPacket &PacketQ2)=0;
	virtual void Receive_QA(G2_RecvdPacket &PacketQA)=0;
	virtual void Receive_QH2(G2_RecvdPacket &PacketQH2)=0;
	virtual void Receive_PUSH(G2_RecvdPacket &PacketPUSH)=0;
	virtual void Receive_MCR(G2_RecvdPacket &PacketMCR)=0;
	virtual void Receive_MCA(G2_RecvdPacket &PacketMCA)=0;
	virtual void Receive_PM(G2_RecvdPacket &PacketPM)=0;
	virtual void Receive_CLOSE(G2_RecvdPacket &PacketCLOSE)=0;
	virtual void Receive_CRAWLR(G2_RecvdPacket &PacketCRAWLR)=0;

	virtual void GetLocalNodeInfo(G2NodeInfo &LocalNode)=0;

	// Sending 
	virtual void RoutePacket(G2_RecvdPacket &Packet, GUID &TargetID)=0;

	virtual void Send_PI(IPv4 Target, G2_PI &Ping, CG2Node* pTCP=NULL)=0;
	virtual void Send_PO(G2_PI &Ping, CG2Node* pTCP=NULL)=0;
	virtual void Send_LNI(CG2Node* pTCP)=0;
	virtual void Send_KHL(CG2Node* pTCP)=0;
	virtual void Send_QHT(CG2Node* pTCP, bool Reset=false)=0;
	virtual void Send_QKR(IPv4 Target)=0;
	virtual void Send_QKA(IPv4 Target, G2_QKA &QueryKeyAnswer, CG2Node* pTCP=NULL)=0;
	virtual void Send_Q2(G2HubInfo* pHub, G2_Q2 &Query, CG2Node* pTCP=NULL)=0;
	virtual void Send_Q2(GnuQuery &FileQuery, std::list<int> &MatchingNodes)=0;
	virtual void Send_QA(IPv4 Target, G2_QA &QueryAck, CG2Node* pTCP=NULL)=0;
	virtual void Send_QH2(GnuQuery &FileQuery, std::list<UINT> &MatchingIndexes)=0;
	virtual void Send_QH2(GnuQuery &FileQuery, G2_QH2 &QueryHit)=0;
	virtual void Send_QH2(IPv4 Target, G2_QH2 &QueryHit)=0;
	virtual void Send_PUSH(FileSource* HostSource)=0;
	virtual void Send_MCR(CG2Node* pTCP)=0;
	virtual void Send_MCA(CG2Node* pTCP, bool Accept)=0;
	virtual void Send_PM(IPv4 Target, G2_PM &PrivateMessage, CG2Node* pTCP=NULL)=0;
	virtual void Send_CLOSE(CG2Node* pTCP, G2_CLOSE &Close)=0;
	virtual void Send_CRAWLA(IPv4 Target, G2_CRAWLA &CrawlAck)=0;

/*
	// Nodes
	CCriticalSection	m_G2NodeAccess;
	std::vector<CG2Node*>	 m_G2NodeList;
	std::map<int, CG2Node*>  m_G2NodeIDMap;
	std::map<uint32, CG2Node*> m_G2NodeAddrMap;

	uint32 m_CleanRoutesNext;
	std::map<uint32, G2_Route> m_RouteMap;

	virtual uint32 GenerateQueryKey(uint32 Address)=0;
	byte RandKeyBlock[64];
	std::map<uint32, G2_QueryKey> m_KeyInfo;
	uint32 m_CleanKeysNext;

	// Client Info
	int    m_ClientMode;
	CTime  m_ClientUptime;
	uint32 m_ClientIdent;

	bool m_ForcedHub;

	// Searching
	virtual void StartSearch(G2_Search *pSearch){};
	virtual void StepSearch(GUID SearchGuid)=0;
	virtual void EndSearch(GUID SearchGuid)=0;

	uint32 m_CleanGlobalNext;
	std::map<uint32, G2HubInfo> m_GlobalHubs;
	uint32 m_GlobalUnique; // Keeps the hub table in a different order for each client

	std::list<G2_Search*> m_G2Searches;

	int m_ActiveSearches;
	int m_ActiveSearchCount;

	int m_SearchReport;

	int m_SearchPacketsRecvd;

	// Bandwidth
	double m_NetSecBytesDown;
	double m_NetSecBytesUp;

	// Avg Query Packets
	CRangeAvg m_PacketsAvgQKR[2];
	CRangeAvg m_PacketsAvgQ2[2];
	
	uint16    m_PacketsQKR[2];
	uint16    m_PacketsQ2[2];

	// Links
	CGnuNetworks*  m_pNet;
	CGnuCache*     m_pCache;
	CGnuPrefs*     m_pPrefs;
	CGnuShare*	   m_pShare;
	CGnuTransfers* m_pTrans;
	CGnuCore*	   m_pCore;

	CG2Datagram*  m_pDispatch;
	CG2Protocol*  m_pProtocol; // Only access with main thread
	CG2Protocol*  m_pThreadProtocol;
*/
};

struct G2_RecvdPacket
{
	IPv4      Source;
	CG2Node*  pTCP;
	G2_Header Root;

	G2_RecvdPacket(IPv4 Address, G2_Header Packet, CG2Node* pNode=NULL)
	{
//		Source = Address;
//		Root   = Packet;
//		pTCP   = pNode;
	};
};


struct G2_QueryKey
{
	uint32 RetryAfter;
	bool   Banned;

	G2_QueryKey()
	{
//		RetryAfter = 0;
//		Banned = false;
	};
};

struct G2_Route
{
	IPv4   Address;
	GUID   RouteID;
	uint32 ExpireTime;

	G2_Route()
	{
//		ExpireTime = 0;
	}
};

struct G2HubInfo
{
	IPv4 Address;
	IPv4 Router;

	uint32 QueryKey;

	uint32 ExpireTime;
	uint32 NextTry;
	uint32 TryCount;

	G2HubInfo()
	{
		Init();
	};

	G2HubInfo(IPv4 address)
	{
		Init();

		Address = address;
	};

	void Init()
	{
//		ExpireTime = time(NULL) + GLOBAL_HUB_EXPIRE;
//		NextTry    = 0;
//		TryCount   = 0;

//		QueryKey   = 0;
	};
};


#endif