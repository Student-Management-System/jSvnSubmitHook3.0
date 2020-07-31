// Max Mustermann (0000) JP000
package cars;

import utils.AbstractVehicle;

public class Car extends AbstractVehicle {

    public Car(String name) {
        super(name);
    }
    
    @Override
    public String getName() {
        return "*" + super.getName() + "*";
    }

    // test-main
    public static void main(String[] args) {
    }

}
