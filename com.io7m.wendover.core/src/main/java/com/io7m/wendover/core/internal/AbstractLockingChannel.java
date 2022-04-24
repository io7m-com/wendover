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
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SeekableByteChannel;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

/**
 * An abstract channel that protects state reading and writing with locks.
 */

public abstract class AbstractLockingChannel implements SeekableByteChannel
{
  private final ReentrantReadWriteLock lockRW;
  private final ReentrantReadWriteLock.ReadLock lockR;
  private final ReentrantReadWriteLock.WriteLock lockW;
  private final AtomicBoolean closed;

  /**
   * An abstract channel that protects state reading and writing with locks.
   */

  public AbstractLockingChannel()
  {
    this.lockRW = new ReentrantReadWriteLock();
    this.lockR = this.lockRW.readLock();
    this.lockW = this.lockRW.writeLock();
    this.closed = new AtomicBoolean(false);
  }

  /**
   * Check if this channel is open. The channel is considered closed if the
   * closed flag has been set.
   *
   * @throws ClosedChannelException If the channel is closed
   * @see #closedAtomic()
   */

  protected final void checkIsOpen()
    throws ClosedChannelException
  {
    if (this.closed.get()) {
      throw new ClosedChannelException();
    }
  }

  /**
   * @return A boolean flag indicating whether or not the channel is closed
   */

  protected final AtomicBoolean closedAtomic()
  {
    return this.closed;
  }

  /**
   * Obtain a lock used to read the state of this channel.
   *
   * @param <T> The type of returned values
   * @param f   A function executed with locks
   *
   * @return The value returned by {@code f}
   */

  protected final <T> T withStateReadingLock(
    final Supplier<T> f)
  {
    this.lockR.lock();
    try {
      return f.get();
    } finally {
      this.lockR.unlock();
    }
  }

  /**
   * Obtain a lock used to modify the state of this channel.
   *
   * @param <T> The type of returned values
   * @param f   A function executed with locks
   *
   * @return The value returned by {@code f}
   */

  protected final <T> T withStateModificationLock(
    final WithLockType<T> f)
    throws IOException
  {
    this.lockW.lock();
    try {
      return f.execute();
    } finally {
      this.lockW.unlock();
    }
  }

  /**
   * Functions that execute with locks held.
   *
   * @param <T> The type of returned values
   */

  public interface WithLockType<T>
  {
    /**
     * Execute and return a value.
     *
     * @return A value of {@code T}
     *
     * @throws IOException If required
     */

    T execute()
      throws IOException;
  }
}
