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

/**
 * A specification of the desired behaviour for subranged channels.
 */

public enum SubrangeLimitBehavior
{
  /**
   * If the reading position is such that no bytes can be read, return 0
   * from read operations.
   */

  LIMIT_REACHED_RETURN_0,

  /**
   * If the reading position is such that no bytes can be read, return -1 (EOF)
   * from read operations. Note that this is the behaviour expected by various
   * methods in the standard library that convert channels to streams.
   */

  LIMIT_REACHED_RETURN_EOF
}
