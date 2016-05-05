Camel CDI Example
-----------------

This example demonstrates using the camel-cdi component with JBoss Fuse on EAP to integrate CDI beans with Camel routes.

In this example, a Camel route takes a message payload from a servlet HTTP GET request and passes it on to a direct endpoint. The payload
is then passed onto a Camel CDI bean invocation to produce a message response which is displayed on the web browser page.

Browse to http://localhost:8080/example-camel-cdi/?name=Kermit.

You should see the message "Hello Kermit" output on the web page.

The Camel route is very simple and looks like this:

```
from("direct:start").bean("helloBean");
```

The `bean` DSL makes camel look for a bean named 'helloBean' in the bean registry. The magic that makes this bean
available to Camel is found in the `SomeBean` class.

```java
@Named("helloBean")
public class SomeBean {

    public String someMethod(String message) {
        return "Hello " + message;
    }
}
```

By using the `@Named` annotation, camel-cdi will add this bean to the Camel bean registry.
