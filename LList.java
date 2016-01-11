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

  static <A> Thunk<LList<A>> take(final Thunk<Integer> n, final Thunk<LList<A>> xs) { return Thunk.lazy(__ -> {
    return n.eval() == 0
      ? new LList<A>(null, null)
      : new LList<A>(xs.eval().head, take(Thunk.ready(n.eval() - 1), xs.eval().tail));
  }); }

  static <A, B> Thunk<LList<B>> map(final Thunk<Fn<A, B>> f, final Thunk<LList<A>> xs) {
    return If.if_(LList.isNil(xs),
                  LList.<B>nil(),
                  LList.<B>cons(Fn.apply(f, LList.head(xs)),
                                Thunk.lazy(__ -> map(f, LList.tail(xs)).eval())));
  }

  static <A> Thunk<LList<A>> filter(final Thunk<Fn<A, Boolean>> pred,
                                    final Thunk<LList<A>> xs) {
        return If.if_(LList.isNil(xs),
                      xs,
                      Thunk.lazy(___ -> {
                          Thunk<A> head        = LList.head(xs);
                          Thunk<LList<A>> tail = LList.tail(xs);
                          return If.if_(Fn.apply(pred, head),
                                        LList.cons(head, filter(pred, tail)),
                                        filter(pred, tail)).eval();
                        })
                      );
  }

  /** Prints a list in a way that printing starts before the list is fully evaluated.
   * Useful when printing infinite lists -- without laziness, printing an infinite list
   * ends in an error before nothing even gets printed; `print` however prints at
   * least something before running out of stack.
   */
  static <A> void print(final Thunk<LList<A>> xs) {
    if (LList.isNil(xs).eval())
      System.out.println("[]");
    else {
      final String str = xs.eval().head.toString();
      System.out.print(str + " : ");
      print(xs.eval().tail);
    }
  }

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
  static <A> Thunk<LList<A>> generate(final Thunk<A> seed,
                                      final Thunk<Fn<A, A>> next) { return Thunk.lazy(__ -> {
    return new LList<A>(seed,
                        Thunk.lazy(___ -> generate(Fn.apply(next, seed), next).eval()));
  }); }
}
