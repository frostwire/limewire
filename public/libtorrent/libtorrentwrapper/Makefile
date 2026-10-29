CXXFLAGS = -O2 -Wall

LIB_DIR = lib/linux
LIB_DIR64 = lib/linux/64
LIBS = -Wl,-Bstatic -l torrent-gcc43-0_14_6 -l ssl -l crypto -l boost_filesystem-gcc43-mt-1_39 -l boost_system-gcc43-mt-1_39 -l boost_thread-gcc43-mt-1_39 -Wl,-Bdynamic -l dl -l z -l rt -l pthread
LIBS64 = -Wl,-Bstatic -l torrent-gcc44-0_14_7 -l ssl -l crypto -l boost_filesystem -l boost_system -l boost_program_options -l boost_thread -Wl,-Bdynamic -l dl -l z -l rt -l pthread
LIBRARY = libtorrent-wrapper.so
LIBRARY64 = libtorrent-wrapper64.so
TARGET = libtorrent-wrapper.out
OPTIONS =  -DLINUX -DNDEBUG -DTORRENT_USE_OPENSSL -DTORRENT_DISABLE_INVARIANT_CHECKS -DUNICODE -D_UNICODE -DTORRENT_NO_DEPRECATE -DBOOST_ALL_NO_LIB -DTORRENT_DISABLE_GEO_IP
LINKOPTIONS = -static-libgcc -shared

OBJS = libtorrentwrapper.o
INCLUDES = -I include 


compile:
	${CXX} $(CXXFLAGS) -c src/libtorrentwrapper.cpp $(OPTIONS) $(INCLUDES)

compile64: 
	${CXX} $(CXXFLAGS) -fpic -m64 -c src/libtorrentwrapper.cpp $(OPTIONS) $(INCLUDES)
	
all: clean copylib

library: compile
	${CXX} $(CXXFLAGS) -o $(LIBRARY) $(LINKOPTIONS) $(OBJS) -L$(LIB_DIR) $(LIBS)

library64: compile64
	${CXX} $(CXXFLAGS) -o $(LIBRARY64) $(LINKOPTIONS) $(OBJS) -L$(LIB_DIR64) $(LIBS64)

copylib: library
	strip $(LIBRARY)
	cp $(LIBRARY) ../../../limewire/lib/native/linux/

copylib64: library64
	strip $(LIBRARY64)
	cp $(LIBRARY64) ../../../limewire/lib/native/linux/

unzip : 
	unzip include

clean:
	rm -f $(OBJS) $(TARGET) $(LIBRARY) a.out

