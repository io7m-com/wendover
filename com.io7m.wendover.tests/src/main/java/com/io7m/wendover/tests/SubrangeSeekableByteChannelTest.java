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

import com.io7m.wendover.core.CloseOperationType;
import com.io7m.wendover.core.SubrangeSeekableByteChannel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.NonWritableChannelException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardOpenOption.READ;
import static java.nio.file.StandardOpenOption.WRITE;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class SubrangeSeekableByteChannelTest
{
  private Path directory;
  private Path file;

  @BeforeEach
  public void setup()
    throws IOException
  {
    this.directory =
      WNTestDirectories.createTempDirectory();
    this.file =
      this.directory.resolve("file.bin");

    Files.createFile(this.file);
  }

  @AfterEach
  public void tearDown()
    throws IOException
  {
    WNTestDirectories.deleteDirectory(this.directory);
  }

  /**
   * Reading data into a buffer that only has a small amount of space works.
   *
   * @throws Exception On errors
   */

  @Test
  public void testReadShortTargetBuffer()
    throws Exception
  {
    final var data = new byte[2];
    final var buffer = ByteBuffer.wrap(data);

    try (var fileChannel = FileChannel.open(this.file, WRITE, READ)) {
      fileChannel.write(ByteBuffer.wrap("AAAABBBBCCCCDDDD".getBytes(UTF_8)));

      try (var ch = new SubrangeSeekableByteChannel(fileChannel, 2L, 2L)) {
        assertEquals(0L, ch.position());
        assertEquals(2L, ch.size());
        final var r = ch.read(buffer);
        assertEquals(2L, ch.position());
        assertEquals(2, r);
        assertArrayEquals("AA".getBytes(UTF_8), data);
      }
    }
  }

  /**
   * Reading data into a buffer that has a larger amount of space than the
   * channel works.
   *
   * @throws Exception On errors
   */

  @Test
  public void testReadShortSourceChannel()
    throws Exception
  {
    final var data = new byte[8];
    final var buffer = ByteBuffer.wrap(data);

    try (var fileChannel = FileChannel.open(this.file, WRITE, READ)) {
      fileChannel.write(ByteBuffer.wrap("AAAABBBBCCCCDDDD".getBytes(UTF_8)));

      try (var ch = new SubrangeSeekableByteChannel(fileChannel, 2L, 2L)) {
        assertEquals(0L, ch.position());
        assertEquals(2L, ch.size());
        final var r = ch.read(buffer);
        assertEquals(2L, ch.position());
        assertEquals(2, r);
        assertArrayEquals("AA\0\0\0\0\0\0".getBytes(UTF_8), data);
      }
    }
  }

  /**
   * Reading data from a specific position works.
   *
   * @throws Exception On errors
   */

  @Test
  public void testReadPositioned()
    throws Exception
  {
    final var data = new byte[8];
    final var buffer = ByteBuffer.wrap(data);

    try (var fileChannel = FileChannel.open(this.file, WRITE, READ)) {
      fileChannel.write(ByteBuffer.wrap("AAAABBBBCCCCDDDD".getBytes(UTF_8)));

      try (var ch = new SubrangeSeekableByteChannel(fileChannel, 2L, 14L)) {
        assertEquals(0L, ch.position());
        assertEquals(14L, ch.size());

        ch.position(4L);
        final var r = ch.read(buffer);
        assertEquals(12L, ch.position());
        assertEquals(8, r);
        assertArrayEquals("BBCCCCDD".getBytes(UTF_8), data);
      }
    }
  }

  /**
   * Operations are delegated.
   *
   * @throws Exception On errors
   */

  @Test
  public void testReadOnly()
    throws Exception
  {
    try (var fileChannel = FileChannel.open(this.file, READ)) {
      try (var ch = new SubrangeSeekableByteChannel(fileChannel, 0L, 1000L)) {
        assertThrows(NonWritableChannelException.class, () -> {
          ch.write(ByteBuffer.wrap("ABCD".getBytes(UTF_8)));
        });
      }
    }
  }

  /**
   * Operations are delegated.
   *
   * @throws Exception On errors
   */

  @Test
  public void testClose()
    throws Exception
  {
    final var called = new AtomicInteger(0);
    final CloseOperationType<SubrangeSeekableByteChannel> onClose = (context) -> {
      called.incrementAndGet();
    };

    try (var fileChannel = FileChannel.open(this.file, READ)) {
      try (var ch = new SubrangeSeekableByteChannel(
        fileChannel,
        0L,
        1000L,
        onClose)) {
        assertTrue(ch.isOpen());
        ch.close();
        assertFalse(ch.isOpen());
      }
      assertFalse(fileChannel.isOpen());
    }

    assertEquals(1, called.get());
  }


  /**
   * Truncation isn't supported.
   *
   * @throws Exception On errors
   */

  @Test
  public void testTruncate()
    throws Exception
  {
    try (var fileChannel = FileChannel.open(this.file, READ)) {
      try (var ch = new SubrangeSeekableByteChannel(fileChannel, 0L, 1000L)) {
        assertThrows(UnsupportedOperationException.class, () -> {
          ch.truncate(0L);
        });
      }
    }
  }

  /**
   * Reading data at EOF returns -1.
   *
   * @throws Exception On errors
   */

  @Test
  public void testReadRemainingEOF()
    throws Exception
  {
    final var data = new byte[2];
    final var buffer = ByteBuffer.wrap(data);

    try (var fileChannel = FileChannel.open(this.file, WRITE, READ)) {
      fileChannel.write(ByteBuffer.wrap("AAAABBBBCCCCDDDD".getBytes(UTF_8)));

      try (var ch = new SubrangeSeekableByteChannel(fileChannel, 2L, 2L)) {
        assertEquals(0L, ch.position());
        assertEquals(2L, ch.size());

        var r = ch.read(buffer);
        assertEquals(2, r);
        assertEquals(2L, ch.position());
        assertArrayEquals("AA".getBytes(UTF_8), data);

        r = ch.read(buffer);
        assertEquals(-1, r);
        assertEquals(2L, ch.position());
      }
    }
  }
}
