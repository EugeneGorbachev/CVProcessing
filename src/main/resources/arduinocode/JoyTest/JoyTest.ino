#include <Servo.h>
int joyX=0; //analog A0
int joyY=0; //analog A1
int btnState=0; // knob A3
int pos=0; //Угол серво
Servo horizontalServo,verticalServo;
void setup() {
horizontalServo.attach(8);
verticalServo.attach(9);
pinMode(joyX,INPUT);
pinMode(joyY,INPUT);
pinMode(btn,INPUT);

Serial.begin(9600);
}
void loop() {
analogRead(KnobZ);
  if (KnobZ>0)
  {
  
   
  pos = analogRead(joyX); //Считываем аналоговый вход (влево 0 вправо 1023)
  pos = map(pos,0,1023,0,180);
  verticalServo.write(180-pos);
  
  pos = analogRead(joyY);
  pos = map(pos,0,1023,0,180);
  horizontalServo.write(pos);
  }
  
}
    
     
   

