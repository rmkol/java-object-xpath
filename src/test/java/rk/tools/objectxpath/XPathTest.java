package rk.tools.objectxpath;

import org.junit.jupiter.api.Test;
import rk.tools.objectxpath.exception.InvalidXPathExpressionError;
import rk.tools.objectxpath.object.Characteristic;
import rk.tools.objectxpath.object.Engine;
import rk.tools.objectxpath.object.Gear;
import rk.tools.objectxpath.object.Sedan;
import rk.tools.xmlprinter.XML;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static rk.tools.objectxpath.Commons.arrayListOf;
import static rk.tools.objectxpath.Commons.print;

class XPathTest {
    XXPath xxPath = new XXPath();
    Sedan sedan;

    {
        sedan = new Sedan();
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
    }

    @Test
    void xml() {
        print(new XML().toXml("sedan", sedan));
    }

    @Test
    void process() {
        Object result;
        List list;

        result = xxPath.process("//engine/@volume", sedan);
        assertEquals(2.0D, result);

        result = xxPath.process("/gears[1]/characteristics[2]", sedan);
        assertEquals(sedan.getGears().get(0).characteristics.get(1), result);

        result = xxPath.process("/gears[@id='111']/characteristics", sedan);
        assertNull(result);

        result = xxPath.process("/gears/gear[@id='111']/characteristics", sedan);
        assertEquals(sedan.getGears().get(0).characteristics, result);

        result = xxPath.process("/gears[1]//characteristics[1]/@details", sedan);
        assertEquals(sedan.getGears().get(0).characteristics.get(0).details, result);

        result = xxPath.process("//gears[1]//characteristics[2]/@details", sedan);
        assertNull(result);

        result = xxPath.process("/countryCodes[1]", sedan);
        assertEquals(sedan.countryCodes.get(0), result);

        result = xxPath.process("/countryCodes[2]", sedan);
        assertEquals(sedan.countryCodes.get(1), result);

        result = xxPath.process("/gears[2]//characteristics", sedan);
        assertEquals(sedan.getGears().get(1).characteristics, result);

        result = xxPath.process("/engine", sedan);
        assertEquals(sedan.engine, result);

        result = xxPath.process("/engine[@volume='2.0']", sedan);
        assertEquals(sedan.engine, result);

        result = xxPath.process("/engine/characteristics", sedan);
        assertNull(result);

        list = (List) xxPath.process("/engine//characteristics", sedan);
        assertEquals(3, list.size());
        assertEquals(sedan.engine.gears.get(0).characteristics.get(0), list.get(0));
        assertEquals(sedan.engine.gears.get(0).characteristics.get(1), list.get(1));
        assertEquals(sedan.engine.gears.get(0).characteristics.get(2), list.get(2));

        result = xxPath.process("//valve", sedan);
        assertEquals(sedan.engine.valve, result);

        result = xxPath.process("//valve/@name", sedan);
        assertEquals(sedan.engine.valve.name, result);

        //root
        result = xxPath.process("/", sedan);
        assertEquals(sedan, result);

        //multiple items
        {
            list = (List) xxPath.process("//characteristic[@details='size:1']", sedan);
            assertEquals(2, list.size());
            assertEquals(sedan.getGears().get(0).characteristics.get(1), list.get(0));
            assertEquals(sedan.getGears().get(2).characteristics.get(1), list.get(1));

            list = (List) xxPath.process("/gears//gear", sedan);
            assertEquals(3, list.size());
            assertEquals(sedan.getGears().get(0), list.get(0));
            assertEquals(sedan.getGears().get(1), list.get(1));
            assertEquals(sedan.getGears().get(2), list.get(2));

            list = (List) xxPath.process("//characteristic", sedan);
            assertEquals(7, list.size());
            assertEquals(sedan.getGears().get(0).characteristics.get(0), list.get(0));
            assertEquals(sedan.getGears().get(0).characteristics.get(1), list.get(1));
            assertEquals(sedan.getGears().get(2).characteristics.get(0), list.get(2));
            assertEquals(sedan.getGears().get(2).characteristics.get(1), list.get(3));
            assertEquals(sedan.engine.gears.get(0).characteristics.get(0), list.get(4));
            assertEquals(sedan.engine.gears.get(0).characteristics.get(1), list.get(5));
            assertEquals(sedan.engine.gears.get(0).characteristics.get(2), list.get(6));
        }

        //any node *
        {
            list = (List) xxPath.process("/*", sedan);
            assertEquals(4, list.size());
            assertEquals(sedan.details, list.get(0));
            assertEquals(sedan.getGears(), list.get(1));
            assertEquals(sedan.countryCodes, list.get(2));
            assertEquals(sedan.engine, list.get(3));

            list = (List) xxPath.process("//*", sedan);
            assertEquals(25, list.size());

            result = xxPath.process("//*[@details='sn:22']", sedan);
            assertEquals(sedan.engine.gears.get(0).characteristics.get(1), result);
        }

        // //Characteristic[@details='size:1']/../..
    }

    //todo empty map
    //todo map with complex key
    //todo empty with complex key
    //todo map of map? :O

    void checkInvalidXpath(String xPath) {
        try {
            xxPath.process(xPath, sedan);
        } catch (InvalidXPathExpressionError e) {
            return;
        }
        throw new AssertionError(xPath + " was treated like valid XPath expression");
    }

    @Test
    void processNegative() {
        // /engine[1]
        // /engine//characteristics[1]
        // /engine//characteristics[@name='nn']
        checkInvalidXpath("/engine/");
        checkInvalidXpath("///");
        checkInvalidXpath("/[]");
    }

    //    @Test
//    @SuppressWarnings("unchecked")
//    void getNextNode() {
//        {
//            String xpath = "//engine/@volume";
//            Optional<XPathNode> node_ = xPath.findNextXPathNode(xpath);
//            assertTrue(node_.isPresent());
//            XPathNode node = node_.get();
//            assertEquals("engine", node.name);
//            assertEquals(NodeRelationship.DESCENDANT, node.relationship);
//            assertEquals(1, node.startIndex);
//            assertEquals(8, node.endIndex);
//
//            xpath = "/engine/@volume";
//            node_ = xPath.findNextXPathNode(xpath);
//            assertTrue(node_.isPresent());
//            node = node_.get();
//            assertEquals("engine", node.name);
//            assertEquals(NodeRelationship.CHILD, node.relationship);
//            assertEquals(0, node.startIndex);
//            assertEquals(7, node.endIndex);
//        }
//
//        //with index
//        {
//            String xpath = "/gears[1]/@name";
//            Optional<XPathNode> node_ = xPath.findNextXPathNode(xpath);
//            assertTrue(node_.isPresent());
//            NodeWithIndex node = (NodeWithIndex) node_.get();
//            assertEquals("gears", node.name);
//            assertEquals(NodeRelationship.CHILD, node.relationship);
//            assertEquals(0, node.startIndex);
//            assertEquals(9, node.endIndex);
//            assertEquals(1, node.index);
//
//            xpath = "//gears[1]/@name";
//            node_ = xPath.findNextXPathNode(xpath);
//            assertTrue(node_.isPresent());
//            node = (NodeWithIndex) node_.get();
//            assertEquals("gears", node.name);
//            assertEquals(NodeRelationship.DESCENDANT, node.relationship);
//            assertEquals(1, node.startIndex);
//            assertEquals(10, node.endIndex);
//            assertEquals(1, node.index);
//        }
//
//        //with attribute
//        {
//            String xpath = "/gears[@name='gear1']";
//            Optional<XPathNode> node_ = xPath.findNextXPathNode(xpath);
//            assertTrue(node_.isPresent());
//            NodeWithAttribute node = (NodeWithAttribute) node_.get();
//            assertEquals("gears", node.name);
//            assertEquals(NodeRelationship.CHILD, node.relationship);
//            assertEquals(0, node.startIndex);
//            assertEquals(21, node.endIndex);
//            assertEquals("name", node.attrName);
//            assertEquals("gear1", node.attrValue);
//
//            xpath = "//gears[@name='gear1']";
//            node_ = xPath.findNextXPathNode(xpath);
//            assertTrue(node_.isPresent());
//            node = (NodeWithAttribute) node_.get();
//            assertEquals("gears", node.name);
//            assertEquals(NodeRelationship.DESCENDANT, node.relationship);
//            assertEquals(1, node.startIndex);
//            assertEquals(22, node.endIndex);
//            assertEquals("name", node.attrName);
//            assertEquals("gear1", node.attrValue);
//        }
//    }

//    @Test
//    void parseXPath() {
//        String xpath = "//engine/@volume";
//        List<XPathNode> nodes = xPath.parseXPath(xpath);
//        assertEquals(2, nodes.size());
//
//        XPathNode first = nodes.get(0);
//        assertEquals("engine", first.name);
//        assertEquals(NodeRelationship.DESCENDANT, first.relationship);
//
//        AttributeNode second = (AttributeNode) nodes.get(1);
//        assertEquals("volume", second.name);
//        assertEquals(NodeRelationship.CHILD, second.relationship);
//    }
}