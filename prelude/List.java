package prelude;

import static prelude.Thunk.*;
import static prelude.Fn.*;
import static prelude.If.*;
import static prelude.Num.*;
import static prelude.Eq.*;
import static prelude.Str.*;
import static prelude.Bool.*;
import static prelude.Tuple.*;

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
  nil() { return thunk(
    new List<A>(null, null)
  ); }

  /** Takes the List `tail` and prepends `head` to it, returning a new List */
  public static <A>
  Thunk<Fn<A, Fn<List<A>, List<A>>>>
  cons() { return fn(x -> fn(xs -> lazy(__ ->
     new List<A>(x, xs)
  ))); }

  /** Takes the head of a List */
  public static <A>
  Thunk<Fn<List<A>, A>>
  head() { return fn(xs -> lazy(__ ->
    xs.eval().head.eval()
  )); }

  /** Takes the tail of a List */
  public static <A>
  Thunk<Fn<List<A>, List<A>>>
  tail() { return fn(xs -> lazy(__ ->
    xs.eval().tail.eval()
  )); }

  public static <A>
  Thunk<Fn<List<A>, Boolean>>
  isNil() { return fn(xs -> lazy(__ ->
    xs.eval().head == null
  )); }

  public static <A>
  Thunk<Fn<List<A>, Boolean>>
  isCons() { return fn(xs -> lazy(__ ->
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
    Thunk<List<A>> self = thunk(this);

    Thunk<String> ret = apply(pretty(), self);

    // and the final result unwrapped again
    return ret.eval();
  }

  public static <A>
  Thunk<Fn<List<A>, Integer>>
  len() { return fn(xs ->
    if_(apply(isNil(), xs),
        thunk(0),
        lazy(__ -> 1 + apply(len(), xs.eval().tail).eval()))
  ); }

  public static <A>
  Thunk<Fn<Integer, Fn<List<A>, A>>>
  elemAt() { return fn(ix -> fn(xs -> lazy(__ ->
    (ix.eval() == 0
     ? xs.eval().head
     : apply2(elemAt(), 
              thunk(ix.eval() - 1),
              xs.eval().tail)).eval()
  ))); }

  /** take first `n` elements of a list */
  public static <A>
  Thunk<Fn<Integer, Fn<List<A>, List<A>>>>
  take() { return fn(n -> fn(xs ->
    if_(apply2(eqI(), n, thunk(0)),
  /*then*/ nil(),
  /*else*/ apply2(cons(),
                     apply(head(), xs),
                     apply2(take(),
                               apply2(Num.subtractI(), n, thunk(1)),
                               apply(tail(), xs))))
  )); }

  // map :: (a -> b) -> [a] -> [b]
  public static <A, B>
  Thunk<Fn<Fn<A,B>, Fn<List<A>, List<B>>>>
  map() { return fn(f -> fn(xs ->
    if_(apply(isNil(), xs),
        /*then*/ nil(),
        /*else*/ apply2(cons(),
                        apply(f, apply(head(), xs)),
                        apply2(map(), f, apply(tail(), xs))))
  )); }

  // filter :: (a -> Bool) -> [a] -> [a]
  public static <A>
  Thunk<Fn<Fn<A, Boolean>, Fn<List<A>, List<A>>>>
  filter() { return fn(pred -> fn(xs ->
    if_(apply(isNil(), xs),
        /*then*/ xs,
        /*else*/ lazy(___ -> {
            Thunk<A>       head = apply(head(), xs);
            Thunk<List<A>> tail = apply(tail(), xs);
            
            Thunk<List<A>> rest = apply2(filter(), pred, tail);
            return if_(apply(pred, head),
                       apply2(cons(),
                              head, rest),
                       rest).eval();
          }))
  )); }

  // /** Prints a list in a way that printing starts before the list is fully evaluated.
  //  * Useful when printing infinite lists -- without laziness, printing an infinite list
  //  * ends in an error before nothing even gets printed; `print` however prints at
  //  * least something before running out of stack.
  //  */
  // public static <A> void print(final Thunk<List<A>> xs) {
  //   if (apply(isNil(), xs).eval())
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
  pretty() { return fn(xs -> lazy(__ ->
    append(thunk("["),
           apply(_pretty(), xs),
           thunk("]")).eval()
  )); }

  private static <A>
  Thunk<Fn<List<A>, String>>
  _pretty() { return fn(xs -> lazy(__ -> {
    Thunk<List<A>> tail = apply(tail(), xs);
    Thunk<String> subPretty = apply(_pretty(), tail);
    return if_(apply(isNil(), xs),
      /*then*/ thunk(""),
      /*else*/ append(show(apply(head(), xs)),
                      if_(apply(isNil(), tail),
                          thunk(""),
                          append(thunk(","),
                                 subPretty)))).eval();
  })); }

  /** Creates an infinite list of `A` where every value is `next` applied
   * to the previous value.  The first value is `seed`.
   */
  public static <A>
  Thunk<Fn<A, Fn<Fn<A,A>, List<A>>>>
  generate() { return fn(seed -> fn(next -> {
    Thunk<A> newSeed = apply(next, seed);

    Thunk<List<A>> rest = apply2(generate(),
                                 newSeed, next);

    return apply2(cons(), seed, rest);
  })); }

  /** zipWith :: [a] -> [b] -> (a -> b -> c) -> [c] */
  public static <A, B, C>
  Thunk<  Fn<Fn<A, Fn<B, C>>,
          Fn<List<A>,
          Fn<List<B>,
             List<C>>>>  >
  zipWith() { return fn(f -> fn(xs -> fn(ys ->
    if_(apply2(or(),
               apply(isNil(), xs),
               apply(isNil(), ys)),
        nil(),
        lazy(__ -> {
            Thunk<C> head =
              apply2(f,
                     apply(head(), xs),
                     apply(head(), ys));

            Thunk<List<A>> xsTail = apply(tail(), xs);
            Thunk<List<B>> ysTail = apply(tail(), ys);

            return apply2(cons(),
                          head,
                          apply3(zipWith(),
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
  zip() { return apply(zipWith(), pair()); }
}
