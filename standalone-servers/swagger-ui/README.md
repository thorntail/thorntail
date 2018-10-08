# Swagger UI Server

The Swagger UI Server provides a simple way to deploy
[Swagger UI](http://swagger.io/swagger-ui/) using Thorntail. The
Swagger UI is, "Swagger UI is a dependency-free collection of HTML,
Javascript, and CSS assets that dynamically generate beautiful
documentation and sandbox from a Swagger-compliant API." If you
are using the `swagger` fraction in your application, the Swagger UI
Server may be useful to you.

## How to Use It

You can run the server using either `java -jar ...` or `mvn wildfly-swarm:run`
depending on your preference and deployment environment.

    $ java -jar swagger-ui-thorntail.jar

The browse to `http://localhost:8080/swagger-ui`. If you don't like the port
number or default context of `swagger-ui` you can change these with runtime
options on the server.

## Runtime Options

The server recognizes three (3) runtime options.

| Option | Default |
| ------ | ------- |
| `thorntail.http.port` | 8080 |
| `thorntail.context.path` | swagger-ui |
| `thorntail.swagger.ui.resources` | `null` |

## Custom HTML, CSS and JavaScript

The final option is a string that points to custom content for the deployed
server. You can use this option to provide `.css` files for styling the UI,
`.js` files for additional functionality, and `.html` files for additional
or modified content. User provided content will override that provided by
`swagger-ui`. For example, if the additional content contains `/index.html`,
this user-provided file will be used instead of the default provided by
`swagger-ui`.

The format of the `thorntail.swagger.ui.resources` string can be in the form of
a path on disk, or a GAV with maven coordinates. For example, if you have a
directory called `resources` alongside your `swagger-ui-thorntail.jar`, and it
contains an `index.html` file, then running

    $ java -jar swagger-ui-thorntail.jar -Dthorntail.swagger.ui.resources=resources

will cause Swagger UI Server to use the provided file from that directory.
If the directory has been compressed as a `jar` or `war` file, you can just
provide the file name.

    $ java -jar swagger-ui-thorntail.jar -Dthorntail.swagger.ui.resources=resources.jar

And if that jar actually exists in a maven repository, then you can just provide
the coordinates.

    $ java -jar swagger-ui-thorntail.jar -Dthorntail.swagger.ui.resources="com.example:resources:war:1.0.0"
