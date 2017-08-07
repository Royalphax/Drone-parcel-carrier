#include <Adafruit_GPS.h>
#include <SoftwareSerial.h>

SoftwareSerial mySerial(8, 7);
Adafruit_GPS GPS(&mySerial);

#define GPSECHO  false

// On declare les variables globales
boolean usingInterrupt = false;
const int buttonPin = 15;
const int leftLedPin = 14;
const int rightLedPin = 16;

// On declare les fonctions
void useInterrupt(boolean);
float flightDistance(float, float, float, float);
float convertToRadian(float);
void makePath(float, float, float, float);
void moveTo(int);
boolean isMovingBack(int, float, float);
boolean isOnForbiddenDiagonal(float, float);
int skirtObstacle(int, float, float);
boolean plusOrLessEquals(float, float, double, boolean);
long convert(float);

void setup()
{
  // baud 115200 assez rapide pour afficher nos donnees
  Serial.begin(115200);
  Serial.println("Initialisation ... ");

  // on declare les entrees/sorties
  pinMode(buttonPin, INPUT);
  pinMode(leftLedPin, OUTPUT);
  pinMode(rightLedPin, OUTPUT);

  // baud par defaut pour l'UltimateShieldGPS
  GPS.begin(9600);
  
  GPS.sendCommand(PMTK_SET_NMEA_OUTPUT_RMCGGA);
  GPS.sendCommand(PMTK_SET_NMEA_UPDATE_1HZ);
  GPS.sendCommand(PGCMD_ANTENNA);

  // systeme d'interuption pour mettre a jour les coordonnees si elles ont changees
  useInterrupt(true);

  delay(1000);
  mySerial.println(PMTK_Q_RELEASE);

}


// Systeme d'interuption appelle chaque milliseconde. Il cherche si de nouvelles donnees GPS sont dispo
SIGNAL(TIMER0_COMPA_vect) {
  char c = GPS.read();
#ifdef UDR0
  if (GPSECHO)
    if (c) UDR0 = c;  
#endif
}

void useInterrupt(boolean v) {
  if (v) {
    OCR0A = 0xAF;
    TIMSK0 |= _BV(OCIE0A);
    usingInterrupt = true;
  } else {
    TIMSK0 &= ~_BV(OCIE0A);
    usingInterrupt = false;
  }
}

// On déclare d'autres variables globales
uint32_t timer = millis();
long xMax;
long zMax;
long res = 1;
//--------------------------// Coordonnées du point de destination à insérer ici
float latDest = 45.233704; // -> Correspond à la variable z
float lonDest = 4.678493; // -> Correspond à la variable x
//-----------------------//
void loop()
{
  if (!usingInterrupt) {
    char c = GPS.read();
    if (GPSECHO)
      if (c) Serial.print(c);
  }
  
  if (GPS.newNMEAreceived() && !GPS.parse(GPS.lastNMEA())) {
    return;
  }

  if (timer > millis())  timer = millis();

  if (millis() - timer > 1000) { 
    timer = millis(); // reset the timer
    
    Serial.print("\nFix: "); Serial.print((int)GPS.fix);
    Serial.print(" quality: "); Serial.println((int)GPS.fixquality); 
    if (GPS.fix) {
      float lat = GPS.latitudeDegrees;
      float lon = GPS.longitudeDegrees;
      /*Serial.print(GPS.latitude, 4); Serial.print(GPS.lat);
      Serial.print(", "); 
      Serial.print(GPS.longitude, 4); Serial.println(GPS.lon);*/
      Serial.print("Position actuelle: ");
      Serial.print(lat, 6);
      Serial.print(", "); 
      Serial.println(lon, 6);
      /*float vitesse = GPS.speed * 1.852;
      Serial.print("Vitesse (Km/h): "); Serial.println(vitesse);
      Serial.print("Altitude: "); Serial.println(GPS.altitude);*/
      
      int buttonState = digitalRead(buttonPin);
      
      if (buttonState == HIGH) {
        while (buttonState == HIGH) {
          buttonState = digitalRead(buttonPin);
        }
        if (latDest && lonDest) {
          makePath(convert(lat), convert(lon), convert(latDest), convert(lonDest));
        } else {
          Serial.println("Aucune destination pour le moment.");
        }
      }
    }
  }
}

void makePath(long lat_deg_from, long lon_deg_from, long lat_deg_to, long lon_deg_to) {
  Serial.println("--- DEBUT DE L'ALGORITHME DE DEPLACEMENT ---");

  Serial.print(lat_deg_from); Serial.print(", ");
  Serial.print(lon_deg_from); Serial.print(" to ");
  Serial.print(lat_deg_to); Serial.print(", ");
  Serial.println(lon_deg_to);

  zMax = (lat_deg_to - lat_deg_from);
  xMax = (lon_deg_to - lon_deg_from);

  long x = xMax;
  long z = zMax;

  boolean obstacle = false;

  while (x != 0 || z != 0) {

    boolean X_higher_or_equal_to_Z = ((x < 0 ? -x : x) >= (z < 0 ? -z : z) ? true : false);
    boolean X_lower_to_Z = ((x < 0 ? -x : x) < (z < 0 ? -z : z) ? true : false);

    int dir = -1;

    if (x > 0) {
      if (z > 0) {
        if (X_higher_or_equal_to_Z) {
          dir = 3; // Droite
        } else if (X_lower_to_Z) {
          dir = 0; // Avance
        }
      } else if (z < 0) {
        if (X_higher_or_equal_to_Z) {
          dir = 3; // Droite
        } else if (X_lower_to_Z) {
          dir = 1; // Recul
        }
      } else {
        dir = 3; // Droite
      }
    } else if (x < 0) {
      if (z > 0) {
        if (X_higher_or_equal_to_Z) {
          dir = 2; // Gauche
        } else if (X_lower_to_Z) {
          dir = 0; // Avance
        }
      } else if (z < 0) {
        if (X_higher_or_equal_to_Z) {
          dir = 2; // Gauche
        } else if (X_lower_to_Z) {
          dir = 1; // Recul
        }
      } else {
        dir = 2; // Gauche
      }
    } else {
      if (z > 0) {
        dir = 0; // Avance
      } else if (z < 0) {
        dir = 1; // Recul
      }
    }
    
    int buttonState = digitalRead(buttonPin);
    
    if (buttonState == HIGH) {
      obstacle = true;
      Serial.println("L'obstacle sera pris en compte, veuillez relacher le bouton.");
      while (buttonState == HIGH) {
        buttonState = digitalRead(buttonPin);
      }
    }

    if (isMovingBack(dir, x, z) && !obstacle) {
      break;
    } else if (obstacle) {
      obstacle = false;
      if (isOnForbiddenDiagonal(x, z)) {
        Serial.println("Le drone prend de l'altitude.");
      } else {
        dir = skirtObstacle(dir, x, z);
      }
    }

    switch (dir) {
      case 0:
        z -= res;
        break;
      case 1:
        z += res;
        break;
      case 2:
        x += res;
        break;
      case 3:
        x -= res;
        break;
    }

    moveTo(dir);
  }
  Serial.println("");
  Serial.println("--- FIN DE L'ALGORITHME DE DEPLACEMENT ---");
}

void moveTo(int dir) {
  /**
   * Les différentes directions :
   * 0 = AVANCE
   * 1 = ARRIERE (On avance mais avec un orientation sud)
   * 2 = GAUCHE
   * 3 = DROITE
   */
  switch (dir) {
  case 0:
    Serial.println("AVANCE (NORD)");
    digitalWrite(rightLedPin, HIGH);
    digitalWrite(leftLedPin, HIGH);
    delay(1500);
    digitalWrite(rightLedPin, LOW);
    digitalWrite(leftLedPin, LOW);
    break;
  case 1:
    Serial.println("AVANCE (SUD)");
    digitalWrite(rightLedPin, HIGH);
    digitalWrite(leftLedPin, HIGH);
    delay(1500);
    digitalWrite(rightLedPin, LOW);
    digitalWrite(leftLedPin, LOW);
    break;
  case 2:
    Serial.println("GAUCHE");
    digitalWrite(leftLedPin, HIGH);
    delay(1500);
    digitalWrite(leftLedPin, LOW);
    break;
  case 3:
    Serial.println("DROITE");
    digitalWrite(rightLedPin, HIGH);
    delay(1500);
    digitalWrite(rightLedPin, LOW);
    break;
  }
  delay(1000);
}

boolean isMovingBack(int dir, long x, long z) {
  long xCopy = x;
  long zCopy = z;
  switch (dir) {
  case 0: // Avance
    zCopy -= res;
    break;
  case 1: // Recul
    zCopy += res;
    break;
  case 2: // Gauche
    xCopy += res;
    break;
  case 3: // Droite
    xCopy -= res;
    break;
  }
  return ((xCopy < 0 ? -xCopy : xCopy) > (x < 0 ? -x : x) || (zCopy < 0 ? -zCopy : zCopy) > (z < 0 ? -z : z));
}

boolean isOnForbiddenDiagonal(long x, long z) {
  if (plusOrLessEquals(xMax, 0, res, true) || plusOrLessEquals(zMax, 0, res, true))
    return true;
  if (!plusOrLessEquals((x < 0 ? -x : x), (z < 0 ? -z : z), res, false))
    return false;
  if ((xMax > 0 && zMax < 0) || (xMax < 0 && zMax > 0)) {
    if ((x < 0 && z < 0) || (x > 0 && z > 0))
      return true;
  }
  if ((xMax > 0 && zMax > 0) || (xMax < 0 && zMax < 0)) {
    if ((x > 0 && z < 0) || (x < 0 && z > 0))
      return true;
  }
  return false;
}

int skirtObstacle(int dir, long x, long z) {
  switch (dir) {
  case 0:
    if (xMax > 0) {
      return 3; // Droite
    } else if (xMax < 0) {
      return 2; // Gauche
    }
    break;
  case 1:
    if (xMax > 0) {
      return 3; // Droite
    } else if (xMax < 0) {
      return 2; // Gauche
    }
    break;
  case 2:
    if (zMax > 0) {
      return 0; // Avance
    } else if (zMax < 0) {
      return 1; // Recul
    }
    break;
  case 3:
    if (zMax > 0) {
      return 0; // Avance
    } else if (zMax < 0) {
      return 1; // Recul
    }
    break;
  }
  return dir;
}

boolean plusOrLessEquals(long value1, long value2, double fluc, boolean strictEquality) {
  if (strictEquality) {
    if (value1 == value2)
      return true;
    if (value1  > value2 - fluc && value1 < value2)
      return true;
    if (value1  < value2 + fluc && value1 > value2)
      return true;
  } else {
    if (value1 == value2)
      return true;
    if (value1 >= value2 - fluc && value1 <= value2)
      return true;
    if (value1 <= value2 + fluc && value1 >= value2)
      return true;
  }
  return false;
}

long convert(float value) {
  float f1 = (value*100000);
  String s1 = String(f1);
  long l1 = s1.toInt();
  return l1;
}
