/** arithmetic operations; analogous to the Haskell `Num` type class
 */
public final class Num {
  /** curried addition of integers */
  public static
  Thunk<Fn<Integer, Fn<Integer, Integer>>>
  addI() { return Thunk.ready(x -> Thunk.ready(y -> Thunk.lazy(__ ->
    x.eval() + y.eval()
  ))); }

  /** curried subtraction of integers */
  public static
  Thunk<Fn<Integer, Fn<Integer, Integer>>>
  subtractI() { return Thunk.ready(x -> Thunk.ready(y -> Thunk.lazy(__ ->
    x.eval() - y.eval()
  ))); }

  /** curried multiplication of integers */
  public static
  Thunk<Fn<Integer, Fn<Integer, Integer>>>
  mulI() { return Thunk.ready(x -> Thunk.ready(y -> Thunk.lazy(__ ->
    x.eval() * y.eval()
  ))); }

  ///

  /** curried addition of doubles */
  public static
  Thunk<Fn<Double, Fn<Double, Double>>>
  addD() { return Thunk.ready(x -> Thunk.ready(y -> Thunk.lazy(__ ->
    x.eval() + y.eval()
  ))); }

  /** curried subtraction of doubles */
  public static
  Thunk<Fn<Double, Fn<Double, Double>>>
  subtractD() { return Thunk.ready(x -> Thunk.ready(y -> Thunk.lazy(__ ->
    x.eval() - y.eval()
  ))); }

  /** curried multiplication of doubles */
  public static
  Thunk<Fn<Double, Fn<Double, Double>>>
  mulD() { return Thunk.ready(x -> Thunk.ready(y -> Thunk.lazy(__ ->
    x.eval() * y.eval()
  ))); }
}
