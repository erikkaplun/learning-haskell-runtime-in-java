/** arithmetic operations; analogous to the Haskell `Num` type class 
 */
public final class Num {
  /** curried addition of integers */
  public static Thunk<Fn<Integer, Fn<Integer, Integer>>> addI() {
    return Thunk.ready(x ->
                       Thunk.ready(y ->
                                   Thunk.lazy(__ -> x.eval() + y.eval())
                                   )
                       );
  }

  /** curried subtraction of integers */
  public static Thunk<Fn<Integer, Fn<Integer, Integer>>> subtractI() {
    return Thunk.ready(x ->
                       Thunk.ready(y ->
                                   Thunk.lazy(__ -> x.eval() - y.eval())
                                   )
                       );
  }

  /** curried multiplication of integers */
  public static Thunk<Fn<Integer, Fn<Integer, Integer>>> mulI() {
    return Thunk.ready(x ->
                       Thunk.ready(y ->
                                   Thunk.lazy(__ -> x.eval() * y.eval())
                                   )
                       );
  }
}
