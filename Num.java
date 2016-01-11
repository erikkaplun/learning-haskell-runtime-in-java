/** arithmetic operations; analogous to the Haskell `Num` type class 
 */
public final class Num {
  /** curried addition of integers */
  public static Thunk<Fn<Integer, Fn<Integer, Integer>>> add() {
    return Thunk.ready(x ->
                       Thunk.ready(y ->
                                   Thunk.lazy(__ -> x.eval() + y.eval())
                                   )
                       );
  }

  /** curried subtraction of integers */
  public static Thunk<Fn<Integer, Fn<Integer, Integer>>> subtract() {
    return Thunk.ready(x ->
                       Thunk.ready(y ->
                                   Thunk.lazy(__ -> x.eval() - y.eval())
                                   )
                       );
  }

  /** curried multiplication of integers */
  public static Thunk<Fn<Integer, Fn<Integer, Integer>>> mul() {
    return Thunk.ready(x ->
                       Thunk.ready(y ->
                                   Thunk.lazy(__ -> x.eval() * y.eval())
                                   )
                       );
  }
}
