package rk.tools.objectxpath;

import org.junit.jupiter.api.Test;
import rk.tools.objectxpath.xpath.AttributeNode;
import rk.tools.objectxpath.xpath.XPathNode;
import rk.tools.objectxpath.xpath.NodeWithAttr;
import rk.tools.objectxpath.xpath.NodeWithIndex;
import rk.tools.objectxpath.object.Characteristic;
import rk.tools.objectxpath.object.Engine;
import rk.tools.objectxpath.object.Gear;
import rk.tools.objectxpath.object.Sedan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class XPathTest {
    XPath xPath = new XPath();
    Sedan sedan;

    {
        sedan = new Sedan();
        sedan.trunkSize = 10.5;
        sedan.serialNumber = 1111;

        sedan.countryCodes = new ArrayList<>();
        sedan.countryCodes.add(55);
        sedan.countryCodes.add(77);

        Gear gear1 = new Gear();
        gear1.name = "gear_1";
        gear1.setId("111");
        gear1.characteristics = new ArrayList<>();
        gear1.characteristics.add(new Characteristic("11", "model 1"));
        gear1.characteristics.add(new Characteristic("22", "size 1"));

        Gear gear2 = new Gear();
        gear2.name = "gear_2";
        gear2.setId("222");
        gear2.characteristics = new ArrayList<>();

        List<Gear> gears = new ArrayList<>();
        gears.add(gear1);
        gears.add(gear2);

        sedan.setGears(gears);

        sedan.details = new HashMap<>();
        sedan.details.put("d1", "details1");
        sedan.details.put("d2", "details2");

        sedan.engine = new Engine();
        sedan.engine.setVolume(2.0);
        sedan.engine.valve = new Gear();
        sedan.engine.valve.setId("15");
        sedan.engine.valve.name = "valve";
    }

    @Test
    void xml() {
//        print(new XML().toXml("sedan", sedan));
    }

    @Test
    void process() {
        Object result = xPath.process("//engine/@volume", sedan);
        assertEquals(2.0D, result);

        result = xPath.process("/gears[1]/characteristics[2]", sedan);
        assertEquals(sedan.getGears().get(0).characteristics.get(1), result);

        result = xPath.process("/gears[@id='111']/characteristics", sedan);
        assertEquals(sedan.getGears().get(0).characteristics, result);

        result = xPath.process("/gears[1]//characteristics[1]/@details", sedan);
        assertEquals(sedan.getGears().get(0).characteristics.get(0).details, result);

        result = xPath.process("//gears[1]//characteristics[2]/@details", sedan);
        assertEquals(sedan.getGears().get(0).characteristics.get(1).details, result);

        result = xPath.process("/countryCodes[1]", sedan);
        assertEquals(sedan.countryCodes.get(0), result);

        result = xPath.process("/countryCodes[2]", sedan);
        assertEquals(sedan.countryCodes.get(1), result);

        result = xPath.process("/gears[2]//characteristics", sedan);
        assertEquals(sedan.getGears().get(1).characteristics, result);

        result = xPath.process("/engine", sedan);
        assertEquals(sedan.engine, result);

        result = xPath.process("/engine[@volume='2.0']", sedan);
        assertEquals(sedan.engine, result);

        result = xPath.process("/engine//characteristics", sedan);
        assertNull(result);

        result = xPath.process("//valve", sedan);
        assertEquals(sedan.engine.valve, result);

        result = xPath.process("//valve/@name", sedan);
        assertEquals(sedan.engine.valve.name, result);
    }

    @Test
    void processNegative() {
        // /engine[1]
        // /engine//characteristics[1]
        // /engine//characteristics[@name='nn']
    }

    @Test
    @SuppressWarnings("unchecked")
    void getNextNode() {
        {
            String xpath = "//engine/@volume";
            Optional<XPathNode> node_ = xPath.getNextXPathNode(xpath);
            assertTrue(node_.isPresent());
            XPathNode node = node_.get();
            assertEquals("engine", node.name);
            assertEquals(NodeRelationship.DESCENDANT, node.relationship);
            assertEquals(1, node.startIndex);
            assertEquals(8, node.endIndex);

            xpath = "/engine/@volume";
            node_ = xPath.getNextXPathNode(xpath);
            assertTrue(node_.isPresent());
            node = node_.get();
            assertEquals("engine", node.name);
            assertEquals(NodeRelationship.CHILD, node.relationship);
            assertEquals(0, node.startIndex);
            assertEquals(7, node.endIndex);
        }

        //with index
        {
            String xpath = "/gears[1]/@name";
            Optional<XPathNode> node_ = xPath.getNextXPathNode(xpath);
            assertTrue(node_.isPresent());
            NodeWithIndex node = (NodeWithIndex) node_.get();
            assertEquals("gears", node.name);
            assertEquals(NodeRelationship.CHILD, node.relationship);
            assertEquals(0, node.startIndex);
            assertEquals(9, node.endIndex);
            assertEquals(1, node.index);

            xpath = "//gears[1]/@name";
            node_ = xPath.getNextXPathNode(xpath);
            assertTrue(node_.isPresent());
            node = (NodeWithIndex) node_.get();
            assertEquals("gears", node.name);
            assertEquals(NodeRelationship.DESCENDANT, node.relationship);
            assertEquals(1, node.startIndex);
            assertEquals(10, node.endIndex);
            assertEquals(1, node.index);
        }

        //with attribute
        {
            String xpath = "/gears[@name='gear1']";
            Optional<XPathNode> node_ = xPath.getNextXPathNode(xpath);
            assertTrue(node_.isPresent());
            NodeWithAttr node = (NodeWithAttr) node_.get();
            assertEquals("gears", node.name);
            assertEquals(NodeRelationship.CHILD, node.relationship);
            assertEquals(0, node.startIndex);
            assertEquals(21, node.endIndex);
            assertEquals("name", node.attrName);
            assertEquals("gear1", node.attrValue);

            xpath = "//gears[@name='gear1']";
            node_ = xPath.getNextXPathNode(xpath);
            assertTrue(node_.isPresent());
            node = (NodeWithAttr) node_.get();
            assertEquals("gears", node.name);
            assertEquals(NodeRelationship.DESCENDANT, node.relationship);
            assertEquals(1, node.startIndex);
            assertEquals(22, node.endIndex);
            assertEquals("name", node.attrName);
            assertEquals("gear1", node.attrValue);
        }
    }

    @Test
    void parseXPath() {
        String xpath = "//engine/@volume";
        List<XPathNode> nodes = xPath.parseXPath(xpath);
        assertEquals(2, nodes.size());

        XPathNode first = nodes.get(0);
        assertEquals("engine", first.name);
        assertEquals(NodeRelationship.DESCENDANT, first.relationship);

        AttributeNode second = (AttributeNode) nodes.get(1);
        assertEquals("volume", second.name);
        assertEquals(NodeRelationship.CHILD, second.relationship);
    }
}