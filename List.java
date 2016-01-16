/** Linked List; the Haskell
 * constructors cons and nil are
 * represented by internal state
 *
 * In Haskell, this would be written as:
 *
 *   data List = Cons head tail | Nil
 *
 * and is isomorphic to the built-in Haskell List type.
 */
public final class List<A> {
  /** The empty list */
  public static <A>
  Thunk<List<A>>
  nil() { return Thunk.ready(
    new List<A>(null, null)
  ); }

  /** Takes the List `tail` and prepends `head` to it, returning a new List */
  public static <A>
  Thunk<Fn<A, Fn<List<A>, List<A>>>>
  cons() { return Thunk.ready(x -> Thunk.ready(xs -> Thunk.lazy(__ ->
     new List<A>(x, xs)
  ))); }

  /** Takes the head of a List */
  public static <A>
  Thunk<Fn<List<A>, A>>
  head() { return Thunk.ready(xs -> Thunk.lazy(__ ->
    xs.eval().head.eval()
  )); }

  /** Takes the tail of a List */
  public static <A>
  Thunk<Fn<List<A>, List<A>>>
  tail() { return Thunk.ready(xs -> Thunk.lazy(__ ->
    xs.eval().tail.eval()
  )); }

  public static <A>
  Thunk<Fn<List<A>, Boolean>>
  isNil() { return Thunk.ready(xs -> Thunk.lazy(__ ->
    xs.eval().head == null
  )); }

  public static <A>
  Thunk<Fn<List<A>, Boolean>>
  isCons() { return Thunk.ready(xs -> Thunk.lazy(__ ->
    xs.eval().head != null
  )); }

  private List(Thunk<      A > head,
                Thunk<List<A>> tail)
  { this.head = head;
    this.tail = tail; }

  private final Thunk<      A > head;
  private final Thunk<List<A>> tail;

  // this completely bypasses the evaluation engine,
  // but for the sake of simplicity, let's not take the step
  // to lazy strings and such (yet).
  public String toString() {
    // this is a Java-land method so Java-values need 
    // to be wrapped:
    Thunk<List<A>> self = Thunk.ready(this);

    Thunk<String> ret = Fn.apply(List.pretty(), self);

    // and the final result unwrapped again
    return ret.eval();
  }

  static <A>
  Thunk<Fn<List<A>, Integer>>
  len() { return Thunk.ready(xs ->
    If.if_(Fn.apply(List.isNil(), xs),
           Thunk.ready(0),
           Thunk.lazy(__ -> 1 + Fn.apply(len(), xs.eval().tail).eval()))
  ); }

  static <A> Thunk<Fn<Integer, Fn<List<A>, A>>>
  elemAt() { return Thunk.ready(ix -> Thunk.ready(xs -> Thunk.lazy(__ ->
    (ix.eval() == 0
     ? xs.eval().head
     : Fn.apply2(elemAt(), 
                 Thunk.ready(ix.eval() - 1),
                 xs.eval().tail)).eval()
  ))); }

  /** take first `n` elements of a list */
  static <A>
  Thunk<Fn<Integer, Fn<List<A>, List<A>>>>
  take() { return Thunk.ready(n -> Thunk.ready(xs ->
    If.if_(Fn.apply2(Eq.eqI(), n, Thunk.ready(0)),
  /*then*/ List.nil(),
  /*else*/ Fn.apply2(List.cons(),
                     Fn.apply(List.head(), xs),
                     Fn.apply2(take(),
                               Fn.apply2(Num.subtractI(), n, Thunk.ready(1)),
                               Fn.apply(List.tail(), xs))))
  )); }

  // map :: (a -> b) -> [a] -> [b]
  static <A, B>
  Thunk<Fn<Fn<A,B>, Fn<List<A>, List<B>>>>
  map() { return Thunk.ready(f -> Thunk.ready(xs ->
    If.if_(Fn.apply(List.isNil(), xs),
  /*then*/ List.nil(),
  /*else*/ Fn.apply2(List.cons(),
                     Fn.apply(f, Fn.apply(List.head(), xs)),
                     Fn.apply2(map(), f, Fn.apply(List.tail(), xs))))
  )); }

  // filter :: (a -> Bool) -> [a] -> [a]
  static <A>
  Thunk<Fn<Fn<A, Boolean>, Fn<List<A>, List<A>>>>
  filter() { return Thunk.ready(pred -> Thunk.ready(xs ->
    If.if_(Fn.apply(List.isNil(), xs),
  /*then*/ xs,
  /*else*/ Thunk.lazy(___ -> {
               Thunk<A>        head = Fn.apply(List.head(), xs);
               Thunk<List<A>> tail = Fn.apply(List.tail(), xs);

               Thunk<List<A>> rest = Fn.apply2(filter(), pred, tail);
               return If.if_(Fn.apply(pred, head),
                             Fn.apply2(List.cons(),
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
  // static <A> void print(final Thunk<List<A>> xs) {
  //   if (Fn.apply(List.isNil(), xs).eval())
  //     System.out.println("[]");
  //   else {
  //     final String str = xs.eval().head.toString();
  //     System.out.print(str + " : ");
  //     print(xs.eval().tail);
  //   }
  // }

  /** Pretty-printer for lists */
  public static <A>
  Thunk<Fn<List<A>, String>>
  pretty() { return Thunk.ready(xs -> Thunk.lazy(__ ->
    Str.append(Thunk.ready("["),
               Fn.apply(_pretty(), xs),
               Thunk.ready("]")).eval()
  )); }

  private static <A>
  Thunk<Fn<List<A>, String>>
  _pretty() { return Thunk.ready(xs -> Thunk.lazy(__ -> {
    Thunk<List<A>> tail = Fn.apply(List.tail(), xs);
    Thunk<String> subPretty = Fn.apply(_pretty(), tail);
    return If.if_(Fn.apply(List.isNil(), xs),
         /*then*/ Thunk.ready(""),
         /*else*/ Str.append(Str.show(Fn.apply(List.head(), xs)),
                             If.if_(Fn.apply(List.isNil(), tail),
                                    Thunk.ready(""),
                                    Str.append(Thunk.ready(","),
                                               subPretty)))).eval();
  })); }

  /** Creates an infinite list of `A` where every value is `next` applied
   * to the previous value.  The first value is `seed`.
   */
  static <A>
  Thunk<Fn<A, Fn<Fn<A,A>, List<A>>>>
  generate() { return Thunk.ready(seed -> Thunk.ready(next -> {
    Thunk<A> newSeed = Fn.apply(next, seed);

    Thunk<List<A>> rest = Fn.apply2(generate(),
                                     newSeed, next);

    return Fn.apply2(List.cons(),
                     seed, rest);
  })); }

  /** zipWith :: [a] -> [b] -> (a -> b -> c) -> [c] */
  static <A, B, C>
  Thunk<  Fn<Fn<A, Fn<B, C>>,
          Fn<List<A>,
          Fn<List<B>,
             List<C>>>>  >
  zipWith() { return Thunk.ready(f -> Thunk.ready(xs -> Thunk.ready(ys ->
    If.if_(Fn.apply2(Bool.or(),
                     Fn.apply(List.isNil(), xs),
                     Fn.apply(List.isNil(), ys)),
           List.nil(),
           Thunk.lazy(__ -> {
               Thunk<C> head =
                 Fn.apply2(f,
                           Fn.apply(List.head(), xs),
                           Fn.apply(List.head(), ys));

               Thunk<List<A>> xsTail = Fn.apply(List.tail(), xs);
               Thunk<List<B>> ysTail = Fn.apply(List.tail(), ys);

               return Fn.apply2(List.cons(),
                                head,
                                Fn.apply3(zipWith(),
                                          f,
                                          xsTail,
                                          ysTail)).eval();
             }))
  ))); }

  /** zip :: [a] -> [b] -> [(a, b)] */
  static <A, B>
  Thunk<  Fn<List<A>,
          Fn<List<B>,
             List<Tuple<A, B>>>>  >
  zip() { return Fn.apply(zipWith(), Tuple.make()); }
}
