// JP 138 , Oktay Samast, Albert Schmal
package de.weihnachten;

public class Weihnachtsmann extends WeihnachtsObjekt {
    private double futtervorrat;
    private Schlitten schlitten;


    public Weihnachtsmann(double futtervorrat, Schlitten schlitten) {
        this.futtervorrat = futtervorrat;
        this.schlitten = schlitten;
    }

    public boolean istFertig() {
        // 
        boolean geschenkeAusgeliefert = false;
        if (this.schlitten.istLeer()) {
            return true;
        }
        return geschenkeAusgeliefert;
    }
    public void naechstesGeschenkAusliefern() {
        if (this.schlitten.istLeer()) {
            System.out.println("Der Weihnachtsmann ist fertig mit der "
                             + "Auslieferung. Schöne Weihnachten.");
        } else {
            System.out.println(this.schlitten.getNaechstesGeschenk());
        }
    }
    public boolean kannNochFuettern() {
        double gesamtHunger = 0;
        Geschenk naechstGeschenk = schlitten.getNaechstesGeschenk();

        for (int i = 0; i <= schlitten.getAnzahlRentiere(); i++) {
            gesamtHunger += schlitten.getRentier(i).getHunger(naechstGeschenk.getGewicht());
        }

        return futtervorrat > gesamtHunger;
    }

    // Methode fuettern
    public void fuettern() {
        // Iteriere über i solange i <= Anzahl Rentiere.
        for (int i = 0; i <= schlitten.getAnzahlRentiere(); i++) {
            // Verringere Futtervorrat durch den Hunger des Rentiers an Stelle i
            futtervorrat -= schlitten.getRentier(i).getHunger();
        }
    }


}