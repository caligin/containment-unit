# containment-unit

Run a docker container for each @Test

## How

Enable [jitpack.io](https://jitpack.io/) repository (I will consider maven central once I have an actual release)

```
<repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
</repository>
```

Add dependency

```
<dependency>
    <groupId>com.github.caligin</groupId>
    <artifactId>containment-unit</artifactId>
    <version>e5de0d56c524c51d870f118aebecb92693e40553</version>
</dependency>
```

in your test class:

```
@Rule
private ContainerRule container = new ContainerRule("<name_of_container_here>");
```

## Next

This little thing is MVP. Target is to be able to run a container that makes a service available on a specific port and wait for it to be ready (e.g. run a clean postres for each test).

Long-run target is to build a new image (if not cached) before running all tests and rely on that image for testing (e.g. build a prepopulated postgres image and spin it up for every @Test)
