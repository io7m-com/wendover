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


package com.io7m.wendover.core.internal;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ReadOnlyBufferException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.NonWritableChannelException;
import java.nio.channels.SeekableByteChannel;
import java.util.Objects;

/**
 * A seekable byte channel based on a byte buffer.
 */

public final class ByteBufferChannel implements SeekableByteChannel
{
  private final ByteBuffer buffer;
  private long position;
  private long limit;
  private final long limitInitial;
  private boolean closed;

  /**
   * A seekable byte channel based on a byte buffer.
   *
   * @param inBuffer The buffer to read/write
   */

  public ByteBufferChannel(
    final ByteBuffer inBuffer)
  {
    this.buffer = Objects.requireNonNull(inBuffer, "buffer");
    this.position = 0L;
    this.limitInitial = Integer.toUnsignedLong(inBuffer.capacity());
    this.limit = this.limitInitial;
    this.closed = false;
  }

  @Override
  public int read(
    final ByteBuffer dst)
    throws IOException
  {
    this.checkNotClosed();

    this.buffer.position(Math.toIntExact(this.position));
    final var w = Math.min(this.buffer.remaining(), dst.remaining());

    try {
      this.buffer.limit(Math.toIntExact(this.position + w));
      dst.put(this.buffer);
    } finally {
      this.buffer.limit(this.buffer.capacity());
    }

    this.position(this.position + w);
    return w;
  }

  private void checkNotClosed()
    throws ClosedChannelException
  {
    if (!this.isOpen()) {
      throw new ClosedChannelException();
    }
  }

  @Override
  public int write(
    final ByteBuffer src)
    throws IOException
  {
    this.checkNotClosed();

    this.buffer.position(Math.toIntExact(this.position));
    final var w = Math.min(this.buffer.remaining(), src.remaining());

    final var oldLimit = src.limit();
    try {
      src.limit(w);
      this.buffer.put(src);
    } catch (final ReadOnlyBufferException e) {
      throw new NonWritableChannelException();
    } finally {
      src.limit(oldLimit);
    }

    this.position(this.position + w);
    return w;
  }

  @Override
  public long position()
  {
    return this.position;
  }

  @Override
  public SeekableByteChannel position(
    final long newPosition)
  {
    if (Long.compareUnsigned(newPosition, this.limit) > 0) {
      this.position = this.limit;
    } else {
      this.position = newPosition;
    }
    return this;
  }

  @Override
  public long size()
  {
    return this.limit - this.position;
  }

  @Override
  public SeekableByteChannel truncate(
    final long size)
    throws IOException
  {
    this.checkNotClosed();

    if (Long.compareUnsigned(size, this.limitInitial) > 0) {
      throw new IllegalArgumentException(
        "Cannot truncate a channel of size %s to a larger size %s"
          .formatted(
            Long.toUnsignedString(this.limitInitial),
            Long.toUnsignedString(size))
      );
    }

    this.limit = size;
    this.position(this.position);
    return this;
  }

  @Override
  public boolean isOpen()
  {
    return !this.closed;
  }

  @Override
  public void close()
  {
    this.closed = true;
  }
}
