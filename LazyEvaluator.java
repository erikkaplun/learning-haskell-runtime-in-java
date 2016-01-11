class Thunk<A> {
  public final A eval() {
    if (value != null) return value;
    else { value = compute(); return value; }
  }

  public A compute() {
    throw new RuntimeException("must override or provide immediately available value");
  }

  private A value = null;

  // just for convenience
  public String toString() { return eval().toString(); }

  /** Make a Thunk with an immediately available value in it. */
  public static <A> Thunk<A> ready(final A value) {
    assert(value != null);
    final Thunk<A> ret = new Thunk<>();
    ret.value = value;
    return ret;
  }

  public interface Compute<A> { A compute(Void v); }
  public static <A> Thunk<A> make(final Compute<A> compute) {
    return new Thunk<A>() { public A compute() {
      return compute.compute(null);
    } };
  }
}

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
 */
class LList<A> {
  public final Thunk<      A > head;
  public final Thunk<LList<A>> tail;
  LList(Thunk<A> head, Thunk<LList<A>> tail) {
    this.head = head;  this.tail = tail;
  }

  // this completely bypasses the evaluation engine,
  // but for the sake of simplicity, let's not take the step
  // to lazy strings and such.
  public String toString() {
    return LList.isNil(Thunk.ready(this)).eval()
      ? "[]"
      : head.toString() + " : " + tail.toString();
  }

  public static <A> Thunk<A> head(Thunk<LList<A>> xs) { return Thunk.make(__ -> {
    return xs.eval().head.eval();
  }); }
  public static <A> Thunk<LList<A>> tail(Thunk<LList<A>> xs) { return Thunk.make(__ -> {
    return xs.eval().tail.eval();
  }); }
  public static <A> Thunk<LList<A>> nil() {
    return Thunk.ready(new LList<A>(null, null));
  }
  public static <A> Thunk<LList<A>> cons(final Thunk<A> head, final Thunk<LList<A>> tail) {
    return Thunk.make(__ -> new LList<A>(head, tail));
  }
  public static <A> Thunk<Boolean> isNil(final Thunk<LList<A>> xs) {
    return Thunk.make(__ -> xs.eval().head == null);
  }
  public static <A> Thunk<Boolean> isCons(final Thunk<LList<A>> xs) {
    return Thunk.make(__ -> xs.eval().head != null);
  }
}

// interface Fn2<A, B, R> extends Fn<A, Fn<B, R>> {}
// interface Fn3<A, B, C, R> extends Fn<A, Fn<B, Fn<C, R>>> {}

public class LazyEvaluator {
  static <A> Thunk<A> if_(Thunk<Boolean> cond,
                          Thunk<A> v1,
                          Thunk<A> v2) { return Thunk.make(__ -> {
    return (cond.eval() ? v1 : v2).eval();
  }); }

  static Fn<Double, Double> incrByD(final double d) { return arg -> Thunk.ready(arg.eval() + d); }
  static Fn<Double, Double> mulByD(final double factor) { return arg -> Thunk.ready(arg.eval() * factor); }
  static Fn<Integer, Integer> incrByI(final int d) { return arg ->Thunk.ready(arg.eval() + d); }
  static Thunk<Fn<Integer, Integer>> mulByI(final int factor) { return Thunk.ready(arg -> Thunk.ready(arg.eval() * factor)); }

  public static void main(String[] args) {
    Thunk<LList<Integer>> nums = generate(Thunk.ready(0), incrByI(1));
    Thunk<LList<Integer>> dblNums = map(mulByI(2), nums);

    Thunk<Fn<Integer, Boolean>> isEven = Thunk.ready(x -> even(x));
    Thunk<LList<Integer>> evens = filter(isEven, nums);
    System.out.println(take(Thunk.ready(3), evens));

    Thunk<LList<Integer>> primes_ = primes();
    Thunk<LList<Integer>> doublePrimes = map(mulByI(2), primes_);
    printList(take(Thunk.ready(10), doublePrimes));

    /////////////////////

    Thunk<Integer> value =
      if_(Thunk.ready(true), Thunk.ready(3), Thunk.ready(4));
    System.out.println(value);
  };

  static Thunk<Boolean> even(final Thunk<Integer> i) {
    return eq(mod(i, Thunk.ready(2)), Thunk.ready(0));
  }

  static Thunk<Boolean> eq(final Thunk<Integer> a, final Thunk<Integer> b) { return Thunk.make(__ -> {
    return a.eval() == b.eval();
  }); }

  static Thunk<Double> sum(final Thunk<LList<Double>> xs) {
    return if_(LList.isNil(xs),
               Thunk.ready(0.0),
               Thunk.make(__ -> xs.eval().head.eval() + sum(xs.eval().tail).eval()));
  }

  static <A> Thunk<Integer> len(final Thunk<LList<A>> xs) {
    return if_(LList.isNil(xs),
               Thunk.ready(0),
               Thunk.make(__ -> 1 + len(xs.eval().tail).eval()));
  }

  static Thunk<Double> avg(final Thunk<LList<Double>> xs) { return Thunk.make(__ ->  {
    final Integer n = len(xs).eval();
    return n == 0
      ? 0.0
      : sum(xs).eval() / n;
  }); }

  static <A> Thunk<A> elemAt(Thunk<Integer> ix, Thunk<LList<A>> xs) { return Thunk.make(__ -> {
    return ix.eval() == 0
      ? xs.eval().head.eval()
      : elemAt(Thunk.ready(ix.eval() - 1), xs.eval().tail).eval();
  }); }

  static <A> Thunk<LList<A>> take(final Thunk<Integer> n, final Thunk<LList<A>> xs) { return Thunk.make(__ -> {
    return n.eval() == 0
      ? new LList<A>(null, null)
      : new LList<A>(xs.eval().head, take(Thunk.ready(n.eval() - 1), xs.eval().tail));
  }); }

  static <A, B> Thunk<LList<B>> map(final Thunk<Fn<A, B>> f, final Thunk<LList<A>> xs) {
    return if_(LList.isNil(xs),
               LList.<B>nil(),
               LList.<B>cons(Fn.apply(f, LList.head(xs)),
                             Thunk.make(__ -> map(f, LList.tail(xs)).eval())));
  }

  static <A> Thunk<LList<A>> filter(final Thunk<Fn<A, Boolean>> pred,
                                    final Thunk<LList<A>> xs) {
        return if_(LList.isNil(xs),
                   xs,
                   Thunk.make(___ -> {
                       Thunk<A> head        = LList.head(xs);
                       Thunk<LList<A>> tail = LList.tail(xs);
                       return if_(Fn.apply(pred, head),
                                  LList.cons(head, filter(pred, tail)),
                                                   filter(pred, tail)).eval();
                     })
                   );
  }

  static <A> void printList(final Thunk<LList<A>> xs) {
    if (LList.isNil(xs).eval())
      System.out.println("[]");
    else {
      final String str = xs.eval().head.toString();
      System.out.print(str + " : ");
      printList(xs.eval().tail);
    }
  }

  static Thunk<Integer> mod(final Thunk<Integer> dividend, final Thunk<Integer> divisor) { return Thunk.make(__ -> {
    return dividend.eval() % divisor.eval();
  }); }

  static Thunk<Boolean> gt(final Thunk<Integer> a, final Thunk<Integer> b) { return Thunk.make(__ -> {
    return a.eval() > b.eval();
  }); }

  static Thunk<LList<Integer>> sieve(Thunk<LList<Integer>> xs_) { return Thunk.make(__ -> {
    final Thunk<Integer> p   = xs_.eval().head;
    Thunk<LList<Integer>> xs = xs_.eval().tail;

    Thunk<Fn<Integer, Boolean>> pred = Thunk.ready(x -> gt(mod(x, p), Thunk.ready(0)));

    return LList.cons(p, sieve(filter(pred, xs))).eval();
  }); }

  static Thunk<LList<Integer>> primes() {
    return sieve(generate(Thunk.ready(2), incrByI(1)));
  }
  
  static <A> Thunk<LList<A>> generate(final Thunk<A> seed,
                                      final Fn<A, A> next) { return Thunk.make(__ -> {
    return new LList<A>(seed,
                        Thunk.make(___ -> generate(next.call(seed), next).eval()));
  }); }
}

class Unit {
  private Unit() {}
  public static final Unit INST = new Unit();
}
