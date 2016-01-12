/** Linked List; the Haskell
 * constructors cons and nil are
 * represented by internal state
 *
 * In Haskell, this would be written as:
 *
 *   data LList = Cons head tail | Nil
 *
 * and is isomorphic to the built-in Haskell List type.
 */
public class LList<A> {
  /** The empty list */
  public static <A> Thunk<LList<A>> nil() {
    return Thunk.ready(new LList<A>(null, null));
  }
  /** Takes the LList `tail` and prepends `head` to it, returning a new LList */
  public static <A> Thunk<LList<A>> cons(final Thunk<A> head, final Thunk<LList<A>> tail) {
    return Thunk.lazy(__ -> new LList<A>(head, tail));
  }

  /** Takes the head of a LList */
  public static <A> Thunk<A> head(Thunk<LList<A>> xs) { return Thunk.lazy(__ -> {
    return xs.eval().head.eval();
  }); }
  /** Takes the tail of a LList */
  public static <A> Thunk<LList<A>> tail(Thunk<LList<A>> xs) { return Thunk.lazy(__ -> {
    return xs.eval().tail.eval();
  }); }

  public static <A> Thunk<Boolean> isNil(final Thunk<LList<A>> xs) {
    return Thunk.lazy(__ -> xs.eval().head == null);
  }
  public static <A> Thunk<Boolean> isCons(final Thunk<LList<A>> xs) {
    return Thunk.lazy(__ -> xs.eval().head != null);
  }

  private LList(Thunk<      A > head,
        Thunk<LList<A>> tail)
  {
    this.head = head;
    this.tail = tail;
  }

  private final Thunk<      A > head;
  private final Thunk<LList<A>> tail;

  // this completely bypasses the evaluation engine,
  // but for the sake of simplicity, let's not take the step
  // to lazy strings and such (yet).
  public String toString() {
    return LList.pretty(Thunk.ready(this)).eval();
  }

  static <A> Thunk<Integer> len(final Thunk<LList<A>> xs) {
    return If.if_(LList.isNil(xs),
                  Thunk.ready(0),
                  Thunk.lazy(__ -> 1 + len(xs.eval().tail).eval()));
  }

  static <A> Thunk<A> elemAt(Thunk<Integer> ix, Thunk<LList<A>> xs) { return Thunk.lazy(__ -> {
    return ix.eval() == 0
      ? xs.eval().head.eval()
      : elemAt(Thunk.ready(ix.eval() - 1), xs.eval().tail).eval();
  }); }

  /** take first `n` elements of a list */
  static <A> Thunk<Fn<Integer, Fn<LList<A>, LList<A>>>> take() {
    return Thunk.ready(n -> Thunk.ready(xs -> {
      return If.if_(Fn.apply2(Eq.eqI(), n, Thunk.ready(0)),
                    /*then*/ LList.nil(),
                    /*else*/ LList.cons(LList.head(xs),
                                        Fn.apply2(take(),
                                                  Fn.apply2(Num.subtract(), n, Thunk.ready(1)),
                                                  LList.tail(xs))));
    }));
  }

  // map :: (a -> b) -> [a] -> [b]
  static <A, B>
  Thunk<Fn<Fn<A,B>, Fn<LList<A>, LList<B>>>>
  map() { return Thunk.ready(f -> Thunk.ready(xs ->
    If.if_(LList.isNil(xs),
  /*then*/ LList.<B>nil(),
  /*else*/ LList.<B>cons(Fn.apply(f, LList.head(xs)),
                         Fn.apply2(map(), f, LList.tail(xs))))
  )); }

  // filter :: (a -> Bool) -> [a] -> [a]
  static <A>
  Thunk<Fn<Fn<A, Boolean>, Fn<LList<A>, LList<A>>>>
  filter() { return Thunk.ready(pred -> Thunk.ready(xs ->
    If.if_(LList.isNil(xs),
  /*then*/ xs,
  /*else*/ Thunk.lazy(___ -> {
               Thunk<A>        head = LList.head(xs);
               Thunk<LList<A>> tail = LList.tail(xs);

               Thunk<LList<A>> rest = Fn.apply2(filter(), pred, tail);
               return If.if_(Fn.apply(pred, head),
                             LList.cons(head, rest),
                             rest).eval();
             })
           )
  )); }

  // /** Prints a list in a way that printing starts before the list is fully evaluated.
  //  * Useful when printing infinite lists -- without laziness, printing an infinite list
  //  * ends in an error before nothing even gets printed; `print` however prints at
  //  * least something before running out of stack.
  //  */
  // static <A> void print(final Thunk<LList<A>> xs) {
  //   if (LList.isNil(xs).eval())
  //     System.out.println("[]");
  //   else {
  //     final String str = xs.eval().head.toString();
  //     System.out.print(str + " : ");
  //     print(xs.eval().tail);
  //   }
  // }

  /** Pretty-printer for lists */
  public static <A> Thunk<String> pretty(Thunk<LList<A>> xs) { return Thunk.lazy(__ -> {
    return Str.append(Thunk.ready("["),
                      _pretty(xs),
                      Thunk.ready("]")).eval();
  }); }
  private static <A> Thunk<String> _pretty(Thunk<LList<A>> xs) { return Thunk.lazy(__ -> {
    Thunk<LList<A>> tail = LList.tail(xs);
    Thunk<String> subPretty = _pretty(tail);
    return If.if_(LList.isNil(xs),
         /*then*/ Thunk.ready(""),
         /*else*/ Str.append(Str.show(LList.head(xs)),
                             If.if_(LList.isNil(tail),
                                    Thunk.ready(""),
                                    Str.append(Thunk.ready(","),
                                               subPretty)))).eval();
  }); }

  /** Creates an infinite list of `A` where every value is `next` applied
   * to the previous value.  The first value is `seed`.
   */
  static <A> Thunk<LList<A>> generate(Thunk<A       > seed,
                                      Thunk<Fn<A, A>> next) { return Thunk.lazy(__ -> {
    Thunk<A> newSeed = Fn.apply(next, seed);

    return LList.cons(seed, generate(newSeed, next)).eval();
  }); }
}
