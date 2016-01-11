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

  public static Thunk<String> show(Object x) {
    return Thunk.lazy(__ -> x.toString());
  }

  public static void print(Thunk<String> str) {
    System.out.print  (str.eval());
  }
  public static void println(Thunk<String> str) {
    System.out.println(str.eval());
  }
}
