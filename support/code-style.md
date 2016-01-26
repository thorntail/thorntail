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
