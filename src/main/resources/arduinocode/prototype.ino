#include <Servo.h> 
Servo horizontalServo;
boolean isPreferences = true;
boolean isDetected;
boolean isVertical;
byte sendedPreferences;
int sendedValue;

void setup() {
  horizontalServo.attach(9);
  Serial.begin(9600);
}

void rotateServo() {
  for (int i = 0; i < 180; i+=10) {
    horizontalServo.write(i);
    delay(100);
  }
}

void loop() {
  while (Serial.available()) {
    if (isPreferences) {
      sendedPreferences = Serial.read();
      isDetected = bitRead(sendedPreferences, 0);
      isVertical = bitRead(sendedPreferences, 1);
      isPreferences = false;
    } else {
      isPreferences = true;
      sendedValue = (int)Serial.read();
      
      if (isVertical) {
        // вращаем серво вертикального направления
      } else {
        horizontalServo.write(sendedValue);
      }
    }
    
  }
}

