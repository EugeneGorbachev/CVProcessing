#include <Servo.h>
int joy_X=0; //analog A0
int joy_Y=1; //analog A1
int btn_state=13; // knob A3
int pos=0; //Угол серво
Servo horizontalServo,verticalServo;
void setup() {
horizontalServo.attach(8);
verticalServo.attach(9);
pinMode(joy_X,INPUT);
pinMode(joy_Y,INPUT);
pinMode(12,OUTPUT);

pinMode(btn_state,INPUT);

Serial.begin(9600);
}
void loop() {
digitalWrite(btn_state, HIGH);
  
   if (digitalRead(btn_state) == LOW) {
digitalWrite(12,LOW); 
// Кнопка нажата
pos = analogRead(joy_X); //Считываем аналоговый вход (влево 0 вправо 1023)
  pos = map(pos,0,1023,0,180);
  verticalServo.write(180-pos);
  
  pos = analogRead(joy_Y);
  pos = map(pos,0,1023,0,180);
  horizontalServo.write(pos);

} else {
 digitalWrite(12,HIGH); 
// Кнопка не нажата


}
  
  
   
  
}
    
     
   

