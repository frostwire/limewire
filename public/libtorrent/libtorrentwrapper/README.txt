#building openssl on windows
perl Configure --openssldir=C:/www/openssl VC-WIN32
ms\do_masm
nmake -f ms\nt.mak
nmake -f ms\nt.mak test
nmake -f ms\nt.mak install

#building boost on windows
bjam --toolset=msvc link=static variant=release

#building libtorrent on windows
export BOOST_ROOT=xxx
bjam --toolset=msvc boost=source link=static variant=release character-set=unicode --without-python

#building libtorrent on linux
export BOOST_ROOT=xxx
bjam --toolset=gcc boost=source link=static variant=release character-set=unicode invariant-checks=off debug-symbols=off

#building boost on osx
export CXXFLAGS=" -arch x86_64 -arch i386"
export LDFLAGS=" -arch x86_64 -arch i386"
bjam --toolset=darwin link=static variant=release architecture=x86 address-model=32_64

#building libtorrent on osx
export BOOST_ROOT=xxx
export CXXFLAGS=" -arch x86_64 -arch i386"
export LDFLAGS=" -arch x86_64 -arch i386"
bjam --toolset=darwin boost=source link=static variant=release character-set=unicode invariant-checks=off debug-symbols=off architecture=x86 address-model=32_64

#building libtorrent on linux 64 bit
bjam --toolset=gcc link=static variant=release character-set=unicode invariant-checks=off debug-symbols=off cflags=-fPIC cxxflags=-fPIC -d+2








