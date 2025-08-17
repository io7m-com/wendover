wendover
===

[![Maven Central](https://img.shields.io/maven-central/v/com.io7m.wendover/com.io7m.wendover.svg?style=flat-square)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.io7m.wendover%22)
[![Maven Central (snapshot)](https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fcentral.sonatype.com%2Frepository%2Fmaven-snapshots%2Fcom%2Fio7m%2Fwendover%2Fcom.io7m.wendover%2Fmaven-metadata.xml&style=flat-square)](https://central.sonatype.com/repository/maven-snapshots/com/io7m/wendover/)
[![Codecov](https://img.shields.io/codecov/c/github/io7m-com/wendover.svg?style=flat-square)](https://codecov.io/gh/io7m-com/wendover)
![Java Version](https://img.shields.io/badge/17-java?label=java&color=e65cc3)

![com.io7m.wendover](./src/site/resources/wendover.jpg?raw=true)

| JVM | Platform | Status |
|-----|----------|--------|
| OpenJDK (Temurin) Current | Linux | [![Build (OpenJDK (Temurin) Current, Linux)](https://img.shields.io/github/actions/workflow/status/io7m-com/wendover/main.linux.temurin.current.yml)](https://www.github.com/io7m-com/wendover/actions?query=workflow%3Amain.linux.temurin.current)|
| OpenJDK (Temurin) LTS | Linux | [![Build (OpenJDK (Temurin) LTS, Linux)](https://img.shields.io/github/actions/workflow/status/io7m-com/wendover/main.linux.temurin.lts.yml)](https://www.github.com/io7m-com/wendover/actions?query=workflow%3Amain.linux.temurin.lts)|
| OpenJDK (Temurin) Current | Windows | [![Build (OpenJDK (Temurin) Current, Windows)](https://img.shields.io/github/actions/workflow/status/io7m-com/wendover/main.windows.temurin.current.yml)](https://www.github.com/io7m-com/wendover/actions?query=workflow%3Amain.windows.temurin.current)|
| OpenJDK (Temurin) LTS | Windows | [![Build (OpenJDK (Temurin) LTS, Windows)](https://img.shields.io/github/actions/workflow/status/io7m-com/wendover/main.windows.temurin.lts.yml)](https://www.github.com/io7m-com/wendover/actions?query=workflow%3Amain.windows.temurin.lts)|

## wendover

The `wendover` package provides extra classes for working with Java NIO
channels.

## Features

* Numerous `Channel` classes and adapters with various properties.
* High coverage test suite.
* [OSGi-ready](https://www.osgi.org/).
* [JPMS-ready](https://en.wikipedia.org/wiki/Java_Platform_Module_System).
* ISC license.

## Usage

### CloseShieldSeekableByteChannel

Use `CloseShieldSeekableByteChannel` in order to prevent external code from
closing an arbitrary `SeekableByteChannel` instance:

```
SeekableByteChannel c;

someUntrustedObject.run(new CloseShieldSeekableByteChannel(c));
```

The `CloseShieldSeekableByteChannel` instance can be closed, but will not
result in the underlying channel really being closed.

### ReadOnlySeekableByteChannel

Use `ReadOnlySeekableByteChannel` to protect an arbitrary `SeekableByteChannel`
instance against writes:

```
SeekableByteChannel c;

someUntrustedObject.run(new ReadOnlySeekableByteChannel(c));
```

Attempts to write to the `ReadOnlySeekableByteChannel` will result in
`NonWritableChannelException` being thrown.

### SubrangeSeekableByteChannel

Use `SubrangeSeekableByteChannel` to restrict reads and writes to an arbitrary
`SeekableByteChannel` instance to a particular range:

```
SeekableByteChannel c;

someObject.run(new SubrangeSeekableByteChannel(c, 100L, 1000L));
```

Reads of the `SubrangeSeekableByteChannel` instance will be limited to only
reading data that falls within offsets `[100, 100 + 1000 - 1]`. Reading at
position `0` of the `SubrangeSeekableByteChannel` instance will actually
read from position `100` of `c`. Writes are similarly translated and limited.

### UpperRangeTrackingSeekableByteChannel

Use `UpperRangeTrackingSeekableByteChannel` to track the uppermost point
written by an arbitrary `SeekableByteChannel` instance:

```
SeekableByteChannel c;

var d = new UpperRangeTrackingSeekableByteChannel(c);

someObject.run(d);

System.out.println(d.uppermostWritten());
```

### ByteBufferChannels

Use the `ByteBufferChannels` class to adapt a `ByteBuffer` into a
`SeekableByteChannel`:

```
ByteBuffer data;

SeekableByteChannel c = ByteBufferChannels.ofByteBuffer(data);
```

### DelegatingSeekableByteChannel

Use the `DelegatingSeekableByteChannel` class to delegate operations to an
existing channel. This class is used to implement most of the `wendover`
package.

