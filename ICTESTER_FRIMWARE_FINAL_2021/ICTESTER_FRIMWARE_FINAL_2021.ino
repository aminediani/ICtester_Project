#include <Arduino.h>
#include <Wire.h>
#include <LiquidCrystal.h>
const int rs = 12, en = 11, d4 = 5, d5 = 4, d6 = 3, d7 = 2;
LiquidCrystal lcd(rs, en, d4, d5, d6, d7);
int test_result = 0;
double stat_L = 0.00, stat_H = 0.00, stat_N = 0.00;
int GREEN_LED = 13, RED_LED = 6;
int i = 0;
int start_stop_b = 10;
int start_stop_stat = 0;
int step_stat = 0;
int step_b = 7;
int mode_j = 0;
int PIN_0 = 9;//data B 
int PIN_1 = 8;//data A
unsigned long time_hor_s, time_hor_f;
double Tvalue = 0.00, val = 0.00;
double A0VAL = 0.0000, A1VAL = 0.000000;
int TIME = 1, pressTime = 200;
bool condi = true;
char mtext;
//_________________________________setup___________________________________________________________
void setup(){
  //button config
  pinMode(step_b, INPUT);
  pinMode(start_stop_b, INPUT);
  // set up the LCD's number of columns and rows:
  lcd.begin(16, 2);
  pinMode(PIN_0, OUTPUT);
  pinMode(PIN_1, OUTPUT);
  pinMode(RED_LED, OUTPUT);
  pinMode(GREEN_LED, OUTPUT);
  Serial.begin(9600);
  lcd.setCursor(0, 0);
  lcd.print("IC-TEST PROTOTYPE ");
  delay(200);
  lcd.setCursor(0, 1);
  lcd.print("AMINE DIANI");
  delay(1000);
  lcd.setCursor(0, 1);
  lcd.print("                     ");
  lcd.setCursor(0, 1);
  lcd.print("STARTING.");
  digitalWrite(GREEN_LED, HIGH);
  digitalWrite(RED_LED, HIGH);
  delay(500);
  lcd.print(".");
  digitalWrite(GREEN_LED, LOW);
  digitalWrite(RED_LED, LOW);
  delay(500);
  lcd.print(".");
  digitalWrite(GREEN_LED, HIGH);
  digitalWrite(RED_LED, HIGH);
  delay(500);
  lcd.print(".");
   start_stop_stat = digitalRead(start_stop_b);
      if (start_stop_stat == 1){
		    lcd.clear();
			lcd.print("ALIM CALIBRATE...");
			lcd.setCursor(0, 1);
			lcd.print("STA: PC STE: EXT" );
			delay(5500);
		
		
	}
  digitalWrite(GREEN_LED, LOW);
  digitalWrite(RED_LED, LOW);
  lcd.clear();
  lcd.setCursor(0, 0);
  delay(500);

  lcd.print("IC TEST: RM3182");
  //Serial.print("START");
  lcd.setCursor(0, 1);
  lcd.print("Press START");
  Serial.println("SYS_RE_START");
}
void loop(){
  //serial  action
   if (Serial.available()) {
	digitalWrite(GREEN_LED, HIGH);
    digitalWrite(RED_LED, HIGH);
    delay(200);
    lcd.clear();
    while (Serial.available() > 0) {
      mtext=Serial.read();
      if(mtext=='v'){
        lcd.setCursor(0, 1);
        lcd.write("connected");
      }
    
      if(mtext=='t'){
        //lcd.write("---------------");
         delay(150);
        if(Serial.read()=='s'){
          //lcd.clear();
          lcd.setCursor(0, 1);
          lcd.write("send data >> >>");
          Serial.println("datastart");
          Serial.println("H");
          Serial.println(Mesuredata(HIGH,LOW));
          Serial.println("L");
          Serial.println(Mesuredata(LOW,HIGH));
          Serial.println("N");
          Serial.println(Mesuredata(LOW,LOW));
          Serial.println("dataend");

        }
      }

    }
  digitalWrite(GREEN_LED, LOW);
  digitalWrite(RED_LED, LOW);
  }
    
  //start button action
  start_stop_stat = digitalRead(start_stop_b);
  if (start_stop_stat == 1)
  {
   // time_hor_s = millis();
    test_result = 0;
    //LED REST CONFIG
    digitalWrite(GREEN_LED, LOW);
    digitalWrite(RED_LED, LOW);


    //LCD REST CONFIG
    lcd.setCursor(0, 1);
    lcd.print("                ");

    //input_set--------STAT 1/3: LOW  ---------------------
    delay(TIME / 2);
    stat_L = Mesuredata(LOW,HIGH);//Calcul and get the OUT valeur of "HIGH" stat
    lcd.setCursor(0, 1);
    lcd.print("LOW STAT :");
    lcd.print(stat_L); 
  
    //input_set--------STAT 2/3; NULL  -------------------------
    delay(TIME / 2);
    stat_N = Mesuredata(LOW,LOW);//Calcul and get the OUT valeur of "NULL" stat
    lcd.setCursor(0, 1);
    lcd.print("NULL STAT : ");
    lcd.print(stat_N); 
  
    //input_set-------STAT 3/3 : HIGH  --------------------
    delay(TIME / 2);
    stat_H = Mesuredata(HIGH,LOW);//Calcul and get the OUT valeur of "HIGH" stat
    lcd.setCursor(0, 1);
    lcd.print("HIGH STAT: ");
    lcd.print(stat_H);

   //comparation data
   // 1/3 stat HIGH
    if (stat_H > 4.0 && stat_H < 5.25)
    { }
    else
    {
      test_result = 1;
    }
    // 2/3 stat LOW
    if (stat_L < -4.0 && stat_L > -5.25)
    {}
    else
    {
      test_result = 2;
    }
    // 3/3 stat NULL
    if (stat_N > -0.25 && stat_N < 0.25)
    {}
    else
    {
      test_result = 3;
    }

    delay(TIME / 2);
    //Resultat
    if (test_result == 0){ //TEST PASS
      TIME = 10;
      digitalWrite(GREEN_LED, HIGH);
      lcd.setCursor(0, 1);
      lcd.print("   TEST PASSED   ");
    } else {//TEST FAIL
      TIME = 1000;
      digitalWrite(RED_LED, HIGH);
      lcd.setCursor(0, 1);
      lcd.print("   TEST FAIL !!  ");
    }
    delay(500);//attend button
  } //fin START STOP EVENT
  //step mode button action
  step_stat = digitalRead(step_b);
  if (step_stat == HIGH)
  {
    delay(10);   //attend button
    lcd.clear(); //affiche step mode dans lcd
    lcd.print("STEP MODE");
    delay(750);

    mode_j = 2;
    digitalWrite(PIN_0, LOW);
    digitalWrite(PIN_1, HIGH); //STAT SIGNAL H
    lcd.clear();
    lcd.print("STEP TEST: +5V");
	digitalWrite(GREEN_LED, LOW);
        digitalWrite(RED_LED, LOW);
    condi = true;
    while (condi)
    {
      step_stat = digitalRead(step_b);
      if (step_stat == HIGH && mode_j == 1)
      {
        step_stat = 0;
        delay(pressTime);
        mode_j = 2;
        digitalWrite(PIN_0, LOW);
        digitalWrite(PIN_1, HIGH); //STAT SIGNAL H
        lcd.clear();
        lcd.print("STEP TEST: +5V");
		
      }
      if (step_stat == HIGH && mode_j == 2)
      {
        step_stat = 0;
        delay(pressTime);
        mode_j = 3;
        digitalWrite(PIN_0, HIGH);
        digitalWrite(PIN_1, LOW); //STAT SIGNAL L
        lcd.clear();
        lcd.print("STEP TEST: -5V");
		
      }
      if (step_stat == HIGH && mode_j == 3)
      {
        step_stat = 0;
        delay(pressTime);
        mode_j = 1;
        digitalWrite(PIN_0, LOW);
        digitalWrite(PIN_1, LOW); //STAT SIGNAL N
        lcd.clear();
        lcd.print("STEP TEST: 0.0V");
		
      }
      double Mesure();
      Tvalue = Mesure();
	  //************************
	  delay(100);
	  /*A0VAL = analogRead(A0);
	  A1VAL = analogRead(A1);
	  Serial.println("*");
		  Serial.print("A0 : ");Serial.print(A0VAL);Serial.print(" voltage :");Serial.println(0.0099*A0VAL+1.1604);
		  Serial.println(A1VAL);
		  //Serial.print("A1 range[+0 ,+2]:  ");Serial.println(0.0348*A1VAL-26.7484);
		  //Serial.print("A1 RANGE[-5 ,+0]:  ");Serial.println(0.035*A1VAL-27);
		  Serial.print("A1 range[-10 ,-5]: ");Serial.println(0.0336*A1VAL-26.09018);
		  //Serial.print("A1  V= : ");Serial.println(3*0.000001*A1VAL*A1VAL+0.0306*A1VAL-25.218);
		  */
		  //*****************
	  
      if (Tvalue == 99.99)
      {
        lcd.setCursor(0, 1);
        lcd.print("Voltage = ");
        lcd.print("  OL ");
      }
      else
      {
        lcd.setCursor(0, 1);
        lcd.print("Voltage = ");
        lcd.print(Tvalue);
        lcd.print("V");
      }

      start_stop_stat = digitalRead(start_stop_b);
      if (start_stop_stat == HIGH)
      {
        condi = false;
        lcd.clear();
        lcd.print("IC TEST: RM3182");
        lcd.setCursor(0, 1);
        lcd.print("Press START");
        delay(100);
		digitalWrite(GREEN_LED, LOW);
    digitalWrite(RED_LED, LOW);
      }
    }
  }

}

double Mesure(){
  A0VAL = analogRead(A0);
  A1VAL = analogRead(A1);
  if (A0VAL >= 93 && A0VAL <= 904){
    val = 0.0099*A0VAL+1.1604;
  }
  else if (A1VAL >= 418 && A1VAL <= 713){
    val = 0.0336*A1VAL-26.09018;
  }
  else if (A1VAL >= 714 && A1VAL <= 835){
    val = -25.736 + 0.0333*A1VAL;
  }
  else{
    val = 99.99;
  }
  return val;
}

double Mesuredata(int aStat,int bStat)
{
digitalWrite(PIN_0, bStat);
digitalWrite(PIN_1, aStat);
delay(100);
return Mesure();
}
    