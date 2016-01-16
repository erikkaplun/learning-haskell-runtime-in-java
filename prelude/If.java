package prelude;

public final class If {
  /** if_(foo, bar, baz) == bar   iff foo evaluates to true
   *  if_(foo, bar, baz) == bar   iff foo evaluates to false
   *
   * The semantics is exactly like the C/Java style ternary if-expression,
   * that is, `baz` will not be computed if `bar` is evaluated, and vice versa.
   */
  public static <A> Thunk<A> if_
    (Thunk<Boolean> cond,
     Thunk<A> v1,
     Thunk<A> v2)
  {
    return Thunk.lazy(__ -> (cond.eval() ? v1 : v2).eval());
  }
}
