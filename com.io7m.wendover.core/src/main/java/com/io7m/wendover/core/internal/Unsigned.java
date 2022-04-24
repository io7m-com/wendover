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

/**
 * Functions over unsigned integers.
 */

public final class Unsigned
{
  private Unsigned()
  {

  }

  /**
   * The maximum of {@code x} and {@code y} if both are unsigned.
   *
   * @param x The left value
   * @param y The right value
   *
   * @return The largest value
   */

  public static long maxUnsigned(
    final long x,
    final long y)
  {
    if (Long.compareUnsigned(x, y) > 0) {
      return x;
    }
    return y;
  }

  /**
   * The minimum of {@code x} and {@code y} if both are unsigned.
   *
   * @param x The left value
   * @param y The right value
   *
   * @return The smallest value
   */

  public static long minUnsigned(
    final long x,
    final long y)
  {
    if (Long.compareUnsigned(x, y) < 0) {
      return x;
    }
    return y;
  }
}
