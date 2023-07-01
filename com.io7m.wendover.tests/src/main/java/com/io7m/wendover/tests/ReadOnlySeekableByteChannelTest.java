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

import com.io7m.wendover.core.ReadOnlySeekableByteChannel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.internal.verification.Times;

import java.nio.ByteBuffer;
import java.nio.channels.NonWritableChannelException;
import java.nio.channels.SeekableByteChannel;

import static org.junit.jupiter.api.Assertions.assertThrows;

public final class ReadOnlySeekableByteChannelTest
{
  private SeekableByteChannel delegate;

  @BeforeEach
  public void setup()
  {
    this.delegate = Mockito.mock(SeekableByteChannel.class);
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
    final var channel = new ReadOnlySeekableByteChannel(this.delegate);
    channel.read(Mockito.mock(ByteBuffer.class));

    Mockito.verify(this.delegate, new Times(1))
      .read(Mockito.any());
  }

  /**
   * Writing is denied.
   *
   * @throws Exception On errors
   */

  @Test
  public void testWrite()
    throws Exception
  {
    final var channel = new ReadOnlySeekableByteChannel(this.delegate);

    assertThrows(NonWritableChannelException.class, () -> {
      channel.write(Mockito.mock(ByteBuffer.class));
    });

    Mockito.verify(this.delegate, new Times(0))
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
    final var channel = new ReadOnlySeekableByteChannel(this.delegate);
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
    final var channel = new ReadOnlySeekableByteChannel(this.delegate);
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
    final var channel = new ReadOnlySeekableByteChannel(this.delegate);
    channel.size();

    Mockito.verify(this.delegate, new Times(1))
      .size();
  }

  /**
   * Writes are denied.
   *
   * @throws Exception On errors
   */

  @Test
  public void testTruncate()
    throws Exception
  {
    final var channel = new ReadOnlySeekableByteChannel(this.delegate);

    assertThrows(NonWritableChannelException.class, () -> {
      channel.truncate(23L);
    });

    Mockito.verify(this.delegate, new Times(0))
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
    final var channel = new ReadOnlySeekableByteChannel(this.delegate);
    channel.close();

    Mockito.verify(this.delegate, new Times(1))
      .close();
  }
}
