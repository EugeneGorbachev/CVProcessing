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
  while (Serial.available()) {
    if (isPreferences) {
      sendedPreferences = Serial.read();
      isDetected = bitRead(sendedPreferences, 0);
      isVertical = bitRead(sendedPreferences, 1);
      isPreferences = false;
    } else {
      sendedValue = (int)Serial.read();
      
      if (isDetected) {
        digitalWrite(LED_BUILTIN,HIGH);
      } 
      else {
        digitalWrite(LED_BUILTIN,LOW);
      }
      
      lcd.setCursor(0,1);
      lcd.print("            ");
      lcd.setCursor(0,1);
      if (isDetected){
        lcd.print("I see you");
          // включить синию лампу
        digitalWrite(ledBlue,HIGH);
        digitalWrite(ledRed,LOW);
      } 
      else {
        lcd.print("I feel you");
        // включить красную лампу
        digitalWrite(ledRed,HIGH);
        digitalWrite(ledBlue,LOW);
      }
      
      if (isVertical) {
        verticalServo.write(sendedValue);
      } else {
        i = map(sendedValue,0,180,0,16);
        horizontalServo.write(sendedValue);
      }
      
      // from "i" depend left or right
      lcd.setCursor(i,0);
      lcd.write(4);
      delay(10);
      lcd.setCursor(i,0);
      lcd.write(1);
         
      isPreferences = true;
    }
  }
}
 
    


