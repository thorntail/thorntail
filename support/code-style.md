# WildFly Swarm Code Style

Currently we have code styles that can be imported for the following IDEs:

  * [Intellij](ide-configs/idea/README.md)

The above styles are an embodiment, hopefully, of the following rules:

  * Spacing/Tabs
    * Java - 4 spaces
    * XML, and other file types - 2 spaces
    * No tab characters
  * Java Imports
    * No '*' imports unless it's for static members

Here are some code examples of what the settings mean:

```java
@Annotation(param1 = "value1", param2 = "value2")
@SuppressWarnings({"ALL"})
public class Foo<T extends Bar & Abba, U> {
    int[] X = new int[]{1, 3, 5, 6, 7, 87, 1213, 2};

    int[] empty = new int[]{};

    public void foo(int x, int y) {
        Runnable r = () -> {
        };
        Runnable r1 = this::bar;
        for (int i = 0; i < x; i++) {
            y += (y ^ 0x123) << 2;
        }
        do {
            try (MyResource r1 = getResource(); MyResource r2 = null) {
                if (0 < x && x < 10) {
                    while (x != y) {
                        x = f(x * 3 + 5);
                    }
                } else {
                    synchronized (this) {
                        switch (e.getCode()) {
                            //...
                        }
                    }
                }
            } catch (MyException e) {
            } finally {
                int[] arr = (int[]) g(y);
                x = y >= 0 ? arr[y] : -1;
                Map<String, String> sMap = new HashMap<String, String>();
                Bar.<String, Integer>mess(null);
            }
        }
        while (true);
    }

    void bar() {
        {
            return;
        }
    }
}

class Bar {
    static <U, T> U mess(T t) {
        return null;
    }
}

interface Abba {
}
```

## Checkstyle Rules

These are the checkstyle rules that are currently being used

### [FileTabCharacter](http://checkstyle.sourceforge.net/config_whitespace.html#FileTabCharacter)

Checks that there are no tab characters. 'eachLine' is set to true so that every tab character found is reported, not just
the first one.

### [RegexpSingleline](http://checkstyle.sourceforge.net/config_regexp.html#RegexpSingleline)

Sets a format of `\s+$` to ensure there are no trailing spaces on lines.

### [AvoidStarImport](http://checkstyle.sourceforge.net/config_imports.html#AvoidStarImport)

Checks for import statements with the `*` notation. `allowStaticMemberImports` is set to true so that `*` is allowed in
situations such as:

```java
import static org.junit.Assert.*;
```

### [RedundantImport](http://checkstyle.sourceforge.net/config_imports.html#RedundantImport)

Checks any import statements that are not necessary for compilation, due to duplication or it being a `java.lang` class.

### [UnusedImports](http://checkstyle.sourceforge.net/config_imports.html#UnusedImports)

Checks for import statements that are no longer necessary for code compilation.

### [IllegalImport](http://checkstyle.sourceforge.net/config_imports.html#IllegalImport)

Checks for imports of illegal packages, such as `sun.*`. In our case `junit.framework` has been added as `org.junit` is the
newer package naming.

### [ModifierOrder](http://checkstyle.sourceforge.net/config_modifier.html#ModifierOrder)

Checks for modifier order to ensure it conforms with:

  1. public
  2. protected
  3. private
  4. abstract
  5. static
  6. final
  7. transient
  8. volatile
  9. synchronized
  10. native
  11. strictfp

### [RedundantModifier](http://checkstyle.sourceforge.net/config_modifier.html#RedundantModifier)

Checks for modifiers that are not necessary. For instance, fields on interfaces are automatically `public`, `static`, and `final`.

### [LeftCurly](http://checkstyle.sourceforge.net/config_blocks.html#LeftCurly)

Checks that `{` is placed at the end of the line for classes, constructors, interfaces, methods, switch statements and static
initialization blocks.

### [EmptyStatement](http://checkstyle.sourceforge.net/config_coding.html#EmptyStatement)

Looks for `;` without code prior to it.

### [EqualsHashCode](http://checkstyle.sourceforge.net/config_coding.html#EqualsHashCode)

Checks that classes overriding `equals()` also override `hashcode()`.

### [DefaultComesLast](http://checkstyle.sourceforge.net/config_coding.html#DefaultComesLast)

Ensure the `default` is after all `case` statements in a `switch`.

### [IllegalInstantiation](http://checkstyle.sourceforge.net/config_coding.html#IllegalInstantiation)

Checks for instantiations where a factory is preferred, such as `Boolean.TRUE` instead of `new Boolean(true)`.

### [MissingSwitchDefault](http://checkstyle.sourceforge.net/config_coding.html#MissingSwitchDefault)

Checks that a `switch` contains a `default`.

### [UpperEll](http://checkstyle.sourceforge.net/config_misc.html#UpperEll)

Check that constants are defined as `Long` and not `long`.

### [PackageAnnotation](http://checkstyle.sourceforge.net/config_annotation.html#PackageAnnotation)

Ensures all package annotations are in `package-info.java`

### [HideUtilityClassConstructor](http://checkstyle.sourceforge.net/config_design.html#HideUtilityClassConstructor)

Checks all utility classes, such as those with all `static` methods, don't have a public constructor.

### [MissingOverride](http://checkstyle.sourceforge.net/config_annotation.html#MissingOverride)

Ensures `java.lang.Override` is present when `{@inheritDoc}` is present.

### [CovariantEquals](http://checkstyle.sourceforge.net/config_coding.html#CovariantEquals)

Checks that a class which defines a Covariant `equals(MyClass)` method also overrides `equals(Object)` to prevent
unexpected behavior.

### [InnerTypeLast](http://checkstyle.sourceforge.net/config_design.html#InnerTypeLast)

Checks that inner classes and interfaces are specified at the bottom of the class file.

### [EqualsAvoidNull](http://checkstyle.sourceforge.net/config_coding.html#EqualsAvoidNull)

Prefers `"someValue".equals(myVar)` over `myVar.equals("someValue")`

### [ArrayTypeStyle](http://checkstyle.sourceforge.net/config_misc.html#ArrayTypeStyle)

Prefers `main(String[] args)` over `main(String args[])`
