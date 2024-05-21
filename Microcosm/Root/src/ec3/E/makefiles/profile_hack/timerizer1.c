
#include <stdio.h>
#include <time.h>

void main() {

FILE *fp = stdin;
char inputLine[8192];
time_t seconds, prev_seconds;
double difference;

time(&prev_seconds);
while (fgets(inputLine, 8192, fp) != NULL) {
  time(&seconds);
  difference = difftime(seconds, prev_seconds);
  time(&prev_seconds);
  printf("%5.0f:%s", difference, inputLine);
  fflush(stdout);
}

}
