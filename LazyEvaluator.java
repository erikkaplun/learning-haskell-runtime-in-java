abstract class Thunk<A> {
  private A value = null;

  public abstract A compute();

  public final A e() {
    if (this.value != null) {
      return this.value;
    } else {
      this.value = this.compute();
      // System.out.print("c");
      return value;
    }
  }

  public String toString() {
    return this.e().toString();
  }
  
  public static <A> Thunk<A> s(final A value) {
    return new Thunk<A>() { public A compute() { return value; } };
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

  public final boolean isEmpty() { return head == null; }

  public String toString() {
    return head != null
      ? head.toString() + " : " + tail.toString();
      : "[]";
  }

  public static <A> Thunk<LList<A>> empty() { return Thunk.s(new LList<A>(null, null)); }
  public static <A> Thunk<LList<A>> cons(final Thunk<A> head, final Thunk<LList<A>> tail) { return new Thunk<LList<A>>() { public LList<A> compute() {
    return new LList<A>(head, tail);
  }; }; }
}

public class LazyEvaluator {
  static Lambda<Double, Double> incrByD(final double d) {
    return new Lambda<Double, Double>() { public Thunk<Double> call(final Thunk<Double> arg) { return Thunk.s(arg.e() + d); } };
  }
  static Lambda<Double, Double> mulByD(final double factor) {
    return new Lambda<Double, Double>() { public Thunk<Double> call(final Thunk<Double> arg) { return Thunk.s(arg.e() * factor); } };
  }
  static Lambda<Integer, Integer> incrByI(final int d) {
    return new Lambda<Integer, Integer>() { public Thunk<Integer> call(final Thunk<Integer> arg) { return Thunk.s(arg.e() + d); } };
  }
  static Lambda<Integer, Integer> mulByI(final int factor) {
    return new Lambda<Integer, Integer>() { public Thunk<Integer> call(final Thunk<Integer> arg) { return Thunk.s(arg.e() * factor); } };
  }

  public static void main(String[] args) {
    // Thunk<LList<Double>> nums1 = generate(Thunk.s(0.0), incrByD(1.0));
    // Thunk<LList<Double>> nums2 = map(mulByD(1.0), nums1);
    // Thunk<LList<Double>> nums2first4 = take(Thunk.s(4), nums2);
    // Thunk<LList<Double>> nums2first5 = take(Thunk.s(5), nums2);
    // System.out.println("-----");

    // // printList(nums2first4);
    // printList(nums2first4);
    // printList(nums2first5);
    // System.out.println(sum(nums2first4));
    // System.out.println(len(nums2first4));
    // System.out.println(avg(nums2first4));

    /////////////////////////////////

    Lambda<Integer, Boolean> isEven = new Lambda<Integer, Boolean>() { public Thunk<Boolean> call(Thunk<Integer> x) { return even(x); } };

    Thunk<LList<Integer>> nums = generate(Thunk.s(0), incrByI(1));
    Thunk<LList<Integer>> evens = filter(isEven, nums);
    System.out.println(take(Thunk.s(3), evens));

    Thunk<LList<Integer>> primes_ = primes();
    Thunk<LList<Integer>> evenPrimes = filter(isEven, primes_);
    printList(take(Thunk.s(10), evenPrimes));
  };

  static Thunk<Boolean> even(final Thunk<Integer> i) {
    return eq(mod(i, Thunk.s(2)), Thunk.s(0));
  }

  static Thunk<Boolean> eq(final Thunk<Integer> a, final Thunk<Integer> b) { return new Thunk<Boolean>() { public Boolean compute() {
    return a.e() == b.e();
  }; }; }

  static Thunk<Double> sum(final Thunk<LList<Double>> xs) { return new Thunk<Double>() { public Double compute() {
    return xs.e().isEmpty()
      ? 0.0
      : xs.e().head.e() + sum(xs.e().tail).e();
  }; }; }

  static <A> Thunk<Integer> len(final Thunk<LList<A>> xs) { return new Thunk<Integer>() { public Integer compute() {
    return xs.e().isEmpty()
      ? 0
      : 1 + len(xs.e().tail).e();
  }; }; }

  static Thunk<Double> avg(final Thunk<LList<Double>> xs) { return new Thunk<Double>() { public Double compute() {
    final Integer n = len(xs).e();
    return n == 0
      ? 0.0
      : sum(xs).e() / n;
  }; }; }

  static <A> Thunk<A> elemAt(Thunk<Integer> ix, Thunk<LList<A>> xs) {
    return ix.e() == 0
      ? xs.e().head
      : elemAt(Thunk.s(ix.e() - 1), xs.e().tail);
  }

  static <A> Thunk<LList<A>> take(final Thunk<Integer> n, final Thunk<LList<A>> xs) { return new Thunk<LList<A>>() { public LList<A> compute() {
    System.out.println("take " + n + " xs");
    return n.e() == 0
      ? new LList<A>(null, null)
      : new LList<A>(xs.e().head, take(Thunk.s(n.e() - 1), xs.e().tail));
  }; }; }

  static <A, B> Thunk<LList<B>> map(final Lambda<A, B> f, final Thunk<LList<A>> xs) { return new Thunk<LList<B>>() { public LList<B> compute() {
    return xs.e().isEmpty()
      ? new LList<B>(null, null)
      : new LList<B>(f.call(xs.e().head),
                    map(f, xs.e().tail));
  }; }; }

  static <A> Thunk<LList<A>> filter(final Lambda<A, Boolean> pred,
                                final Thunk<LList<A>> xs) { return new Thunk<LList<A>>() { public LList<A> compute() {
    if (xs.e().isEmpty()) {
      return xs.e();
    } else {
      Thunk<A> head        = xs.e().head;
      Thunk<LList<A>> tail = xs.e().tail;
      if (pred.call(head).e())
        return LList.cons(head, filter(pred, tail)).e();
      else
        return                  filter(pred, tail) .e();
    }
  }; }; }

  static <A> void printList(final Thunk<LList<A>> xs) {
    if (xs.e().isEmpty())
      System.out.println("[]");
    else {
      final String str = xs.e().head.toString();
      System.out.print(str + " : ");
      printList(xs.e().tail);
    }
  }

  static Thunk<Integer> mod(final Thunk<Integer> dividend, final Thunk<Integer> divisor) { return new Thunk<Integer>() { public Integer compute() {
    return dividend.e() % divisor.e();
  }; }; }

  static Thunk<Boolean> gt(final Thunk<Integer> a, final Thunk<Integer> b) { return new Thunk<Boolean>() { public Boolean compute() {
    return a.e() > b.e();
  }; }; }

  static Thunk<LList<Integer>> sieve(Thunk<LList<Integer>> xs_) {
    final Thunk<Integer> p = xs_.e().head; Thunk<LList<Integer>> xs = xs_.e().tail;

    Lambda<Integer, Boolean> pred = new Lambda<Integer, Boolean>() {
        public Thunk<Boolean> call(final Thunk<Integer> x) {
          Thunk<Boolean> ret = gt(mod(x, p), Thunk.s(0));
          System.out.println("gt(mod(" + x + ", " + p + "), Thunk.s(0)) => " + ret);
          return ret;
        }
      };
    System.out.println("cons(" + p + ", sieve(filter(pred, xs))");
    return LList.cons(p, sieve(filter(pred, xs)));
  }

  static Thunk<LList<Integer>> primes() { return sieve(generate(Thunk.s(2), incrByI(1))); }
  
  static <A> Thunk<LList<A>> generate(final Thunk<A> seed,
                                  final Lambda<A, A> next) { return new Thunk<LList<A>>() { public LList<A> compute() {
    return new LList<A>(seed,
                       new Thunk<LList<A>>() {
                         public LList<A> compute() { return generate(next.call(seed), next).e(); }
                       });
   } }; }
}

class Unit {
  private Unit() {}
  public static final Unit INST = new Unit();
}
