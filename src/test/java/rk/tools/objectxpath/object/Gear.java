package rk.tools.objectxpath.object;

import java.util.List;

public class Gear {
    private String id;
    public String name;
    public List<Characteristic> characteristics;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
