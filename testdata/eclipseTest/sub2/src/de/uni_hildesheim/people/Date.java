/**
 * 
 */
package de.uni_hildesheim.people;

/**
 * @author XXX
 *
 */
import java.util.Scanner;

public class Date {
    private int tag;
    private int monat;
    private int jahr;

    public Date(int t; int m; int j) {
        tag = t;
        monat = m;
        jahr = j;
    }

    /**
     * @param args
     */
    // Hauptprogramm
    public static void main(String[] args) {
        // TODO Auto-generated method stub
        Date ankunftUni = new Date(14, 10, 2019);
        System.out.println("Die Uni begann fuer uns am " + ankunftUni.asString());
        System.out.println("Wir sind seit " + getYearDiff(ankunftUni) + " Jahr/en hier.");
    }

    // Statusausgabe
    public String asString() {
        String back;
        back = tag + "." + monat + "." + jahr;
        return back;
    }

    // Berechnung des Alters
    public int getYearDiff(Date datum) {
        Date heute = new Date(17, 12, 2019);
        int diff;
        if (heute.jahr >= datum.jahr) {
            diff = heute.jahr - datum.jahr + 1;
        } else if (heute.jahr == datum.jahr && heute.monat == datum.monat && heute.tag >= datum.tag) {
            diff = datum.jahr - heute.jahr + 1;
        } else {
            diff = datum.jahr - heute.jahr + 1;
        }
        return diff;
    }

}