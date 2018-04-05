//package rk.tools.objectxpath;
//
//import org.junit.jupiter.api.Test;
//import rk.tools.objectxpath.object.Characteristic;
//import rk.tools.objectxpath.object.Gear;
//import rk.tools.objectxpath.object.Sedan;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//
//import static rk.tools.objectxpath.Commons.print;
//
//class XMLTest {
//
//    XML xml = new XML();
//    Sedan sedan;
//
//    {
//        sedan = new Sedan();
//        sedan.trunkSize = 10.5;
//        sedan.serialNumber = 1111;
//
//        sedan.countryCodes = new ArrayList<>();
//        sedan.countryCodes.add(55);
//        sedan.countryCodes.add(77);
//
//        Gear gear1 = new Gear();
//        gear1.name = "gear_1";
//        gear1.setId("111");
//        gear1.characteristics = new ArrayList<>();
//        gear1.characteristics.add(new Characteristic("11", "model 1"));
//        gear1.characteristics.add(new Characteristic("22", "size 1"));
//
//        Gear gear2 = new Gear();
//        gear2.name = "gear_2";
//        gear2.setId("222");
//        gear2.characteristics = new ArrayList<>();
//
//        List<Gear> gears = new ArrayList<>();
//        gears.add(gear1);
//        gears.add(gear2);
//
//        sedan.details = new HashMap<>();
//        sedan.details.put("d1", "details1");
//        sedan.details.put("d2", "details2");
//
//        sedan.setGears(gears);
//    }
//
//    @Test
//    void toXml() {
//        String result = xml.toXml("car", sedan);
//        print(result);
//    }
//}