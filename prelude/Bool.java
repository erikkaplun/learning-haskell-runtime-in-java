package prelude;

public final class Bool {
  public static
  Thunk<Fn<Boolean, Fn<Boolean, Boolean>>>
  or() { return Thunk.ready(a -> Thunk.ready(b -> Thunk.lazy(__ -> 
    a.eval().booleanValue() || b.eval().booleanValue()
  ))); }

  public static
  Thunk<Fn<Boolean, Fn<Boolean, Boolean>>>
  and() { return Thunk.ready(a -> Thunk.ready(b -> Thunk.lazy(__ -> 
    a.eval().booleanValue() && b.eval().booleanValue()
  ))); }

  public static
  Thunk<Fn<Boolean, Boolean>>
  not() { return Thunk.ready(a -> Thunk.lazy(__ -> 
    !a.eval().booleanValue()
  )); }
}
