/** A thunked computation from values of type Arg1 to values of type Ret */
public interface Fn<Arg, Ret> {
  public static <Arg, Ret>
    Thunk<Ret> apply(Thunk<Fn<Arg, Ret>> f,
                      Thunk<Arg> x)
  {
    return Thunk.lazy(__ -> f.eval().call(x).eval());
  }

  /** convenience helper for applying a curried function to 2 arguments */
  public static <Arg1, Arg2, Ret>
    Thunk<Ret> apply2(Thunk<Fn<Arg1, Fn<Arg2, Ret>>> f,
                      Thunk<Arg1> a,
                      Thunk<Arg2> b)
  {
    return apply(apply(f, a), b);
  }

  /** convenience helper for applying a curried function to 3 arguments */
  public static <Arg1, Arg2, Arg3, Ret>
    Thunk<Ret> apply3(Thunk<Fn<Arg1, Fn<Arg2, Fn<Arg3, Ret>>>> f,
                      Thunk<Arg1> a,
                      Thunk<Arg2> b,
                      Thunk<Arg3> c)
  {
    return apply(apply(apply(f, a), b), c);
  }

  /** convenience helper for applying a curried function to 3 arguments */
  public static <Arg1, Arg2, Arg3, Arg4, Ret>
    Thunk<Ret> apply3(Thunk<Fn<Arg1, Fn<Arg2, Fn<Arg3, Fn<Arg4, Ret>>>>> f,
                      Thunk<Arg1> a,
                      Thunk<Arg2> b,
                      Thunk<Arg3> c,
                      Thunk<Arg4> d)
  {
    return apply(apply(apply(apply(f, a), b), c), d);
  }

  // this really ought to be private, but interface members are forcibly public,
  // but we can't use a class here because functional interfaces only work
  // with, well, interfaces, and we really need the lambda syntax support for
  // readability and type inference.
  Thunk<Ret> call(final Thunk<Arg> arg);
}
