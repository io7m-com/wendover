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
import java.nio.channels.NonWritableChannelException;
import java.nio.channels.SeekableByteChannel;
import java.util.Objects;

import static com.io7m.wendover.core.internal.Unsigned.minUnsigned;
import static java.lang.Integer.toUnsignedLong;

/**
 * A seekable byte channel based on a byte buffer.
 */

public final class ByteBufferChannel
  extends AbstractLockingChannel
{
  private final ByteBuffer buffer;
  private final long limitInitial;
  private long position;
  private long limit;

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
    this.limitInitial = toUnsignedLong(inBuffer.capacity());
    this.limit = this.limitInitial;
  }

  @Override
  public int read(
    final ByteBuffer dst)
    throws IOException
  {
    this.checkIsOpen();

    return this.withStateModificationLock(() -> {
      this.buffer.position(Math.toIntExact(this.position));
      final var w =
        minUnsigned(
          toUnsignedLong(this.buffer.remaining()),
          toUnsignedLong(dst.remaining())
        );

      try {
        this.buffer.limit(Math.toIntExact(this.position + w));
        dst.put(this.buffer);
      } finally {
        this.buffer.limit(this.buffer.capacity());
      }

      this.position(this.position + w);
      return Integer.valueOf(Math.toIntExact(w));
    }).intValue();
  }

  @Override
  public int write(
    final ByteBuffer src)
    throws IOException
  {
    this.checkIsOpen();

    return this.withStateModificationLock(() -> {
      this.buffer.position(Math.toIntExact(this.position));
      final var w =
        minUnsigned(
          toUnsignedLong(this.buffer.remaining()),
          toUnsignedLong(src.remaining())
        );

      final var oldLimit = src.limit();
      try {
        src.limit(Math.toIntExact(w));
        this.buffer.put(src);
      } catch (final ReadOnlyBufferException e) {
        throw new NonWritableChannelException();
      } finally {
        src.limit(oldLimit);
      }

      this.position(this.position + w);
      return Integer.valueOf(Math.toIntExact(w));
    }).intValue();
  }

  @Override
  public long position()
  {
    return this.withStateReadingLock(
        () -> Long.valueOf(this.position))
      .longValue();
  }

  @Override
  public SeekableByteChannel position(
    final long newPosition)
    throws IOException
  {
    return this.withStateModificationLock(() -> {
      if (Long.compareUnsigned(newPosition, this.limit) > 0) {
        this.position = this.limit;
      } else {
        this.position = newPosition;
      }
      return this;
    });
  }

  @Override
  public long size()
  {
    return this.withStateReadingLock(
        () -> Long.valueOf(this.limit - this.position))
      .longValue();
  }

  @Override
  public SeekableByteChannel truncate(
    final long size)
    throws IOException
  {
    this.checkIsOpen();

    return this.withStateModificationLock(() -> {
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
    });
  }

  @Override
  public boolean isOpen()
  {
    return !this.closedAtomic().get();
  }

  @Override
  public void close()
  {
    this.closedAtomic().set(true);
  }
}
