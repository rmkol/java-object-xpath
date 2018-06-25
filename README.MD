# OXPy (Object XPath)
A Java library that allows to access object fields using XPath queries.

### Examples
```java
class Characteristic {
    String key;
    String value;
}

class Gear {
    String id;
    List<Characteristic> characteristics;
}

class Vehicle {
    List<Gear> gears;
}

Vehicle vehicle = new Vehicle();
...

OXPy oxpy = new OXPy();
String xpath = "/gears[1]/characteristics/characteristic[@key='some-key']";
Optional<Object> characteristic = oxpy.process(xpath, vehicle);
assert characteristic.isPresent();
```

For more examples please see [OXPyTest](https://github.com/rmkol/java-object-xpath/blob/master/src/test/java/rk/tools/objectxpath/OXPyTest.java) test class.

### Building
This project depends on [Java Reflection Utils](https://github.com/rmkol/java-reflection-utils) project.
Make sure it's built and installed in your local maven repository.
Then build the project just as a regular Maven project: ```mvn package```.