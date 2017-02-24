#include <Servo.h>
int joyX=0; //analog A0
int joyY=0; //analog A1
int btn=0; // button 
int pos=0; //Угол серво
Servo horizontalServo, verticalServo;

void setup() {
  horizontalServo.attach(8);
  verticalServo.attach(9);
  
  pinMode(joyX,INPUT);
  pinMode(joyY,INPUT);
  pinMode(btn,INPUT);
  
  Serial.begin(9600);
}
void loop() {
  int z = analogRead(btn);
   
  int x = analogRead(joyX); //Считываем аналоговый вход (влево 0 вправо 1023)
  pos = map(x, 0, 1023, 0, 180);
  verticalServo.write(180 - pos);
  
  int y = analogRead(joyY);
  pos = map(y, 0, 1023, 0, 180);
  horizontalServo.write(pos);

  Serial.print("x: ");
  Serial.println(x);
  Serial.print("y: ");
  Serial.println(y);
  Serial.print("z: ");
  Serial.println(z);
}
