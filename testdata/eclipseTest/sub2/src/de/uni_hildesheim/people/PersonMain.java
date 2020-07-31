/**
 * 
 */
import java.util.Scanner;
import de.uni_hildesheim.people.*;

/**
 * @author XXX
 *
 */
public class PersonMain {

    /**
     * @param args
     */
    //Hauptmethode
    public static void main(String[] args) {
        // TODO Auto-generated method stub
        int personenZahl = 0;
        int zaehler = 1;
        Person[] gruppe = new Person[5];
        Date[] daten = new Date[5];
        Scanner scanner = new Scanner(System.in);
        int a;
        int b;
        int c;
        Date geburtsd;
        //Anzahl der erstellten Personen DAVOR
        System.out.println("Bisher erstellte Personen: " + personenZahl);

        //Fuenfmalige Fuellung der Attribute fuer Person
        for(int i = 0; i < 5; i++) {
            do{
                System.out.println(zaehler + ". Person! Gib ein Jahr ein");
                c = scanner.nextInt();
            } while(c < 0);

            do {
                System.out.println("Gib einen Monat ein");
                b = scanner.nextInt();
            } while(b <= 0||b > 12);

            boolean stimmig = false;

            //Eingabe des Tages, Pruefung auf Stimmigkeit zum Monat. Schaltjahre wurden aussen vor gelassen
            do {
                System.out.println("Gib einen Tag ein");
                a = scanner.nextInt();
                if(b == 1||b == 3||b == 5||b == 7||b == 8||b == 10||b == 12) {
                    if(a > 0 && a <= 31){
                        stimmig = true;
                    } 
                } else if(b == 4||b == 6||b == 9||b == 11) {
                    if(a > 0 && a <= 30) {
                        stimmig = true;
                    }	
                } else if(b == 2) {
                    if(a > 0 && a <= 28) {
                        stimmig = true;
                    }
                }
            } while(!stimmig);

            geburtsd = new Date(a, b, c);
            daten[i] = geburtsd;
            scanner.nextLine();
            System.out.println("Gib deinen Vornamen ein");
            String vorn = scanner.nextLine();
            System.out.println("Gib deinen Nachnamen ein");
            String nachn = scanner.nextLine();
            Person person = new Person(vorn, nachn, geburtsd);
            //Ausgabe Personenzahl IM Feld
            System.out.println("Erstellte Personen: " + personenZahl);
            gruppe[i] = person;
            zaehler += 1;
            personenZahl += 1;
        }

        for(int j = 0; j < 5; j++) {
            //Ausgaben von Status, Alter, Anzahl der gesamten Personen im Feld
            gruppe[j].asString();
            System.out.println("Das Alter betrgt " + gruppe[j].getAge(daten[j]));
            System.out.println("Insgesamt erstellte Personen: " + personenZahl);
        }
    }
}