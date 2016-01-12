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
  public static <A>
  Thunk<LList<A>>
  nil() { return Thunk.ready(
    new LList<A>(null, null)
  ); }

  /** Takes the LList `tail` and prepends `head` to it, returning a new LList */
  public static <A>
  Thunk<Fn<A, Fn<LList<A>, LList<A>>>>
  cons() { return Thunk.ready(x -> Thunk.ready(xs -> Thunk.lazy(__ ->
     new LList<A>(x, xs)
  ))); }

  /** Takes the head of a LList */
  public static <A>
  Thunk<Fn<LList<A>, A>>
  head() { return Thunk.ready(xs -> Thunk.lazy(__ ->
    xs.eval().head.eval()
  )); }

  /** Takes the tail of a LList */
  public static <A>
  Thunk<Fn<LList<A>, LList<A>>>
  tail() { return Thunk.ready(xs -> Thunk.lazy(__ ->
    xs.eval().tail.eval()
  )); }

  public static <A>
  Thunk<Fn<LList<A>, Boolean>>
  isNil() { return Thunk.ready(xs -> Thunk.lazy(__ ->
    xs.eval().head == null
  )); }

  public static <A>
  Thunk<Fn<LList<A>, Boolean>>
  isCons() { return Thunk.ready(xs -> Thunk.lazy(__ ->
    xs.eval().head != null
  )); }

  private LList(Thunk<      A > head,
                Thunk<LList<A>> tail)
  { this.head = head;
    this.tail = tail; }

  private final Thunk<      A > head;
  private final Thunk<LList<A>> tail;

  // this completely bypasses the evaluation engine,
  // but for the sake of simplicity, let's not take the step
  // to lazy strings and such (yet).
  public String toString() {
    // this is a Java-land method so Java-values need 
    // to be wrapped:
    Thunk<LList<A>> self = Thunk.ready(this);

    Thunk<String> ret = Fn.apply(LList.pretty(), self);

    // and the final result unwrapped again
    return ret.eval();
  }

  static <A>
  Thunk<Fn<LList<A>, Integer>>
  len() { return Thunk.ready(xs ->
    If.if_(Fn.apply(LList.isNil(), xs),
           Thunk.ready(0),
           Thunk.lazy(__ -> 1 + Fn.apply(len(), xs.eval().tail).eval()))
  ); }

  static <A> Thunk<Fn<Integer, Fn<LList<A>, A>>>
  elemAt() { return Thunk.ready(ix -> Thunk.ready(xs -> Thunk.lazy(__ ->
    (ix.eval() == 0
     ? xs.eval().head
     : Fn.apply2(elemAt(), 
                 Thunk.ready(ix.eval() - 1),
                 xs.eval().tail)).eval()
  ))); }

  /** take first `n` elements of a list */
  static <A>
  Thunk<Fn<Integer, Fn<LList<A>, LList<A>>>>
  take() { return Thunk.ready(n -> Thunk.ready(xs ->
    If.if_(Fn.apply2(Eq.eqI(), n, Thunk.ready(0)),
  /*then*/ LList.nil(),
  /*else*/ Fn.apply2(LList.cons(),
                     Fn.apply(LList.head(), xs),
                     Fn.apply2(take(),
                               Fn.apply2(Num.subtractI(), n, Thunk.ready(1)),
                               Fn.apply(LList.tail(), xs))))
  )); }

  // map :: (a -> b) -> [a] -> [b]
  static <A, B>
  Thunk<Fn<Fn<A,B>, Fn<LList<A>, LList<B>>>>
  map() { return Thunk.ready(f -> Thunk.ready(xs ->
    If.if_(Fn.apply(LList.isNil(), xs),
  /*then*/ LList.nil(),
  /*else*/ Fn.apply2(LList.cons(),
                     Fn.apply(f, Fn.apply(LList.head(), xs)),
                     Fn.apply2(map(), f, Fn.apply(LList.tail(), xs))))
  )); }

  // filter :: (a -> Bool) -> [a] -> [a]
  static <A>
  Thunk<Fn<Fn<A, Boolean>, Fn<LList<A>, LList<A>>>>
  filter() { return Thunk.ready(pred -> Thunk.ready(xs ->
    If.if_(Fn.apply(LList.isNil(), xs),
  /*then*/ xs,
  /*else*/ Thunk.lazy(___ -> {
               Thunk<A>        head = Fn.apply(LList.head(), xs);
               Thunk<LList<A>> tail = Fn.apply(LList.tail(), xs);

               Thunk<LList<A>> rest = Fn.apply2(filter(), pred, tail);
               return If.if_(Fn.apply(pred, head),
                             Fn.apply2(LList.cons(),
                                       head, rest),
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
  //   if (Fn.apply(LList.isNil(), xs).eval())
  //     System.out.println("[]");
  //   else {
  //     final String str = xs.eval().head.toString();
  //     System.out.print(str + " : ");
  //     print(xs.eval().tail);
  //   }
  // }

  /** Pretty-printer for lists */
  public static <A>
  Thunk<Fn<LList<A>, String>>
  pretty() { return Thunk.ready(xs -> Thunk.lazy(__ ->
    Str.append(Thunk.ready("["),
               Fn.apply(_pretty(), xs),
               Thunk.ready("]")).eval()
  )); }

  private static <A>
  Thunk<Fn<LList<A>, String>>
  _pretty() { return Thunk.ready(xs -> Thunk.lazy(__ -> {
    Thunk<LList<A>> tail = Fn.apply(LList.tail(), xs);
    Thunk<String> subPretty = Fn.apply(_pretty(), tail);
    return If.if_(Fn.apply(LList.isNil(), xs),
         /*then*/ Thunk.ready(""),
         /*else*/ Str.append(Str.show(Fn.apply(LList.head(), xs)),
                             If.if_(Fn.apply(LList.isNil(), tail),
                                    Thunk.ready(""),
                                    Str.append(Thunk.ready(","),
                                               subPretty)))).eval();
  })); }

  /** Creates an infinite list of `A` where every value is `next` applied
   * to the previous value.  The first value is `seed`.
   */
  static <A>
  Thunk<Fn<A, Fn<Fn<A,A>, LList<A>>>>
  generate() { return Thunk.ready(seed -> Thunk.ready(next -> {
    Thunk<A> newSeed = Fn.apply(next, seed);

    Thunk<LList<A>> rest = Fn.apply2(generate(),
                                     newSeed, next);

    return Fn.apply2(LList.cons(), 
                     seed, rest);
  })); }
}
