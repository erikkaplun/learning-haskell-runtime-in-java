package prelude;

public final class Str {
  public static Thunk<String> append(Thunk<String> s1,
                                     Thunk<String> s2)
  {
    return Thunk.lazy(__ -> s1.eval() + s2.eval());
  }

  public static Thunk<String> append(Thunk<String> s1,
                                     Thunk<String> s2,
                                     Thunk<String> s3)
  {
    return Thunk.lazy(__ -> s1.eval() + s2.eval() + s3.eval());
  }

  /** A naive "port" of the Haskell Show type class */
  public static <A> Thunk<String> show(Thunk<A> x) {
    return Thunk.lazy(__ -> x.eval().toString());
  }
}
