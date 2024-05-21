
#include <stdio.h>
#include <time.h>

void main() {

FILE *fp = stdin;
char inputLine[8192];
char timestr[64];
time_t seconds, prev_seconds, start_seconds;
double difference;

time(&start_seconds);
time(&prev_seconds);
while (fgets(inputLine, 8192, fp) != NULL) {

  time(&seconds);
  difference = difftime(seconds, prev_seconds);
  time(&prev_seconds);

  strcpy(timestr, ctime(&seconds));
  timestr[24] = '\0';
  printf("%5.0f:%s:%s", difference, timestr, inputLine);
  fflush(stdout);

}

strcpy(timestr, ctime(&start_seconds));
timestr[24] = '\0';
printf("\nStart time: %s\n", timestr);
strcpy(timestr, ctime(&seconds));
timestr[24] = '\0';
printf("End time:   %s\n", timestr);
difference = difftime(seconds, start_seconds);
printf("Elapsed time: %6.0f seconds\n", difference);

}
