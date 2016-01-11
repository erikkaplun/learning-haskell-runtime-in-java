class Thunk<A> {
  public final A eval() {
    if (value != null) return value;
    else { value = computation.compute(null); return value; }
  }

  /** Make a Thunk with a lazily executed computation in it. */
  public Thunk(Computation<A> computation) {
    this.computation = computation;
  }
  /** Make a Thunk with an already computed value in it. */
  public Thunk(A value)            {
    // FP *never* uses null pointers as real values, so we can safely use
    // nulls inside our low-level, (hopefully) hidden plumbing.
    assert(value != null);

    this.value = value;
  }

  /** Convenience/readability alias for new Thunk<A>(value) */
  public static <A> Thunk<A> ready(            A  x) { return new Thunk<A>(x); }
  /** Convenience/readability alias for new Thunk<A>(computation) */
  public static <A> Thunk<A> lazy (Computation<A> x) { return new Thunk<A>(x); }

  private             A  value       = null;
  private Computation<A> computation = null;

  /** a helper interface for Thunk.lazy that should not be used directly */
  public static interface Computation<A> { A compute(Void v); }

  // just for convenience
  public String toString() { return eval().toString(); }
}

/** A thunked computation from values of type ArgT to values of type RetT */
interface Fn<ArgT, RetT> {
  Thunk<RetT> call(final Thunk<ArgT> arg);

  public static <ArgT, RetT>
    Thunk<RetT> apply(Thunk<Fn<ArgT, RetT>> f, Thunk<ArgT> x)
  {
    return f.eval().call(x);
  }
}

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
class LList<A> {
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
    return LList.isNil(Thunk.ready(this)).eval()
      ? "[]"
      : head.toString() + " : " + tail.toString();
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

  /** Creates an infinite list of `A` where every value is `next` applied
   * to the previous value.  The first value is `seed`.
   */
  static <A> Thunk<LList<A>> generate(final Thunk<A> seed,
                                      final Fn<A, A> next) { return Thunk.lazy(__ -> {
    return new LList<A>(seed,
                        Thunk.lazy(___ -> generate(next.call(seed), next).eval()));
  }); }
}

class If {
  /** if_(foo, bar, baz) == bar   iff foo evaluates to true
   *  if_(foo, bar, baz) == bar   iff foo evaluates to false
   *
   * The semantics is exactly like the C/Java style ternary if-expression.
   * `baz` will not be computed if `bar` is evaluated, and vice versa.
   */
  static <A> Thunk<A> if_(Thunk<Boolean> cond,
                          Thunk<A> v1,
                          Thunk<A> v2) { return Thunk.lazy(__ -> {
    return (cond.eval() ? v1 : v2).eval();
  }); }
}

public class LazyEvaluator {
  static Fn<Double, Double> incrByD(final double d) { return arg -> Thunk.ready(arg.eval() + d); }
  static Fn<Double, Double> mulByD(final double factor) { return arg -> Thunk.ready(arg.eval() * factor); }
  static Fn<Integer, Integer> incrByI(final int d) { return arg ->Thunk.ready(arg.eval() + d); }
  static Thunk<Fn<Integer, Integer>> mulByI(final int factor) { return Thunk.ready(arg -> Thunk.ready(arg.eval() * factor)); }

  public static void main(String[] args) {
    Thunk<LList<Integer>> nums = LList.generate(Thunk.ready(0), incrByI(1));
    Thunk<LList<Integer>> dblNums = LList.map(mulByI(2), nums);

    Thunk<Fn<Integer, Boolean>> isEven = Thunk.ready(x -> even(x));
    Thunk<LList<Integer>> evens = LList.filter(isEven, nums);
    System.out.println(LList.take(Thunk.ready(3), evens));

    Thunk<LList<Integer>> primes_ = primes();
    Thunk<LList<Integer>> doublePrimes = LList.map(mulByI(2), primes_);
    LList.print(LList.take(Thunk.ready(10), doublePrimes));

    /////////////////////

    Thunk<Integer> value =
      If.if_(Thunk.ready(true), Thunk.ready(3), Thunk.ready(4));
    System.out.println(value);
  };

  static Thunk<Boolean> even(final Thunk<Integer> i) {
    return eq(mod(i, Thunk.ready(2)), Thunk.ready(0));
  }

  static Thunk<Boolean> eq(final Thunk<Integer> a, final Thunk<Integer> b) { return Thunk.lazy(__ -> {
    return a.eval() == b.eval();
  }); }

  static Thunk<Double> sum(final Thunk<LList<Double>> xs) {
    return If.if_(LList.isNil(xs),
                  Thunk.ready(0.0),
                  Thunk.lazy(__ -> LList.head(xs).eval() + sum(LList.tail(xs)).eval()));
  }

  static Thunk<Double> avg(final Thunk<LList<Double>> xs) { return Thunk.lazy(__ ->  {
    final Integer n = LList.len(xs).eval();
    return n == 0
      ? 0.0
      : sum(xs).eval() / n;
  }); }

  static Thunk<Integer> mod(final Thunk<Integer> dividend, final Thunk<Integer> divisor) { return Thunk.lazy(__ -> {
    return dividend.eval() % divisor.eval();
  }); }

  static Thunk<Boolean> gt(final Thunk<Integer> a, final Thunk<Integer> b) { return Thunk.lazy(__ -> {
    return a.eval() > b.eval();
  }); }

  static Thunk<LList<Integer>> sieve(Thunk<LList<Integer>> xs_) { return Thunk.lazy(__ -> {
    final Thunk<Integer> p   = LList.head(xs_);
    Thunk<LList<Integer>> xs = LList.tail(xs_);

    Thunk<Fn<Integer, Boolean>> pred = Thunk.ready(x -> gt(mod(x, p), Thunk.ready(0)));

    return LList.cons(p, sieve(LList.filter(pred, xs))).eval();
  }); }

  static Thunk<LList<Integer>> primes() {
    return sieve(LList.generate(Thunk.ready(2), incrByI(1)));
  }
}

class Unit {
  private Unit() {}
  public static final Unit INST = new Unit();
}
