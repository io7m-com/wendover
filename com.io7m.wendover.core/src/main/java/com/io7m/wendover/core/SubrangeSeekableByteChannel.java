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

package com.io7m.wendover.core;

import com.io7m.wendover.core.internal.AbstractLockingChannel;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.util.Objects;

import static com.io7m.wendover.core.internal.Unsigned.minUnsigned;
import static java.lang.Integer.toUnsignedLong;

/**
 * A seekable byte channel that can address a subset of a delegate channel.
 */

public final class SubrangeSeekableByteChannel
  extends AbstractLockingChannel
{
  private final CloseOperationType<SubrangeSeekableByteChannel> onClose;
  private final SeekableByteChannel delegate;
  private final long baseStart;
  private final long relativeLimit;
  private long relativePosition;

  /**
   * A seekable byte channel that can address a subset of a delegate channel.
   *
   * @param inDelegate The delegate channel
   * @param inBase     The base offset
   * @param inLimit    The number of bytes that can be addressed
   * @param inOnClose  A function executed when the channel is closed
   */

  public SubrangeSeekableByteChannel(
    final SeekableByteChannel inDelegate,
    final long inBase,
    final long inLimit,
    final CloseOperationType<SubrangeSeekableByteChannel> inOnClose)
  {
    this.delegate =
      Objects.requireNonNull(inDelegate, "delegate");
    this.onClose =
      Objects.requireNonNull(inOnClose, "inOnClose");

    this.relativePosition = 0L;
    this.relativeLimit = inLimit;
    this.baseStart = inBase;
  }

  /**
   * A seekable byte channel that can address a subset of a delegate channel.
   *
   * @param inDelegate The delegate channel
   * @param inBase     The base offset
   * @param inLimit    The number of bytes that can be addressed
   */

  public SubrangeSeekableByteChannel(
    final SeekableByteChannel inDelegate,
    final long inBase,
    final long inLimit)
  {
    this(inDelegate, inBase, inLimit, context -> {

    });
  }

  @Override
  public int read(
    final ByteBuffer dst)
    throws IOException
  {
    this.checkIsOpen();

    return this.<Integer>withStateModificationLock(() -> {

      /*
       * The largest amount of data that can be read is either the space
       * in the buffer, or the remaining space in this limited channel; whichever
       * is smaller.
       */

      final var dstRemaining =
        toUnsignedLong(dst.remaining());
      final var srcRemaining =
        this.remaining();
      final var toRead =
        minUnsigned(dstRemaining, srcRemaining);

      /*
       * Temporarily set the limit on the destination buffer so that the
       * underlying channel doesn't read too much data.
       */

      final var oldLimit = dst.limit();
      try {
        dst.limit(Math.toIntExact(toRead));

        /*
         * Store and restore the underlying channel position after reading.
         */

        final var oldPosition = this.delegate.position();
        try {
          this.delegate.position(this.baseStart + this.relativePosition);
          return Integer.valueOf(this.delegate.read(dst));
        } finally {
          this.delegate.position(oldPosition);
        }
      } finally {
        dst.limit(oldLimit);
      }
    }).intValue();
  }

  private long remaining()
  {
    return this.relativeLimit - this.relativePosition;
  }

  @Override
  public int write(
    final ByteBuffer src)
    throws IOException
  {
    this.checkIsOpen();

    return this.<Integer>withStateModificationLock(() -> {

      /*
       * The largest amount of data that can be written is either the space
       * in the buffer, or the remaining space in this limited channel; whichever
       * is smaller.
       */

      final var srcRemaining =
        toUnsignedLong(src.remaining());
      final var dstRemaining =
        this.remaining();
      final var toWrite =
        minUnsigned(dstRemaining, srcRemaining);

      /*
       * Temporarily set the limit on the source buffer so that the
       * underlying channel doesn't write too much data.
       */

      final var oldLimit = src.limit();
      try {
        src.limit(Math.toIntExact(toWrite));

        /*
         * Store and restore the underlying channel position after writing.
         */

        final var oldPosition = this.delegate.position();
        try {
          this.delegate.position(this.baseStart + this.relativePosition);
          return Integer.valueOf(this.delegate.write(src));
        } finally {
          this.delegate.position(oldPosition);
        }
      } finally {
        src.limit(oldLimit);
      }
    }).intValue();
  }

  @Override
  public long position()
  {
    return this.<Long>withStateReadingLock(
        () -> Long.valueOf(this.relativePosition))
      .longValue();
  }

  @Override
  public SeekableByteChannel position(
    final long newPosition)
    throws IOException
  {
    return this.withStateModificationLock(() -> {
      this.relativePosition = minUnsigned(newPosition, this.relativeLimit);
      return this;
    });
  }

  @Override
  public long size()
  {
    return this.<Long>withStateReadingLock(
        () -> Long.valueOf(this.relativeLimit))
      .longValue();
  }

  @Override
  public SeekableByteChannel truncate(
    final long size)
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isOpen()
  {
    return this.delegate.isOpen();
  }

  @Override
  public void close()
    throws IOException
  {
    if (this.closedAtomic().compareAndSet(false, true)) {
      try {
        this.onClose.execute(this);
      } finally {
        this.delegate.close();
      }
    }
  }
}
