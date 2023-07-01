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

import com.io7m.wendover.core.UpperRangeTrackingSeekableByteChannel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.internal.verification.Times;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Path;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.READ;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public final class UpperRangeTrackingSeekableByteChannelTest
{
  private SeekableByteChannel delegate;
  private Path directory;
  private Path file;
  private FileChannel fileChannel;

  @BeforeEach
  public void setup()
    throws IOException
  {
    this.delegate =
      Mockito.mock(SeekableByteChannel.class);

    this.directory =
      WNTestDirectories.createTempDirectory();
    this.file =
      this.directory.resolve("file.bin");
    this.fileChannel =
      FileChannel.open(this.file, CREATE, READ, WRITE, TRUNCATE_EXISTING);
  }

  @AfterEach
  public void tearDown()
    throws IOException
  {
    this.fileChannel.close();
    WNTestDirectories.deleteDirectory(this.directory);
  }

  /**
   * Operations are delegated.
   *
   * @throws Exception On errors
   */

  @Test
  public void testRead()
    throws Exception
  {
    final var channel = new UpperRangeTrackingSeekableByteChannel(this.delegate);
    channel.read(ByteBuffer.allocate(100));

    Mockito.verify(this.delegate, new Times(1))
      .read(Mockito.any());
  }

  /**
   * Operations are delegated.
   *
   * @throws Exception On errors
   */

  @Test
  public void testWrite()
    throws Exception
  {
    final var channel = new UpperRangeTrackingSeekableByteChannel(this.delegate);
    channel.write(ByteBuffer.allocate(100));

    Mockito.verify(this.delegate, new Times(1))
      .write(Mockito.any());
  }

  /**
   * Operations are delegated.
   *
   * @throws Exception On errors
   */

  @Test
  public void testPosition()
    throws Exception
  {
    final var channel = new UpperRangeTrackingSeekableByteChannel(this.delegate);
    channel.position();

    Mockito.verify(this.delegate, new Times(1))
      .position();
  }

  /**
   * Operations are delegated.
   *
   * @throws Exception On errors
   */

  @Test
  public void testPositionSet()
    throws Exception
  {
    final var channel = new UpperRangeTrackingSeekableByteChannel(this.delegate);
    channel.position(23L);

    Mockito.verify(this.delegate, new Times(1))
      .position(23L);
  }

  /**
   * Operations are delegated.
   *
   * @throws Exception On errors
   */

  @Test
  public void testSize()
    throws Exception
  {
    final var channel = new UpperRangeTrackingSeekableByteChannel(this.delegate);
    channel.size();

    Mockito.verify(this.delegate, new Times(1))
      .size();
  }

  /**
   * Operations are delegated.
   *
   * @throws Exception On errors
   */

  @Test
  public void testTruncate()
    throws Exception
  {
    final var channel = new UpperRangeTrackingSeekableByteChannel(this.delegate);
    channel.truncate(23L);

    Mockito.verify(this.delegate, new Times(1))
      .truncate(23L);
  }

  /**
   * Closing doesn't delegate.
   *
   * @throws Exception On errors
   */

  @Test
  public void testClose()
    throws Exception
  {
    final var channel = new UpperRangeTrackingSeekableByteChannel(this.delegate);
    channel.close();
    assertFalse(channel.isOpen());

    Mockito.verify(this.delegate, new Times(1))
      .close();
  }

  /**
   * The written position is correct.
   *
   * @throws Exception On errors
   */

  @Test
  public void testWritePositions()
    throws Exception
  {
    final var data = ByteBuffer.allocate(23);

    final var channel = new UpperRangeTrackingSeekableByteChannel(this.fileChannel);
    channel.write(data);
    assertEquals(23L, channel.uppermostWritten());

    data.flip();
    channel.write(data);
    assertEquals(46L, channel.uppermostWritten());

    channel.position(40L);

    data.flip();
    channel.write(data);
    assertEquals(63L, channel.uppermostWritten());
  }
}
