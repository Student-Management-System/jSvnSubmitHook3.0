/**
 * 
 */
package de.uni_hildesheim.people;

/**
 * @author XXX
 *
 */
import java.util.Scanner;
public class Person {
    String vorname;
    String nachname;
    public Date geburtsdatum;
    int dasAlter;

    public Person(String vorNam, String nachNam, Date gebDat) {
        this.vorname = vorNam;
        this.nachname = nachNam;
        this.geburtsdatum = gebDat;
        this.dasAlter = getAge(geburtsdatum);
    }

    /**
     * @param args
     */
    //Hauptprogramm, das die Methoden testet und den Konstruktor fuellen kann
    public static void main(String[] args) {
        // TODO Auto-generated method stub
        Scanner scanner = new Scanner(System.in);
        int a;
        int b;
        int c;
        //Eingabe des Beburtsjahres >0
        do{
            System.out.println("Gib ein Jahr ein");
            c = scanner.nextInt();
        } while(c < 0);
        do {
            System.out.println("Gib einen Monat ein");
            b = scanner.nextInt();
        } while(b <= 0||b > 12);

        boolean stimmig;
        //Eingabe des Tages, Pruefung auf Stimmigkeit zum Monat. Schaltjahre wurden aussen vor gelassen
        do {
            stimmig = false;
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

        //Uebergabe der Werte an Konstruktor
        Date geburtsd = new Date(a, b, c);
        scanner.nextLine();
        System.out.println("Gib deinen Vornamen ein");
        String vorn = scanner.nextLine();
        System.out.println("Gib deinen Nachnamen ein");
        String nachn = scanner.nextLine();
        Person person = new Person(vorn, nachn, geburtsd);

        //Funktionalitaetsbeweise:
        person.asString();
        System.out.println("Das Alter betraegt " + person.getAge(geburtsdatum));
    }

    //Rueckgabe des Alters
    public int getAge(Date geburt) {
        int alter = geburt.getYearDiff(geburt);
        return alter;
    }

    //Statusausgabe des entsprechenden Menschens
    public void asString() {
        String zurueck = nachname + ", " + vorname + " geboren am " + geburtsdatum.asString();
        System.out.println(zurueck);
    }
}