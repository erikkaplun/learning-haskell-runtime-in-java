package prelude;

public final class Eq {
  /** curried equality of integers */
  public static Thunk<Fn<Integer, Fn<Integer, Boolean>>> eqI() {
    return Thunk.ready(x -> Thunk.ready(y -> Thunk.lazy(__ -> x.eval() == y.eval())));
  }

  /** curried equality of doubles */
  public static Thunk<Fn<Double, Fn<Double, Boolean>>> eqD() {
    return Thunk.ready(x -> Thunk.ready(y -> Thunk.lazy(__ -> x.eval() == y.eval())));
  }

  /** curried equality of strings */
  public static Thunk<Fn<String, Fn<String, Boolean>>> eqS() {
    return Thunk.ready(x -> Thunk.ready(y -> Thunk.lazy(__ -> x.eval().equals(y.eval()))));
  }
}
