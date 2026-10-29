#include <iostream>

extern "C" void echo(char* hey) {
  std::cout << "tasting:" << hey << std::endl;
}