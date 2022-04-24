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
import java.util.Objects;

/**
 * A seekable byte channel that delegates to an existing channel.
 */

public abstract class DelegatingSeekableByteChannel
  implements SeekableByteChannel
{
  private final SeekableByteChannel delegate;

  /**
   * A seekable byte channel that delegates to an existing channel.
   *
   * @param inDelegate The delegate channel
   */

  public DelegatingSeekableByteChannel(
    final SeekableByteChannel inDelegate)
  {
    this.delegate =
      Objects.requireNonNull(inDelegate, "delegate");
  }

  protected final SeekableByteChannel delegate()
  {
    return this.delegate;
  }

  @Override
  public int read(final ByteBuffer dst)
    throws IOException
  {
    return this.delegate.read(dst);
  }

  @Override
  public int write(final ByteBuffer src)
    throws IOException
  {
    return this.delegate.write(src);
  }

  @Override
  public long position()
    throws IOException
  {
    return this.delegate.position();
  }

  @Override
  public SeekableByteChannel position(
    final long newPosition)
    throws IOException
  {
    return this.delegate.position(newPosition);
  }

  @Override
  public long size()
    throws IOException
  {
    return this.delegate.size();
  }

  @Override
  public SeekableByteChannel truncate(
    final long size)
    throws IOException
  {
    return this.delegate.truncate(size);
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
    this.delegate.close();
  }
}
