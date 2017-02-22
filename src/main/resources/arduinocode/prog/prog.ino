#include <Servo.h> 
//LiquidCrystal library by David A. Mellis, Limor Fried (http://www.ladyada.net)
#include <LiquidCrystal.h>
Servo verticalServo,horizontalServo;

int pos = 0;
int sendedValue; 
byte bytev=0;
bool faceDetected = false;
bool flag = false;
int ledBlue = 12;
int ledRed = 11;
int i = 0;
boolean isPreferences = true;
boolean isDetected;
boolean isVertical;
byte sendedPreferences;
    // initialize the library with the numbers of the interface pins
LiquidCrystal lcd(2, 3, 4, 5, 6, 7);
byte p40[8] = {
  B00000,
  B00000,
  B00000,
  B00000,
  B00000,
  B00000,
  B00000,
};

byte p100[8] = {
  B11111,
  B11111,
  B11111,
  B11111,
  B11111,
  B11111,
  B11111,
};
void setup() {
  pinMode(ledBlue,OUTPUT);
  pinMode(ledRed,OUTPUT);
  horizontalServo.attach(8);
  verticalServo.attach(9);
  Serial.begin(9600);
  sendedValue = 0; 

  lcd.begin(16, 2); 
  lcd.createChar(1, p40);
  lcd.createChar(4, p100);
}
void loop() {
 /* horizontalServo.write(pos);
  verticalServo.write(pos+13);*/

/*void serialEvent() {*/
  while (Serial.available()) {
   /* sendedValue = (byte)Serial.read();
    // получаем первый бит из sendedValue
   if (bitRead(sendedValue, 7) == 0) {
     flag = false;
   } 
   else
     flag = true;
*/
  if (isPreferences) {
      sendedPreferences = Serial.read();
      isDetected = bitRead(sendedPreferences, 0);
      isVertical = bitRead(sendedPreferences, 1);
      isPreferences = false;
    } else {
      isPreferences = true;
      sendedValue = (int)Serial.read();
    
    /*if (flag) {
      digitalWrite(LED_BUILTIN,HIGH);
    } 
    else {
      digitalWrite(LED_BUILTIN,LOW);
    }
    //test*/
    if (isDetected) {
      digitalWrite(LED_BUILTIN,HIGH);
    } 
    else {
      digitalWrite(LED_BUILTIN,LOW);
    }
    }
    /*
    if (faceDetected != flag) {
      faceDetected = flag;
      if (faceDetected)*/
      if (isDetected){
        lcd.setCursor(0,1);
        lcd.print("            ");
        lcd.setCursor(0,1);
        lcd.print("I see you");
        // включить синию лампу
        digitalWrite(ledBlue,HIGH);
        digitalWrite(ledRed,LOW);
      } 
      else {
        lcd.setCursor(0,1);
        lcd.print("            ");
        lcd.setCursor(0,1);
        lcd.print("I feel you");
        // включить красную лампу
        digitalWrite(ledRed,HIGH);
        digitalWrite(ledBlue,LOW);
      }
    } 
      /* bitClear(sendedValue, 7);
        bytev = sendedValue;
        pos = map(bytev,0,127,0,180);
//display pos
        i = map(bytev,0,127,0,16);*/
         if (isVertical) {
          i = map(sendedValue,0,127,0,180);
        verticalServo.write(i);
      } else {
        i = map(sendedValue,0,127,0,180);
        horizontalServo.write(i);
      }

        
// from "i" depend left or right
        lcd.setCursor(i,0);
        lcd.write(4);
        delay(10);
        lcd.setCursor(i,0);
        lcd.write(1);
       
 }
    

 
    


