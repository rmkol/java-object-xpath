package rk.tools.objectxpath.object;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static rk.tools.objectxpath.Lists.arrayListOf;

public class Sedan extends Vehicle {

    public double trunkSize;
    public Map<String, String> details;
    public Map<Integer, String> materials;

    public static Sedan createDefault() {
        Sedan sedan = new Sedan();

        sedan.trunkSize = 10.5;
        sedan.serialNumber = 1111;

        {
            sedan.details = new HashMap<>();
            sedan.details.put("d1", "details1");
            sedan.details.put("d2", "details2");
        }

        {
            Gear gear1 = new Gear();
            gear1.name = "gear_1";
            gear1.setId("111");
            gear1.weight = 1.2;
            gear1.characteristics = new ArrayList<>();
            gear1.characteristics.add(new Characteristic("11", "model:1"));
            gear1.characteristics.add(new Characteristic("22", "size:1"));
            gear1.characteristics.add(null);

            Gear gear2 = new Gear();
            gear2.name = "gear_2";
            gear2.setId("112");
            gear2.weight = 1.5;
            gear2.characteristics = new ArrayList<>();

            Gear gear3 = new Gear();
            gear3.name = "gear_3";
            gear3.setId("113");
            gear3.weight = 1.2;
            gear3.characteristics = new ArrayList<>();
            gear3.characteristics.add(new Characteristic("13", "model:12"));
            gear3.characteristics.add(new Characteristic("14", "size:1"));

            List<Gear> gears = new ArrayList<>();
            gears.add(gear1);
            gears.add(gear2);
            gears.add(gear3);

            sedan.setGears(gears);
        }

        {
            sedan.countryCodes = new ArrayList<>();
            sedan.countryCodes.add(55);
            sedan.countryCodes.add(77);

            sedan.engine = new Engine();
            sedan.engine.setVolume(2.0);

            sedan.engine.valve = new Gear();
            sedan.engine.valve.setId("15");
            sedan.engine.valve.name = "valve";

            Gear gear = new Gear();
            gear.setId("311");
            gear.name = "gear_31";
            gear.weight = 11;
            gear.characteristics = new ArrayList<>();
            gear.characteristics.add(new Characteristic("331", "color:red"));
            gear.characteristics.add(new Characteristic("332", "sn:22"));
            gear.characteristics.add(new Characteristic("333", "year:2000"));

            sedan.engine.gears = arrayListOf(gear);
        }
        return sedan;
    }
}
