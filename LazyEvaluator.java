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
}

abstract class Lambda<ArgT, RetT> {
  public abstract Thunk<RetT> call(final Thunk<ArgT> arg);
}

/** Linked List; the Haskell
 * constructors cons and nil are
 * represented by internal state
 */
class LList<A> {
  public final Thunk<A>       head;
  public final Thunk<LList<A>> tail;
  LList(Thunk<A> head, Thunk<LList<A>> tail) {
    this.head = head;  this.tail = tail;
  }

  public final boolean isNil()  { return head == null; }
  public final boolean isCons() { return head != null; }

  // this completely bypasses the evaluation engine,
  // but for the sake of simplicity, let's not take the step
  // to lazy strings and such.
  public String toString() {
    return isNil()
      ? "[]"
      : head.toString() + " : " + tail.toString();
  }

  public static <A> Thunk<LList<A>> nil() { return Thunk.ready(new LList<A>(null, null)); }
  public static <A> Thunk<LList<A>> cons(final Thunk<A> head, final Thunk<LList<A>> tail) { return new Thunk<LList<A>>() { public LList<A> compute() {
    return new LList<A>(head, tail);
  }; }; }
}

public class LazyEvaluator {
  static Lambda<Double, Double> incrByD(final double d) { return new Lambda<Double, Double>() { public Thunk<Double> call(final Thunk<Double> arg) {
    return Thunk.ready(arg.eval() + d);
  } }; }
  static Lambda<Double, Double> mulByD(final double factor) { return new Lambda<Double, Double>() { public Thunk<Double> call(final Thunk<Double> arg) {
    return Thunk.ready(arg.eval() * factor);
  } }; }
  static Lambda<Integer, Integer> incrByI(final int d) { return new Lambda<Integer, Integer>() { public Thunk<Integer> call(final Thunk<Integer> arg) {
      return Thunk.ready(arg.eval() + d);
  } }; }
  static Lambda<Integer, Integer> mulByI(final int factor) { return new Lambda<Integer, Integer>() { public Thunk<Integer> call(final Thunk<Integer> arg) {
    return Thunk.ready(arg.eval() * factor);
  } }; }

  public static void main(String[] args) {
    // Thunk<LList<Double>> nums1 = generate(Thunk.ready(0.0), incrByD(1.0));
    // Thunk<LList<Double>> nums2 = map(mulByD(1.0), nums1);
    // Thunk<LList<Double>> nums2first4 = take(Thunk.ready(4), nums2);
    // Thunk<LList<Double>> nums2first5 = take(Thunk.ready(5), nums2);
    // System.out.println("-----");

    // // printList(nums2first4);
    // printList(nums2first4);
    // printList(nums2first5);
    // System.out.println(sum(nums2first4));
    // System.out.println(len(nums2first4));
    // System.out.println(avg(nums2first4));

    /////////////////////////////////

    Lambda<Integer, Boolean> isEven = new Lambda<Integer, Boolean>() { public Thunk<Boolean> call(Thunk<Integer> x) {
        return even(x);
      } };

    Thunk<LList<Integer>> nums = generate(Thunk.ready(0), incrByI(1));
    Thunk<LList<Integer>> evens = filter(isEven, nums);
    System.out.println(take(Thunk.ready(3), evens));

    Thunk<LList<Integer>> primes_ = primes();
    Thunk<LList<Integer>> doublePrimes = map(mulByI(2), primes_);
    printList(take(Thunk.ready(10), doublePrimes));
  };

  static Thunk<Boolean> even(final Thunk<Integer> i) {
    return eq(mod(i, Thunk.ready(2)), Thunk.ready(0));
  }

  static Thunk<Boolean> eq(final Thunk<Integer> a, final Thunk<Integer> b) { return new Thunk<Boolean>() { public Boolean compute() {
    return a.eval() == b.eval();
  }; }; }

  static Thunk<Double> sum(final Thunk<LList<Double>> xs) { return new Thunk<Double>() { public Double compute() {
    return xs.eval().isNil()
      ? 0.0
      : xs.eval().head.eval() + sum(xs.eval().tail).eval();
  }; }; }

  static <A> Thunk<Integer> len(final Thunk<LList<A>> xs) { return new Thunk<Integer>() { public Integer compute() {
    return xs.eval().isNil()
      ? 0
      : 1 + len(xs.eval().tail).eval();
  }; }; }

  static Thunk<Double> avg(final Thunk<LList<Double>> xs) { return new Thunk<Double>() { public Double compute() {
    final Integer n = len(xs).eval();
    return n == 0
      ? 0.0
      : sum(xs).eval() / n;
  }; }; }

  static <A> Thunk<A> elemAt(Thunk<Integer> ix, Thunk<LList<A>> xs) {
    return ix.eval() == 0
      ? xs.eval().head
      : elemAt(Thunk.ready(ix.eval() - 1), xs.eval().tail);
  }

  static <A> Thunk<LList<A>> take(final Thunk<Integer> n, final Thunk<LList<A>> xs) { return new Thunk<LList<A>>() { public LList<A> compute() {
    return n.eval() == 0
      ? new LList<A>(null, null)
      : new LList<A>(xs.eval().head, take(Thunk.ready(n.eval() - 1), xs.eval().tail));
  }; }; }

  static <A, B> Thunk<LList<B>> map(final Lambda<A, B> f, final Thunk<LList<A>> xs) { return new Thunk<LList<B>>() { public LList<B> compute() {
    return xs.eval().isNil()
      ? new LList<B>(null, null)
      : new LList<B>(f.call(xs.eval().head),
                    map(f, xs.eval().tail));
  }; }; }

  static <A> Thunk<LList<A>> filter(final Lambda<A, Boolean> pred,
                                final Thunk<LList<A>> xs) { return new Thunk<LList<A>>() { public LList<A> compute() {
    if (xs.eval().isNil()) {
      return xs.eval();
    } else {
      Thunk<A> head        = xs.eval().head;
      Thunk<LList<A>> tail = xs.eval().tail;
      if (pred.call(head).eval())
        return LList.cons(head, filter(pred, tail)).eval();
      else
        return                  filter(pred, tail) .eval();
    }
  }; }; }

  static <A> void printList(final Thunk<LList<A>> xs) {
    if (xs.eval().isNil())
      System.out.println("[]");
    else {
      final String str = xs.eval().head.toString();
      System.out.print(str + " : ");
      printList(xs.eval().tail);
    }
  }

  static Thunk<Integer> mod(final Thunk<Integer> dividend, final Thunk<Integer> divisor) { return new Thunk<Integer>() { public Integer compute() {
    return dividend.eval() % divisor.eval();
  }; }; }

  static Thunk<Boolean> gt(final Thunk<Integer> a, final Thunk<Integer> b) { return new Thunk<Boolean>() { public Boolean compute() {
    return a.eval() > b.eval();
  }; }; }

  static Thunk<LList<Integer>> sieve(Thunk<LList<Integer>> xs_) { return new Thunk<LList<Integer>>() { public LList<Integer> compute() {
    final Thunk<Integer> p   = xs_.eval().head;
    Thunk<LList<Integer>> xs = xs_.eval().tail;

    Lambda<Integer, Boolean> pred = new Lambda<Integer, Boolean>() { public Thunk<Boolean> call(final Thunk<Integer> x) {
        return gt(mod(x, p), Thunk.ready(0));
      } };

    return LList.cons(p, sieve(filter(pred, xs))).eval();
  }; }; }

  static Thunk<LList<Integer>> primes() {
    return sieve(generate(Thunk.ready(2), incrByI(1)));
  }
  
  static <A> Thunk<LList<A>> generate(final Thunk<A> seed,
                                  final Lambda<A, A> next) { return new Thunk<LList<A>>() { public LList<A> compute() {
    return new LList<A>(seed,
                       new Thunk<LList<A>>() {
                         public LList<A> compute() { return generate(next.call(seed), next).eval(); }
                       });
   } }; }
}

class Unit {
  private Unit() {}
  public static final Unit INST = new Unit();
}
