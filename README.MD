# Java Object XPath
A library that allows to access object fields using XPath queries.

### Examples
```
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

final Vehicle vehicle = new Vehicle();
...

final String xpath = "/gears[1]/characteristics/characteristics[@key='some-key'];
final Object characteristic = ObjectXpath.process(xpath, vehicle); 
```

For more examples please see [ObjectXpathTest](https://github.com/rmkol/java-object-xpath/blob/master/src/test/java/rk/tools/objectxpath/ObjectXpathTest.java) test class.

### Building
This project depends on [Java Reflection Utils](https://github.com/rmkol/java-reflection-utils) project.
Make sure it's built and installed in your local maven repository.
Then build the project just as a regular Maven project: ```mvn package```.