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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;

import static com.io7m.wendover.core.internal.Unsigned.maxUnsigned;

/**
 * A seekable byte channel that tracks the uppermost limit touched by the
 * channel.
 */

public final class UpperRangeTrackingSeekableByteChannel
  extends DelegatingSeekableByteChannel
{
  private volatile long uppermost;

  /**
   * A seekable byte channel that tracks the uppermost limit touched by the
   * channel.
   *
   * @param inDelegate The delegate channel
   */

  public UpperRangeTrackingSeekableByteChannel(
    final SeekableByteChannel inDelegate)
  {
    super(inDelegate);
    this.uppermost = 0L;
  }

  @Override
  public int write(
    final ByteBuffer src)
    throws IOException
  {
    final var wrote = super.write(src);
    this.uppermost = maxUnsigned(this.position(), this.uppermost);
    return wrote;
  }

  @Override
  public SeekableByteChannel position(
    final long newPosition)
    throws IOException
  {
    this.uppermost = maxUnsigned(newPosition, this.uppermost);
    return super.position(newPosition);
  }

  @Override
  public SeekableByteChannel truncate(
    final long size)
    throws IOException
  {
    this.uppermost = maxUnsigned(size, this.uppermost);
    return super.truncate(size);
  }

  /**
   * @return The uppermost position that has ever been written by this channel
   */

  public long uppermostWritten()
  {
    return this.uppermost;
  }
}
