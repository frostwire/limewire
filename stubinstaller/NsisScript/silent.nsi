# This example shows how to handle silent installers.
# In short, you need IfSilent and the /SD switch for MessageBox to make your installer
# really silent when the /S switch is used.

Name "Silent"
OutFile "silent.exe"

# uncomment the following line to make the installer silent by default.
; SilentInstall silent


Section
	SetOutPath $TEMP
#	File jrestub.exe

##########################################################
#
#	NsisInstallerStub::downloadFromSha1WithSource 	\
#		"Alfie_Zappacosta_Start_Again.mp3" 	\
#		"C:\Documents and Settings\dave\Desktop\Downloads\InstallerStub" \
#		"C:\Documents and Settings\dave\Desktop\Downloads\InstallerStub\Incomplete" \
#		"L5FIK3UZS5QLCPBNJGTJIDW75B3UVS3Y" 	\
#		"0" 					\
#		"http://64.61.25.138:6346/uri-res/N2R?urn:sha1:L5FIK3UZS5QLCPBNJGTJIDW75B3UVS3Y"

##########################################################
#	NsisInstallerStub::downloadFromSha1WithSource	\
#		"ArmyOps190.exe"			\
#		"C:\Documents and Settings\dave\Desktop\Downloads\InstallerStub" \
#		"C:\Documents and Settings\dave\Desktop\Downloads\InstallerStub\Incomplete" \
#		"D4HEJPLRREUNXMGZMLSMM6DHVND2PLPD"	\
#		"0"					\
#		"http://64.61.25.138:6346/uri-res/N2R?urn:sha1:D4HEJPLRREUNXMGZMLSMM6DHVND2PLPD"

##########################################################
#
#	NsisInstallerStub::addToDownloadManifest 	/NOUNLOAD \
#		"Alfie_Zappacosta_Start_Again.mp3" 	\
#		"C:\Documents and Settings\dave\Desktop\Downloads\InstallerStub" \
#		"C:\Documents and Settings\dave\Desktop\Downloads\InstallerStub\Incomplete" \
#		"L5FIK3UZS5QLCPBNJGTJIDW75B3UVS3Y" 	\
#		"0" 					\
#		"http://64.61.25.138:6346/uri-res/N2R?urn:sha1:L5FIK3UZS5QLCPBNJGTJIDW75B3UVS3Y"

#	NsisInstallerStub::addToDownloadManifest	/NOUNLOAD \
#		"the_scene_xvid_episode_2.avi"			\
#		"C:\Documents and Settings\dave\Desktop\Downloads\InstallerStub" \
#		"C:\Documents and Settings\dave\Desktop\Downloads\InstallerStub\Incomplete" \
#		"JUKPCGZHIWLK3GFOP4S5OQT7TV5ZWZXF"	\
#		"0"					\
#		"http://64.61.25.138:6346/uri-res/N2R?urn:sha1:JUKPCGZHIWLK3GFOP4S5OQT7TV5ZWZXF"
		
#	NsisInstallerStub::addToDownloadManifest	/NOUNLOAD \
#		"the_scene_xvid_episode_3.avi"			\
#		"C:\Documents and Settings\dave\Desktop\Downloads\InstallerStub" \
#		"C:\Documents and Settings\dave\Desktop\Downloads\InstallerStub\Incomplete" \
#		"BD4WXAF5YQ3RKEBIH5NWROPG5LSHAIKS"	\
#		"0"					\
#		"http://64.61.25.138:6346/uri-res/N2R?urn:sha1:BD4WXAF5YQ3RKEBIH5NWROPG5LSHAIKS"
		
#	NsisInstallerStub::downloadFromManifest

##########################################################
#
#	NsisInstallerStub::downloadFromBitprintWithSource	\
#		"LimeWirePackedJars4.4.2.7z" 	\
#		"C:\Documents and Settings\dave\Desktop\Downloads\InstallerStub" \
#		"C:\Documents and Settings\dave\Desktop\Downloads\InstallerStub\Incomplete" \
#		"U4DBTY2YITMT27GMB64ZFPABXDNIJOIN.DZC3ZKUH55CTSAMFUOMEI7KE3EJM57TYDVO772I" 	\
#		"0" 					\
#		"http://129.89.132.134:6346/uri-res/N2R?urn:sha1:U4DBTY2YITMT27GMB64ZFPABXDNIJOIN"

##########################################################
#	Works really well!  =)  -DN
#
#	NsisInstallerStub::downloadFromSha1WithSource	\
#		"LimeWirePackedJars4.4.2.7z" 	\
#		"C:\Documents and Settings\dave\Desktop\Downloads\InstallerStub" \
#		"C:\Documents and Settings\dave\Desktop\Downloads\InstallerStub\Incomplete" \
#		"U4DBTY2YITMT27GMB64ZFPABXDNIJOIN" 	\
#		"0" 					\
#		"http://128.253.132.236:6346/uri-res/N2R?urn:sha1:U4DBTY2YITMT27GMB64ZFPABXDNIJOIN"

##########################################################
# 	No SHA1 for download or source.  X-Gnutella-Content-URN
#		or equivelent must be returned by the server for other sources to give us the correct
#		file, should the desired file have been updated (new version, etc)
#
#	NsisInstallerStub::downloadFromSha1WithSource	\
#		"LimeWirePackedJars4.4.2.7z" 	\
#		"C:\Documents and Settings\dave\Desktop\Downloads\InstallerStub" \
#		"C:\Documents and Settings\dave\Desktop\Downloads\InstallerStub\Incomplete" \
#		"" 	\
#		"0" 					\
#		"http://127.0.0.1:7346/get/0/LimeWirePackedJars4.4.2.7z"

##########################################################
# 	Should fail nicely...
#
#	NsisInstallerStub::downloadFromSha1WithSource	\
#		"LimeWirePackedJars4.4.2.7z" 	\
#		"C:\Documents and Settings\dave\Desktop\Downloads\InstallerStub" \
#		"C:\Documents and Settings\dave\Desktop\Downloads\InstallerStub\Incomplete" \
#		"" 	\
#		"0" 					\
#		"http://1.1.1.1:6346/uri-res/N2R?urn:sha1:U4DBTY2YITMT27GMB64ZFPABXDNIJOIN"

##########################################################
# 	Works fairly well, download without a SHA1 for the download
#		File: LimeWirePackedJars4.8.1.7z

	NsisInstallerStub::downloadFromSha1WithSource	\
		"LimeWirePackedJars4.8.1.7z" 	\
		"C:\Documents and Settings\dave\Desktop\Downloads\InstallerStub" \
		"C:\Documents and Settings\dave\Desktop\Downloads\InstallerStub\Incomplete" \
		"" 	\
		"0" 					\
		"http://24.100.70.56:6346/uri-res/N2R?urn:sha1:B3LGWVESD6H6OFMBD4UGDFJ25EAHI3ZJ"

##########################################################

  # there is no need to use IfSilent for this one because the /SD switch takes care of that

MessageBox MB_OK $R0

;This is how the decision to try another method will be made.
;StrCmp $R0 "Power" CheckLimewire

;Sleep 5000
  
SectionEnd
