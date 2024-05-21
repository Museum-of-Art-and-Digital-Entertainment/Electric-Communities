
#include <stdio.h>
#include <time.h>

void main() {

FILE *fp = stdin;
char inputLine[8192];
char timestr[64];
time_t seconds;

while (fgets(inputLine, 8192, fp) != NULL) {

  time(&seconds);
  strcpy(timestr, ctime(&seconds));
  timestr[24] = '\0';
  printf("%s:%s", timestr, inputLine);
  fflush(stdout);

}

}