package prelude;

import static prelude.Thunk.*;

/** A thunked computation from values of type Arg1 to values of type Ret.
 *
 * Not considering potential optimizations and hacks, this is the only place
 * in the codebase where direct method calls are allowed. Everything else must
 * use this as the gateway to evaluating the thunked graph.
 *
 * Another exception is code that wraps non-Haskell-style code: there, it is
 * allowed to use Java primitives and method calls, as well as invoke Thunk#eval().
 */
public interface Fn<Arg, Ret> {
  public static <Arg, Ret>
  Thunk<Ret>
  apply(Thunk<Fn<Arg, Ret>> f,
        Thunk<Arg> x)
  {
    return thunk(__ -> f.eval().apply(x).eval());
  }

  /** convenience helper for applying a curried function to 2 arguments */
  public static <Arg1, Arg2, Ret>
  Thunk<Ret>
  apply2(Thunk<Fn<Arg1, Fn<Arg2, Ret>>> f,
         Thunk<Arg1> a,
         Thunk<Arg2> b)
  {
    return apply(apply(f, a), b);
  }

  /** convenience helper for applying a curried function to 3 arguments */
  public static <Arg1, Arg2, Arg3, Ret>
  Thunk<Ret>
  apply3(Thunk<Fn<Arg1, Fn<Arg2, Fn<Arg3, Ret>>>> f,
         Thunk<Arg1> a,
         Thunk<Arg2> b,
         Thunk<Arg3> c)
  {
    return apply(apply(apply(f, a), b), c);
  }

  /** convenience helper for applying a curried function to 3 arguments */
  public static <Arg1, Arg2, Arg3, Arg4, Ret>
  Thunk<Ret>
  apply3(Thunk<Fn<Arg1, Fn<Arg2, Fn<Arg3, Fn<Arg4, Ret>>>>> f,
         Thunk<Arg1> a,
         Thunk<Arg2> b,
         Thunk<Arg3> c,
         Thunk<Arg4> d)
  {
    return apply(apply(apply(apply(f, a), b), c), d);
  }

  public static <Arg1, Arg2, Ret>
  Thunk<Ret>
  infix(Thunk<Arg1> a,
        Thunk<Fn<Arg1, Fn<Arg2, Ret>>> op,
        Thunk<Arg2> b)
  {
    return apply2(op, a, b);
  }

  public static <Arg1, Arg2, Ret>
  Thunk<Fn<Arg2, Ret>>
  infixL(Thunk<Arg1> a,
         Thunk<Fn<Arg1, Fn<Arg2, Ret>>> op)
  {
    return apply(op, a);
  }

  public static <Arg1, Arg2, Ret>
  Thunk<Fn<Arg1, Ret>>
  infixR(Thunk<Fn<Arg1, Fn<Arg2, Ret>>> op,
         Thunk<Arg2> b)
  {
    return fn(a -> infix(a, op, b));
  }


  /** Takes a 2-argument function and returnn a new function with the order of
    * these arguments flipped.
    *
    * In Haskell, the type of the function is:
    *
    *     flip :: (a -> b -> c) -> (b -> a -> c)
    */
  public static <Arg1, Arg2, Ret>
  Thunk<  Fn<Fn<Arg1, Fn<Arg2, Ret>>,
             Fn<Arg2, Fn<Arg1, Ret>>>  >
  flip() { return fn(f -> fn(b -> fn(a ->
      apply2(f, a, b)
  ))); }

  // this really ought to be private, but interface members are forcibly public,
  // but we can't use a class here because functional interfaces only work
  // with, well, interfaces, and we really need the lambda syntax support for
  // readability and type inference.
  Thunk<Ret> apply(final Thunk<Arg> arg);
}
