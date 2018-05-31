ProtoPoet
=========

`ProtoPoet` is a Java API for generating [Protocol Buffer](https://developers.google.com/protocol-buffers/) (`.proto`) source files. This library was inspired by the fantastic [JavaPoet](https://github.com/square/javapoet) project by Square.

> NOTE: ProtoPoet 1.0 only supports `proto3` syntax. If you have a need for `proto2` syntax support, please file an issue and/or submit a PR for it.

ProtoPoet has a fully documented API, please refer to the JavaDoc reference for more details, or keep reading to see some quick examples of to use this API.

## Defining a Simple Message

Let's say we wanted to declare a simple file with a message in it, like this:

```protobuf
syntax = "proto3";
package helloworld;

// My cool new message.
message MyMessage {
  string some_string = 1;
  repeated int64 some_numbers = 2;
}
```

Using the API, we could generate that output by authoring the following Java code:

```java
ProtoFile.builder()
  .setPackageName("helloworld")
  .addMessages(MessageSpec.builder("MyMessage")
    .setMessageComment("My cool new message.")
    .addMessageFields(MessageFieldSpec.builder(FieldType.STRING, "some_string", 1),
                      MessageFieldSpec.repeated(FieldType.INT64, "some_numbers", 2)));
```

ProtoPoet will also help you catch bugs, ensuring that your field names and numbers are unique within the scope you declare them. *(eg: field names/numbers within a message, message names within a file, etc.)*

## Adding Imports

It is possible for a proto file to depend on another, like so:

```protobuf
syntax = "proto3";

import "some/file.proto";
```

Using ProtoPoet, you would replicate this using the following:

```java
ProtoFile.builder()
  .addImports(ImportSpec.of("some/file.proto"));
```

Note that ProtoPoet does **nothing** with the path string you provide it, just renders it. It is up to you to ensure its accessible at compile time etc.


## Using Oneofs and Maps with Message

[Oneofs](https://developers.google.com/protocol-buffers/docs/proto3#oneof) and [Maps](https://developers.google.com/protocol-buffers/docs/proto3#maps) are useful and expressive concepts to rely on when defining a message. Imagine the following example message:

```protobuf
// Defines an event.
message Event {
  // rsvps by name.
  map<string, bool> rsvps = 1;

  // The kind of event happening.
  oneof occasion {
    BirthdayParty birthday_party = 2;
    Wedding wedding = 3;
    Graduation graduation = 4;
  }
}
```

Using the API, we could generate that output by authoring the following Java code:

```java
MessageSpec.builder("Event")
  .setMessageComment("Defines an event.")
  .addMessageFields(MapFieldSpec.builder(FieldType.STRING, FieldType.BOOL, "rsvps", 1)
                   .setFieldComment("rsvps by name."),
                   OneofFieldSpec.builder("occasion")
                     .setFieldComment("The kind of event happening.")
                     .addMessageFields(MessageFieldSpec.message("BirthdayParty", "birthday_party", 2),
                                       MessageFieldSpec.message("Wedding", "wedding", 3),
                                       MessageFieldSpec.message("Graduation", "graduation", 4)));
```

As before, ProtoPoet continues to monitor for unique names and usages within the right scopes. It also ensures you choose the appropriate key/value types for maps.

## Defining and Respecting Reservations

Sometimes, you'll need to rely on [Reserved Fields](https://developers.google.com/protocol-buffers/docs/proto3#reserved) when defining a message, or in this case an Enum:

```protobuf
// Declares a greeting.
enum Greeting {
  reserved 2, 5, 9 to 11;
  reserved "GDAY", "AHOY";
  HELLO = 0;
  // Techinically, also hello.
  ALOHA = 1;
}
```

If we wanted to use ProtoPoet to define this, we'd do the following:

```java
EnumSpec.builder("Greeting")
  .setEnumComment("Declares a greeting.")
  .addReservations(ReservationSpec.builder(2, 5, 9).addRanges(FieldRange.of(9, 11)),
                   ReservationSpec.builder("GDAY", "AHOY"))
  .addEnumFields(EnumFieldSpec.builder("HELLO", 0),
                 EnumFieldSpec.builder("ALOHA", 1)
                 .setFieldComment("Technically, also hello."));
```

Additionally, if the code we wrote accidentally made use of a reserved field name or number, ProtoPoet will alert you to the culprit and help you debug/fix it. Reservations may also be used with Messages.

## Services and RPCs

These come in handy when working with [gRPC](http://grpc.io), lets say we wanted to write their classic example:

```protobuf
// The greeting service definition.
service Greeter {
  // Sends a greeting
  rpc SayHello (HelloRequest) returns (HelloReply) {}
}
```

With ProtoPoet, we'd do the following:

```java
ServiceSpec.builder("Greeter")
  .setServiceComment("The greeting service definition")
  .addRpcFields(RpcFieldSpec.builder("SayHello")
                .setFieldComment("Sends a greeting")
                .setRequestMessageName("HelloRequest")
                .setResponseMessageName("HelloReply"));
```

## Declaring Options

ProtoPoet has extensive support for [Options](https://developers.google.com/protocol-buffers/docs/proto3#options) across the many builders in provides. Imagine this example:

```protobuf
syntax = "proto3";

option java_package = "com.whatever";

message MyMessage {
  // this has a comment.
  option (my_option) = true;
  repeated string my_field = 1 [deprecated = true, (my_other_option) = "hello"];
}

service Greeter {
  option (another_option) = "hello";
  rpc SayHello (Greeting) returns (Reply) {
    option (my_message_option) = {
      hello: 123
      foobar: "OK"
    };
  }
}
```

OK, this example is a bit complicated, but it showcases (nearly) all the supported syntax variants that an option can be declared as. Lets look at the code:

```java
ProtoFile.builder()
  .addFileOptions(OptionSpec.builder(OptionType.FILE, "java_package")
                  .setValue(FieldType.STRING, "com.whatever"))
  .addMessages(MessageSpec.builder("MyMessage")
               .addMessageOptions(OptionSpec.builder(OptionType.MESSAGE, "another_option")
                                  .setValue(FieldType.STRING, "hello"))
               .addMessageFields(MessageFieldSpec.repeated(FieldType.STRING, "my_field", 1)
                                 .addFieldOptions(OptionSpec.builder(OptionType.FIELD, "deprecated")
                                                  .setValue(FieldType.BOOL, true),
                                                  OptionSpec.builder(OptionType.FIELD, "my_other_option")
                                                  .setValue(FieldType.STRING, "hello"))))
  .addServices(ServiceSpec.builder("Greeter")
    .setServiceOptions(OptionSpec.builder(FieldType.SERVICE, "another_option")
                       .setValue(FieldType.STRING, "hello"))
    .setRpcFields(RpcFieldSpec.builder("sayHello")
                  .setRequestMessageName("Greeting")
                  .setResponseMessageName("Reply")
                  .setFieldOptions(OptionSpec.builder(OptionType.METHOD, "my_message_option")
                                   .setValue(FieldType.MESSAGE,
                                             FieldValue.of("hello", FieldType.INT32, 123),
                                             FieldValue.of("foobar", FieldType.STRING, "OK"))))));
```

This snippet demonstrates the broad support for `OptionSpec` across ProtoPoet. Options are first class API primitives, and can be robustly utilized. Checkout the Javadoc for more specific details.

## Defining Custom Options

Proto3 no longer supports the concept of message extensions, except as a hack to expose a way to define [Custom Options](https://developers.google.com/protocol-buffers/docs/proto#customoptions), like so:

```protobuf
import "google/protobuf/descriptor.proto";

// Some extension for a message type.
extend google.protobuf.MessageOptions {
  string my_option = 51234;
}
```

Because of the limit conceptual support, ProtoPoet provides a locked down API to support this:

```java
ExtensionSpec.builder(OptionType.MESSAGE)
  .setExtensionComment("Some extension for a message type.")
  .addExtensionFields(MessageFieldSpec.builder(FieldType.STRING, "my_option", 51234));
```

Note that when adding an `ExtensionSpec` to a `ProtoFile`, the necessary import from the example will be added and hoisted to the top of the generated output for you.


## Thank You

Thank you for taking an interest in ProtoPoet. Feel free to use it in accordance with the `LICENSE` (Apache2), and submit issues or checkout `CONTRIBUTING` for details on how to collaborate with this project if you're interested.