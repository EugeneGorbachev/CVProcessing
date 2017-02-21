#include <Servo.h>
int joyX=0; //Положение джостика
int angl=0; //Угол серво
Servo horizontalServo;
void setup() {
horizontalServo.attach(11);
pinMode(joyX,INPUT);
Serial.begin(9600);
}
void loop() {
  angl = analogRead(joyX); //Считываем аналоговый вход (влево 0 вправо 1023)
 
  angl = map(angl,0,1023,0,180);
  horizontalServo.write(angl);
  
}
    
     
   

