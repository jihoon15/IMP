//
// Created by JiHun on 2020-06-25.
//

#include "com_daisy_flappybird_GameActivity.h"
#include <unistd.h>
#include <fcntl.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <jni.h>

short led_state[9] = {0,
                    0b1,
                    0b11,
                    0b111,
                    0b1111,
                    0b11111,
                    0b111111,
                    0b1111111,
                    0b11111111};//한칸씩 더하고 줄어지게 1~8
//led, jump_cnt ctrl
JNIEXPORT void JNICALL Java_com_daisy_flappybird_GameActivity_LED_1CTRL
(JNIEnv *env, jobject this_ptr, jint num)
{

    int ret;
    int fd;
    short value;
    //if(num < 1){value = 0;}
    //else{value = led_state[num - 1];}
    value = led_state[num];
    //int number = num;
    fd = open("/dev/fpga_led", O_RDWR);
    if(fd == -1)return;

    ret = write(fd, &value, sizeof(value));
    close(fd);
}
//ledall on
JNIEXPORT void JNICALL Java_com_daisy_flappybird_GameActivity_LED_1ALL_1ON
(JNIEnv *env, jobject this_ptr)
{
    short value = led_state[8];
    int ret;
    int fd;

    fd = open("/dev/fpga_led", O_RDWR);
    if(fd == -1)return;
    ret = write(fd, &value, sizeof(value));
    close(fd);
}
//led all off
JNIEXPORT void JNICALL Java_com_daisy_flappybird_GameActivity_LED_1ALL_1OFF
(JNIEnv *env, jobject this_ptr)
{
    short value = 0;
    int ret;
    int fd;

    fd = open("/dev/fpga_led", O_RDWR);
    if(fd == -1)return;
    ret = write(fd, &value, sizeof(value));
    close(fd);
}
//jump sound
JNIEXPORT void JNICALL Java_com_daisy_flappybird_GameActivity_JUMPSOUND
(JNIEnv *env, jobject this_ptr)
{
    int x = 0;
    int fd,ret;
    int data = 0x42;
    fd = open("/dev/fpga_piezo",O_WRONLY);
    if(fd == -1)return;
    for(int i = 0; i < 10000; i++)write(fd,&data,1);


    write(fd,&x,1);

    close(fd);
}
//dead sound
JNIEXPORT void JNICALL Java_com_daisy_flappybird_GameActivity_DEADSOUND
(JNIEnv *env, jobject this_ptr)
{
    int x = 0;
    int fd,ret;
    int data = 0x1;
    fd = open("/dev/fpga_piezo",O_WRONLY);
    if(fd == -1)return;
    for(int i = 0; i < 50000; i++)write(fd,&data,1);

    write(fd,&x,1);

    close(fd);
}