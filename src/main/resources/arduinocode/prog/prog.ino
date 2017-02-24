#include <Servo.h> 
#include <LiquidCrystal.h>

Servo verticalServo;
Servo horizontalServo;
boolean is_pref = true;// sent value contains preferences
boolean is_detected;
boolean is_vertical;

int sent_value = 0;
byte sent_pref;

int joy_x = 0;
int joy_y = 1;
int joy_input_error = 50;
boolean prev_joy_input = false;
boolean joy_input = false;

int led_blue = 12;
int led_red = 11;
int lcd_cursor_pos = 0;

LiquidCrystal lcd(2, 3, 4, 5, 6, 7);// initialize the library with the numbers of the interface pins
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
  pinMode(led_blue, OUTPUT);
  pinMode(led_red, OUTPUT);

  horizontalServo.attach(8);
  verticalServo.attach(9);

  pinMode(joy_x,INPUT);
  pinMode(joy_y,INPUT);

  lcd.begin(16, 2); 
  lcd.createChar(1, p40);
  lcd.createChar(4, p100);

  Serial.begin(9600);
}

void loop() {
  int x = analogRead(joy_x);
  int y = analogRead(joy_y);

  prev_joy_input = joy_input;
  joy_input = abs(x - 500) > joy_input_error || abs (y - 500) > joy_input_error;

  if (prev_joy_input || joy_input) {
    sent_value = map(x, 0, 1023, 0, 180);
    verticalServo.write(180 - sent_value);

    sent_value = map(y, 0, 1023, 0, 180);
    horizontalServo.write(sent_value);
  } else {
      while (Serial.available()) {
        if (is_pref) {
          sent_pref = Serial.read();

          is_detected = bitRead(sent_pref, 0);
          is_vertical = bitRead(sent_pref, 1);

          is_pref = false;
        } else {
          sent_value = (int)Serial.read();

          if (is_detected) {
            digitalWrite(LED_BUILTIN,HIGH);

            lcd.setCursor(0, 1);
            lcd.print("            ");
            lcd.setCursor(0, 1);

            lcd.print("I see you");
            digitalWrite(led_blue, HIGH);
            digitalWrite(led_red, LOW);

            if (is_vertical) {
              verticalServo.write(sent_value);
            } else {
              lcd_cursor_pos = map(sent_value, 0, 180, 0, 16);
              horizontalServo.write(sent_value);
            }
          }
          else {
            digitalWrite(LED_BUILTIN,LOW);

            lcd.setCursor(0, 1);
            lcd.print("            ");
            lcd.setCursor(0, 1);

            lcd.print("I feel you");
            digitalWrite(led_red,HIGH);
            digitalWrite(led_blue,LOW);
          }

          // depending on "lcd_cursor_pos" move cursor left or right
          lcd.setCursor(lcd_cursor_pos,0);
          lcd.write(4);// paints with black
          delay(10);
          lcd.setCursor(lcd_cursor_pos,0);
          lcd.write(1);// paint with white

          is_pref = true;
        }
      }
  }
}
 
    


