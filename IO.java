/** Contains actions that, in Haskell, would only be allowed to live inside the IO monad */
public final class IO {
  public static void putStr  (Thunk<String> str) {
    System.out.print  (str.eval());
  }
  public static void putStrLn(Thunk<String> str) {
    System.out.println(str.eval());
  }

  public static <A> void print(Thunk<A> x) {
    putStrLn(Str.show(x));
  }
}
