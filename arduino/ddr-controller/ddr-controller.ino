/*
 * 8 Game controller buttons:
 *    X      up    O
 *   left        right
 * triangle down square
 *
 * Each controller can be in one of 9 states:
 * psxLeft  0x0001
 * psxDown  0x0002
 * psxRight 0x0004
 * psxUp    0x0008
 *
 * psxSqu   0x0100
 * psxX     0x0200
 * psxO     0x0400
 * psxTri   0x0800
 *
 * nothing  0x0000
 *
 * Serial protocol:
 * frame start 0xAA (1 byte) | pad identifier A=0x01/B=0x02 (1 byte) | button value (2 bytes, mask=0x0F0F) | frame end 0xFF (1 byte)
 */
#include <Psx.h>

#define PAD_A 0x01
#define PAD_B 0x02

Psx psxA;
Psx psxB;

void setup()
{
  // setupPins(dataPin, cmndPin, attPin, clockPin, delay_microseconds);
  psxA.setupPins(4, 5, 6, 7, 10);
  psxB.setupPins(8, 9, 10, 11, 10);
  Serial.begin(115200);
}

void update(byte pad, unsigned int data) {
    Serial.write(0xAA);
    Serial.write(pad);
    Serial.write(data & 0x000F);
    Serial.write((data & 0x0F00) >> 8);
    Serial.write(0xFF);

//    Serial.print("AA");
//    Serial.print("0");
//    Serial.print(pad);
//    Serial.print("0");
//    Serial.print(data & 0x000F);
//    Serial.print("0");
//    Serial.print((data & 0x0F00) >> 8);
//    Serial.println("FF");
}

void loop()
{
  update(PAD_A, psxA.read() & 0x0F0F);
  update(PAD_B, psxB.read() & 0x0F0F);
  
  delay(20);
}

