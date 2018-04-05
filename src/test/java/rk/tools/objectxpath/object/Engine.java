package rk.tools.objectxpath.object;

import java.util.List;

public class Engine {
    private double volume;
    public List<Characteristic> characteristics;
    public Gear valve;

    public double getVolume() {
        return volume;
    }

    public void setVolume(double volume) {
        this.volume = volume;
    }
}
