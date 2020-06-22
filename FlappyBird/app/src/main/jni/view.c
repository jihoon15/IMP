//
// Created by JiHun on 2020-06-16.
//
#include "com_daisy_flappybird_GameView.h"
#include <unistd.h>
#include <fcntl.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <jni.h>

#define FULL_LED1 9
#define FULL_LED2 8
#define FULL_LED3 7
#define FULL_LED4 6
#define ALL_LED 5

JNIEXPORT void JNICALL Java_com_daisy_flappybird_GameView_FULL
(JNIEnv *env, jobject this_ptr)
{
    int fd;
    fd = open("/dev/fpga_fullcolorled", O_WRONLY);
    if(fd == -1)return;

    char buf[3];
    ioctl(fd,ALL_LED);
    buf[0] = 100;buf[1] = 100;buf[2] = 100;
    write(fd,buf,3);
    sleep(1);

    buf[0] = 0;buf[1] = 0;buf[2] = 0;
    ioctl(fd,ALL_LED);
    write(fd,buf,3);
    close(fd);
}
JNIEXPORT void JNICALL Java_com_daisy_flappybird_GameView_SEG
(JNIEnv *env, jobject this_ptr, jint num)
{
    int fd = open("/dev/fpga_segment", O_WRONLY);

    for(int i = 0; i < 150; i++){
    write(fd, &num, 4);
    }
    close(fd);
}