#include <Servo.h>
int joyX=0; //analog A0
int joyY=1; //analog A1
int btn=2; // button 
int pos=0; //Угол серво
boolean pevJoyInput = false;
boolean joyInput = false;
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
  int x = analogRead(joyX); //Считываем аналоговый вход (влево 0 вправо 1023)
  int y = analogRead(joyY);
  int z = analogRead(btn);
  pevJoyInput = joyInput;
  joyInput = abs(x - 500) > 30 || abs (y - 500) > 30;
  if (pevJoyInput || joyInput){
    pos = map(x, 0, 1023, 0, 180);
    verticalServo.write(180 - pos);
    
    pos = map(y, 0, 1023, 0, 180);
    horizontalServo.write(pos);
  
    Serial.print("x: ");
    Serial.print(x);
    Serial.print(" | y: ");
    Serial.print(y);
    Serial.print(" | z: ");
    Serial.println(z);
  }
}
