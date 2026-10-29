//#define LIME_DEBUG
#include "stdafx.h"

#define INCLUDE_DSA_VERIFY
#define USE_GNUCDNA_HASH
#define NO_MAIN




#ifdef INCLUDE_DSA_VERIFY


#include <iostream>
#include <conio.h>
//#include "gfpcrypt.h"
//#include "dsa.h"
#include "integer.h"
#ifndef USE_GNUCDNA_HASH
#include "sha.h"
#endif
#include "LimePK.h"

using namespace CryptoPP;

static byte *base32_decode(const byte *b32, int b32_length);
bool verify(Integer &r, Integer &s, Integer &hash);

/*
// Lime Wire's vesrion-signing DSA public key parameters
static const Integer *lime_p_ptr=NULL;  // Prime defining the group
static const Integer *lime_q_ptr=NULL;  // Prime factor of (p-1) defining subgroup
static const Integer *lime_g_ptr=NULL;  // Subgroup generator
static const Integer *lime_y_ptr=NULL;  // y = (g**x)%p where x is the secret key parameter
*/
#ifndef __GNUCDNAHANDLER__
#include "GnucDNAHandler.h"
#endif

bool verify(const byte *base32_sig, int base32_sig_length, const byte *message, int message_length) {
	// This Boolean simplifies memory management
	// by moving all of the function's entry points
	// after the call to free(sig);
	bool result = true;

	// Basic sanity checks
	if (base32_sig_length < 0 || message_length < 0)
		result = false;
	// Convert base32 signature into base256 signature
	byte *sig = base32_decode(base32_sig, base32_sig_length);
	int sig_length = (base32_sig_length * 5) / 8;
	if (sig_length < 8)
		result = false;
	// Break sig into r and s
	Integer r,s;
	// Check for proper ASN.1 DER encoding
	if (sig[0] != 0x30 || sig[1] + 2 != sig_length  || sig[2] != 0x02)
		result = false;
	int r_length = sig[3];
	int s_length = sig[5+r_length];
	// More ASN.1 DER sanity checks
	if (sig[4+r_length] != 0x02 || r_length+s_length+6 != sig_length)
		result = false;

	if (result) 
	{
		r.Decode(sig+4, r_length);
		s.Decode(sig+6+r_length, s_length);
	}
	free(sig);

#ifdef LIME_DEBUG
	std::cout << "sig_length=" << sig_length << "\n";
	std::cout << "r=" << r << "\n";
	std::cout << "s=" << s << "\n";
#endif

	if (! result)
		return false;

	Integer hash;
#ifdef USE_GNUCDNA_HASH
	byte digest[20];
	CGnucDNAHandler::SHA1((BYTE*)message, message_length,digest,sizeof(digest));
	hash.Decode((byte*)digest,sizeof(digest));
#else
	SHA sha1;
	SecByteBlock digest(sha1.DigestSize());

	sha1.Update(message, message_length);
	sha1.Final(digest);
	hash.Decode((byte *)digest, sha1.DigestSize());
#endif
	
	return verify(r,s,hash);
}


bool verify(Integer &r, Integer &s, Integer &hash) {

	const Integer lime_p(LIMEPK_P);
	const Integer lime_q(LIMEPK_Q);
	const Integer lime_g(LIMEPK_G);
	const Integer lime_y(LIMEPK_Y);

	
	// Discount trivial solutions
	// 0 < r <  lime_q 
	if (1 > r || r >= lime_q)
		return false;
	// 0 < s < lime_q
	if (1 > s || s >= lime_q)
		return false;

	Integer w = s.InverseMod( lime_q);

	// u1 = (hash * w) mod q
	Integer u1 = (hash * w ) % ( lime_q);
	// u2 = (r * w) mod q
	Integer u2 = (r * w) % ( lime_q);
	
	// v = (((g**u1)*(y**u2)) mod p) mod q
	Integer v = a_times_b_mod_c(a_exp_b_mod_c( lime_g, u1,  lime_p), a_exp_b_mod_c( lime_y, u2,  lime_p), lime_p);
    v %= ( lime_q);

#ifdef LIME_DEBUG
	std::cout << "w=" << w << "\n";
	std::cout << "u1=" << u1 << "\n";
	std::cout << "u2=" << u2 << "\n";
	std::cout << "v=" << v << "\n";
#endif

	return (v == r);
}

bool verify(byte *message, int message_length) {
	int base32_sig_length = 0;
	for(;base32_sig_length <= message_length; ++base32_sig_length) 
	{
		//MS VC++ seems to be localizing the pipe character
		if(message[base32_sig_length] == 124 && message[base32_sig_length+1] == 124)
			break;
	}
	return verify(message, base32_sig_length, message+base32_sig_length+2, message_length-(base32_sig_length+2));
}

byte *base32_decode(const byte *b32, int b32_length)
{
	int b256_length = (b32_length*5)/8;
	byte *b256 = (byte*) malloc((b32_length*5)/8);

	int storedData = 0;
	int storedBitCount = 0;
	int outPosition = 0;
	for(int inPosition=0; inPosition < b32_length; inPosition++) {
		int nextValue = b32[inPosition];

		if (nextValue < '2' || nextValue > 'Z') {
			continue;
		}
		if (nextValue > '7' && nextValue < 'A') {
			continue;
		}

		nextValue -= 'A';
		if(nextValue < 0) {
			nextValue += 'A' - '2' + 26;
		}

		storedData = (storedData << 5) + nextValue;
		storedBitCount += 5;

		if (storedBitCount >= 8) {
			b256[outPosition++] = (byte) (0xFF & (storedData >> (storedBitCount - 8)));
			storedBitCount -= 8;
			storedData &= (1 << storedBitCount) -1;
		}
	}
	return b256;
 }

#endif

#include "GnuPackets.h"
extern bool verifyLimeUpdateResponsePacket( packet_UpdateResponse * packet )
{
//	return 0;
#ifdef INCLUDE_DSA_VERIFY
	return verify( packet->getPayload(), packet->getPayloadLength() );
#else
	return false;
#endif
}

#ifndef NO_MAIN
int _tmain(int argc, _TCHAR* argv[])
{
	std::cout.flags(std::ios::hex);

	byte buf[4096];
	FILE *f = fopen("version.xml","rb");
	int i=0;
	for(; i < sizeof(buf); ++i)
	{
		int next = fgetc(f);
		if (next == EOF)
			break;
		buf[i] = (byte) next;
	}
	buf[i+1] = 0;
	fclose(f);

#ifdef LIME_DEBUG
	std::cout << "read :0x" << i << " bytes\n";
	std::cout << (char *) buf << "\n";
#endif

	if (verify(buf, i))
	{
		std::cout << "Verified\n";
	} 
	else
	{
		std::cout << "Failed\n";
	}
	while (! kbhit());

	return 0;
}
#endif
