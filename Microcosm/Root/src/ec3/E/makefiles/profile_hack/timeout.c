#include    <stdio.h>
#include    <unistd.h>
#include    <sys/types.h>
#include    <signal.h>

void    handler();

int main(argc,argv) 
int argc;
char    *argv[];
{
    pid_t   pid;
    int timeout = 0;
    extern  char    *optarg;
    extern  int optind;
    int c;

    if (argc == 1) {
            fprintf(stderr,"usage: timeout -t n command options\n");
            exit(1);
    }

    while ((c = getopt(argc,argv,"t:")) != EOF)
        switch (c) {
        case 't':
            timeout = atoi(optarg);
            break;
        case '?':
            fprintf(stderr,"usage: timeout -t n command options\n");
            exit(1);
            break;
        }

    if (timeout) {
    if ((pid= fork()) != 0) {
        /* parent */
        signal(SIGCHLD,(void*)handler); /* die if child dies */
        sleep(timeout);
        kill(pid, SIGKILL); /* kill parent */
        exit(1);
        }
    }
/* child */
    execvp(argv[optind],&argv[optind]);
    exit(0);
}

void handler() {
exit(0);
}
