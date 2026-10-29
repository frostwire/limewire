#include <dlfcn.h>
#include <iostream>

int main(int argc, char** argv) {

  void (*echo)(char*);
  
  void *handle = dlopen(argv[1], RTLD_LAZY);
  
  if (!handle) {
    std::cout << "eraw" << std::endl <<  dlerror() << std::endl;
    return 1;
  }
  
  *(void **)(&echo) = dlsym(handle, "echo");
  echo("hellaw");
  
  return 0;
}
