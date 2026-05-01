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

import java.nio.channels.SeekableByteChannel;

/**
 * <p>
 * A seekable byte channel that delegates to an existing channel and can't
 * be closed.
 * </p>
 * <p>
 * This differs from {@link CloseShieldSeekableByteChannel} in that close
 * requests are ignored and this channel is only considered closed if
 * its delegate is closed.
 * </p>
 *
 * @since 1.1.0
 */

public final class UncloseableSeekableByteChannel
  extends DelegatingSeekableByteChannel
{
  /**
   * <p>
   * A seekable byte channel that delegates to an existing channel and can't
   * be closed.
   * </p>
   * <p>
   * This differs from {@link CloseShieldSeekableByteChannel} in that close
   * requests are ignored and this channel is only considered closed if
   * its delegate is closed.
   * </p>
   *
   * @param inDelegate The delegate channel
   */

  public UncloseableSeekableByteChannel(
    final SeekableByteChannel inDelegate)
  {
    super(inDelegate);
  }

  @Override
  public boolean isOpen()
  {
    return this.delegate().isOpen();
  }

  @Override
  public void close()
  {
    // Ignored.
  }
}
