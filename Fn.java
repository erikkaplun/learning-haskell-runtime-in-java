/** A thunked computation from values of type ArgT to values of type RetT */
public interface Fn<ArgT, RetT> {
  public static <ArgT, RetT>
    Thunk<RetT> apply(Thunk<Fn<ArgT, RetT>> f, Thunk<ArgT> x)
  {
    return f.eval().call(x);
  }

  // this really ought to be private, but interface members are forcibly public,
  // but we can't use a class here because functional interfaces only work
  // with, well, interfaces, and we really need the lambda syntax support for
  // readability and type inference.
  Thunk<RetT> call(final Thunk<ArgT> arg);
}
