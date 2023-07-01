/*
 * Copyright © 2022 Mark Raynsford <code@io7m.com> https://www.io7m.com
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
 * SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR
 * IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package com.io7m.wendover.tests;

import com.io7m.wendover.core.ByteBufferChannels;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.FileChannel;
import java.nio.channels.NonWritableChannelException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

import static java.nio.channels.FileChannel.MapMode.READ_WRITE;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardOpenOption.READ;
import static java.nio.file.StandardOpenOption.WRITE;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class ByteBufferChannelsTest
{
  private Path directory;

  @BeforeEach
  public void setup()
    throws IOException
  {
    this.directory = WNTestDirectories.createTempDirectory();
  }

  @AfterEach
  public void tearDown()
    throws IOException
  {
    WNTestDirectories.deleteDirectory(this.directory);
  }

  /**
   * Empty channels are empty.
   *
   * @throws Exception On errors.
   */

  @Test
  public void testEmpty()
    throws Exception
  {
    final var buffer = ByteBuffer.allocate(0);
    try (var channel = ByteBufferChannels.ofByteBuffer(buffer)) {
      assertEquals(0L, channel.size());
    }
  }

  /**
   * Reading/writing closed channels fails.
   *
   * @throws Exception On errors.
   */

  @Test
  public void testClosedIO()
    throws Exception
  {
    final var buffer =
      ByteBuffer.allocate(128);
    final var data =
      ByteBuffer.allocate(128);

    final var channel =
      ByteBufferChannels.ofByteBuffer(buffer);

    assertTrue(channel.isOpen());
    channel.close();
    assertFalse(channel.isOpen());

    assertThrows(ClosedChannelException.class, () -> channel.read(data));
    assertThrows(ClosedChannelException.class, () -> channel.write(data));
    assertThrows(ClosedChannelException.class, () -> channel.truncate(24L));
  }

  /**
   * Trying to truncate channels to a size that's too large... Fails.
   *
   * @throws Exception On errors.
   */

  @Test
  public void testTruncateLarge()
    throws Exception
  {
    final var buffer = ByteBuffer.allocate(128);
    try (var channel = ByteBufferChannels.ofByteBuffer(buffer)) {
      assertEquals(128L, channel.size());
      assertThrows(
        IllegalArgumentException.class,
        () -> channel.truncate(129L));
      assertEquals(128L, channel.size());
    }
  }

  /**
   * Truncating channels works.
   *
   * @throws Exception On errors.
   */

  @Test
  public void testTruncateOK()
    throws Exception
  {
    final var buffer = ByteBuffer.allocate(128);
    try (var channel = ByteBufferChannels.ofByteBuffer(buffer)) {
      assertEquals(0L, channel.position());
      assertEquals(128L, channel.size());

      channel.position(64L);
      assertEquals(64L, channel.position());
      assertEquals(64L, channel.size());

      channel.truncate(32L);
      assertEquals(32L, channel.position());
      assertEquals(0L, channel.size());
    }
  }

  /**
   * Basic writing works.
   *
   * @throws Exception On errors.
   */

  @Test
  public void testWriteBasic()
    throws Exception
  {
    final var backing = new byte[16];
    final var buffer = ByteBuffer.wrap(backing);

    try (var channel = ByteBufferChannels.ofByteBuffer(buffer)) {
      int w;

      assertEquals(0L, channel.position());
      w = channel.write(ByteBuffer.wrap("111111".getBytes(UTF_8)));
      assertEquals(6, w);
      assertEquals(6L, channel.position());
      w = channel.write(ByteBuffer.wrap("222222".getBytes(UTF_8)));
      assertEquals(6, w);
      assertEquals(12L, channel.position());
      w = channel.write(ByteBuffer.wrap("333333".getBytes(UTF_8)));
      assertEquals(4, w);
      assertEquals(16L, channel.position());
      assertArrayEquals("1111112222223333".getBytes(UTF_8), backing);
      assertEquals(0L, channel.size());

      channel.position(0L);
      assertEquals(0L, channel.position());
      w = channel.write(ByteBuffer.wrap("444444".getBytes(UTF_8)));
      assertEquals(6, w);
      assertEquals(6L, channel.position());
      w = channel.write(ByteBuffer.wrap("555555".getBytes(UTF_8)));
      assertEquals(6, w);
      assertEquals(12L, channel.position());
      assertArrayEquals("4444445555553333".getBytes(UTF_8), backing);
      assertEquals(4L, channel.size());

      w = channel.write(ByteBuffer.wrap("2222".getBytes(UTF_8)));
      assertEquals(4, w);
      w = channel.write(ByteBuffer.wrap("1".getBytes(UTF_8)));
      assertEquals(-1, w);
    }
  }

  /**
   * Basic reading works.
   *
   * @throws Exception On errors.
   */

  @Test
  public void testReadBasic()
    throws Exception
  {
    final var backing = "111111222222333333".getBytes(UTF_8);
    final var buffer = ByteBuffer.wrap(backing);

    try (var channel = ByteBufferChannels.ofByteBuffer(buffer)) {
      assertEquals(0L, channel.position());
      assertEquals(backing.length, channel.size());

      {
        final var bb = new byte[4];
        final var b = ByteBuffer.wrap(bb);
        final var r = channel.read(b);
        assertEquals(4, r);
        assertArrayEquals("1111".getBytes(UTF_8), bb);
        assertEquals(4L, channel.position());
      }

      {
        final var bb = new byte[4];
        final var b = ByteBuffer.wrap(bb);
        final var r = channel.read(b);
        assertEquals(4, r);
        assertArrayEquals("1122".getBytes(UTF_8), bb);
        assertEquals(8L, channel.position());
      }

      {
        final var bb = new byte[4];
        final var b = ByteBuffer.wrap(bb);
        final var r = channel.read(b);
        assertEquals(4, r);
        assertArrayEquals("2222".getBytes(UTF_8), bb);
        assertEquals(12L, channel.position());
      }

      {
        final var bb = new byte[4];
        final var b = ByteBuffer.wrap(bb);
        final var r = channel.read(b);
        assertEquals(4, r);
        assertArrayEquals("3333".getBytes(UTF_8), bb);
        assertEquals(16L, channel.position());
      }

      {
        final var bb = new byte[4];
        final var b = ByteBuffer.wrap(bb);
        final var r = channel.read(b);
        assertEquals(2, r);
        assertArrayEquals("33\0\0".getBytes(UTF_8), bb);
        assertEquals(18L, channel.position());
      }
    }
  }

  /**
   * Basic writing works for slices.
   *
   * @throws Exception On errors.
   */

  @Test
  public void testWriteSlice()
    throws Exception
  {
    final var backing = new byte[16];
    final var buffer = ByteBuffer.wrap(backing);
    final var slice = buffer.slice(4, 7);

    try (var channel = ByteBufferChannels.ofByteBuffer(slice)) {
      assertEquals(0L, channel.position());
      assertEquals(7L, channel.size());
      channel.write(ByteBuffer.wrap("ABCDEFG".getBytes(UTF_8)));
      assertEquals(7L, channel.position());
      assertEquals(0L, channel.size());

      assertArrayEquals(
        "\0\0\0\0ABCDEFG\0\0\0\0\0".getBytes(UTF_8),
        backing
      );
    }
  }

  /**
   * Writing to a read-only buffer fails.
   *
   * @throws Exception On errors.
   */

  @Test
  public void testWriteReadOnly()
    throws Exception
  {
    final var backing = new byte[16];
    final var buffer =
      ByteBuffer.wrap(backing)
        .asReadOnlyBuffer();

    try (var channel = ByteBufferChannels.ofByteBuffer(buffer)) {
      assertThrows(NonWritableChannelException.class, () -> {
        channel.write(ByteBuffer.wrap("ABCD".getBytes(UTF_8)));
      });
    }
  }

  /**
   * Mapped buffer I/O works.
   *
   * @throws Exception On errors.
   */

  @Test
  public void testMappedIO()
    throws Exception
  {
    final var file =
      WNTestDirectories.resourceOf(
        ByteBufferChannelsTest.class,
        this.directory,
        "example.txt"
      );

    try (var fileChannel = FileChannel.open(file, READ, WRITE)) {
      final var map =
        fileChannel.map(READ_WRITE, 0L, fileChannel.size());

      try (var channel = ByteBufferChannels.ofByteBuffer(map)) {
        {
          final var bb = new byte[18];
          final var b = ByteBuffer.wrap(bb);
          final var r = channel.read(b);
          assertEquals(18, r);
          assertArrayEquals("On the Main Street".getBytes(UTF_8), bb);
          assertEquals(18L, channel.position());
        }

        channel.position(0L);
        channel.write(ByteBuffer.wrap("ON THE MAIN STREET".getBytes(UTF_8)));
        channel.position(0L);

        {
          final var bb = new byte[51];
          final var b = ByteBuffer.wrap(bb);
          final var r = channel.read(b);
          assertEquals(51, r);
          assertArrayEquals(
            "ON THE MAIN STREET itself are a number of buildings".getBytes(UTF_8),
            bb);
          assertEquals(51L, channel.position());
        }
      }
    }
  }

  /**
   * Byte buffer IO signals EOF correctly.
   *
   * @throws Exception On errors.
   */

  @Test
  public void testMappedIOTerminatesRead()
    throws Exception
  {
    final var file =
      WNTestDirectories.resourceOf(
        ByteBufferChannelsTest.class,
        this.directory,
        "example.txt"
      );

    final var outputBytes =
      new ByteArrayOutputStream();

    try (var fileChannel = FileChannel.open(file, READ, WRITE)) {
      final var map =
        fileChannel.map(READ_WRITE, 0L, fileChannel.size());

      try (var channel = ByteBufferChannels.ofByteBuffer(map)) {
        assertTimeoutPreemptively(Duration.ofSeconds(1L), () -> {
          Channels.newInputStream(channel)
            .transferTo(outputBytes);
        });
      }
    }
  }

  /**
   * Byte buffer IO signals EOF correctly.
   *
   * @throws Exception On errors.
   */

  @Test
  public void testMappedIOTerminatesWrite()
    throws Exception
  {
    final var file =
      WNTestDirectories.resourceOf(
        ByteBufferChannelsTest.class,
        this.directory,
        "example.txt"
      );

    final var sourceBytes = new ByteArrayOutputStream();
    sourceBytes.write(Files.readAllBytes(file));

    final var inputBytes =
      new ByteArrayInputStream(sourceBytes.toByteArray());

    try (var fileChannel = FileChannel.open(file, READ, WRITE)) {
      final var map =
        fileChannel.map(READ_WRITE, 0L, fileChannel.size());

      try (var channel = ByteBufferChannels.ofByteBuffer(map)) {
        assertTimeoutPreemptively(Duration.ofSeconds(1L), () -> {
          inputBytes.transferTo(Channels.newOutputStream(channel));
        });
      }
    }
  }
}
