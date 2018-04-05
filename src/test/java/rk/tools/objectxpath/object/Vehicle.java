package rk.tools.objectxpath.object;

import java.util.List;

public class Vehicle {
    private List<Gear> gears;
    public int serialNumber;
    public List<Integer> countryCodes;
    public Engine engine;

    public List<Gear> getGears() {
        return gears;
    }

    public void setGears(List<Gear> gears) {
        this.gears = gears;
    }
}
