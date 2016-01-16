package prelude;

public final class Tuple<A, B> {
  public static <A, B>
  Thunk<Fn<A, Fn<B, Tuple<A, B>>>>
  pair() { return Thunk.ready(a -> Thunk.ready(b -> Thunk.lazy(__ ->
    new Tuple<A, B>(a, b)
  ))); }

  /** Extracts the 1st element of a 2-tuple
    *
    * in Haskell, the type would be:
    *
    *     snd :: (a,b) -> a
    */
  public static <A, B>
  Thunk<Fn<Tuple<A, B>, 
           A>>
  fst() { return Thunk.ready(
     pair -> pair.eval().fst
   ); }

  /** Extracts the 2nd element of a 2-tuple
    *
    * in Haskell, the type would be: 
    *
    *     snd :: (a,b) -> b
    */
  public static <A, B>
  Thunk<Fn<Tuple<A, B>,
           B>>
  snd() { return Thunk.ready(
     pair -> pair.eval().snd
   ); }

  private Tuple(final Thunk<A> fst,
                final Thunk<B> snd) {
    this.fst = fst;
    this.snd = snd;
  }

  private final Thunk<A> fst;
  private final Thunk<B> snd;
}
