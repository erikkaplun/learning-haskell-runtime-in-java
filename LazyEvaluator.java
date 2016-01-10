abstract class S<A> {
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
  
  public static <A> S<A> s(final A value) {
    return new S<A>() { public A compute() { return value; } };
  }
}

abstract class Lambda<ArgT, RetT> {
  public abstract S<RetT> call(final S<ArgT> arg);
}

class LList<A> {
  public final S<A>       head;
  public final S<LList<A>> tail;
  LList(S<A> head, S<LList<A>> tail) { this.head = head; this.tail = tail; }

  public final boolean isEmpty() { return this.head == null; }

  public String toString() {
    if (head != null)
      return head.toString() + " : " + tail.toString();
    else
      return "[]";
  }

  public static <A> S<LList<A>> empty() { return S.s(new LList<A>(null, null)); }
  public static <A> S<LList<A>> cons(final S<A> head, final S<LList<A>> tail) { return new S<LList<A>>() { public LList<A> compute() {
    return new LList<A>(head, tail);
  }; }; }
}

public class LazyEvaluator {
  static Lambda<Double, Double> incrByD(final double d) {
    return new Lambda<Double, Double>() { public S<Double> call(final S<Double> arg) { return S.s(arg.e() + d); } };
  }
  static Lambda<Double, Double> mulByD(final double factor) {
    return new Lambda<Double, Double>() { public S<Double> call(final S<Double> arg) { return S.s(arg.e() * factor); } };
  }
  static Lambda<Integer, Integer> incrByI(final int d) {
    return new Lambda<Integer, Integer>() { public S<Integer> call(final S<Integer> arg) { return S.s(arg.e() + d); } };
  }
  static Lambda<Integer, Integer> mulByI(final int factor) {
    return new Lambda<Integer, Integer>() { public S<Integer> call(final S<Integer> arg) { return S.s(arg.e() * factor); } };
  }

  public static void main(String[] args) {
    // S<LList<Double>> nums1 = generate(S.s(0.0), incrByD(1.0));
    // S<LList<Double>> nums2 = map(mulByD(1.0), nums1);
    // S<LList<Double>> nums2first4 = take(S.s(4), nums2);
    // S<LList<Double>> nums2first5 = take(S.s(5), nums2);
    // System.out.println("-----");

    // // printList(nums2first4);
    // printList(nums2first4);
    // printList(nums2first5);
    // System.out.println(sum(nums2first4));
    // System.out.println(len(nums2first4));
    // System.out.println(avg(nums2first4));

    /////////////////////////////////

    Lambda<Integer, Boolean> isEven = new Lambda<Integer, Boolean>() { public S<Boolean> call(S<Integer> x) { return even(x); } };

    S<LList<Integer>> nums = generate(S.s(0), incrByI(1));
    S<LList<Integer>> evens = filter(isEven, nums);
    System.out.println(take(S.s(3), evens));

    S<LList<Integer>> primes_ = primes();
    S<LList<Integer>> evenPrimes = filter(isEven, primes_);
    printList(take(S.s(10), evenPrimes));
  };

  static S<Boolean> even(final S<Integer> i) {
    return eq(mod(i, S.s(2)), S.s(0));
  }

  static S<Boolean> eq(final S<Integer> a, final S<Integer> b) { return new S<Boolean>() { public Boolean compute() {
    return a.e() == b.e();
  }; }; }

  static S<Double> sum(final S<LList<Double>> xs) { return new S<Double>() { public Double compute() {
    return xs.e().isEmpty()
      ? 0.0
      : xs.e().head.e() + sum(xs.e().tail).e();
  }; }; }

  static <A> S<Integer> len(final S<LList<A>> xs) { return new S<Integer>() { public Integer compute() {
    return xs.e().isEmpty()
      ? 0
      : 1 + len(xs.e().tail).e();
  }; }; }

  static S<Double> avg(final S<LList<Double>> xs) { return new S<Double>() { public Double compute() {
    final Integer n = len(xs).e();
    return n == 0
      ? 0.0
      : sum(xs).e() / n;
  }; }; }

  static <A> S<A> elemAt(S<Integer> ix, S<LList<A>> xs) {
    return ix.e() == 0
      ? xs.e().head
      : elemAt(S.s(ix.e() - 1), xs.e().tail);
  }

  static <A> S<LList<A>> take(final S<Integer> n, final S<LList<A>> xs) { return new S<LList<A>>() { public LList<A> compute() {
    System.out.println("take " + n + " xs");
    return n.e() == 0
      ? new LList<A>(null, null)
      : new LList<A>(xs.e().head, take(S.s(n.e() - 1), xs.e().tail));
  }; }; }

  static <A, B> S<LList<B>> map(final Lambda<A, B> f, final S<LList<A>> xs) { return new S<LList<B>>() { public LList<B> compute() {
    return xs.e().isEmpty()
      ? new LList<B>(null, null)
      : new LList<B>(f.call(xs.e().head),
                    map(f, xs.e().tail));
  }; }; }

  static <A> S<LList<A>> filter(final Lambda<A, Boolean> pred,
                                final S<LList<A>> xs) { return new S<LList<A>>() { public LList<A> compute() {
    if (xs.e().isEmpty()) {
      return xs.e();
    } else {
      S<A> head        = xs.e().head;
      S<LList<A>> tail = xs.e().tail;
      if (pred.call(head).e())
        return LList.cons(head, filter(pred, tail)).e();
      else
        return                  filter(pred, tail) .e();
    }
  }; }; }

  static <A> void printList(final S<LList<A>> xs) {
    if (xs.e().isEmpty())
      System.out.println("[]");
    else {
      final String str = xs.e().head.toString();
      System.out.print(str + " : ");
      printList(xs.e().tail);
    }
  }

  static S<Integer> mod(final S<Integer> dividend, final S<Integer> divisor) { return new S<Integer>() { public Integer compute() {
    return dividend.e() % divisor.e();
  }; }; }

  static S<Boolean> gt(final S<Integer> a, final S<Integer> b) { return new S<Boolean>() { public Boolean compute() {
    return a.e() > b.e();
  }; }; }

  static S<LList<Integer>> sieve(S<LList<Integer>> xs_) {
    final S<Integer> p = xs_.e().head; S<LList<Integer>> xs = xs_.e().tail;

    Lambda<Integer, Boolean> pred = new Lambda<Integer, Boolean>() {
        public S<Boolean> call(final S<Integer> x) {
          S<Boolean> ret = gt(mod(x, p), S.s(0));
          System.out.println("gt(mod(" + x + ", " + p + "), S.s(0)) => " + ret);
          return ret;
        }
      };
    System.out.println("cons(" + p + ", sieve(filter(pred, xs))");
    return LList.cons(p, sieve(filter(pred, xs)));
  }

  static S<LList<Integer>> primes() { return sieve(generate(S.s(2), incrByI(1))); }
  
  static <A> S<LList<A>> generate(final S<A> seed,
                                  final Lambda<A, A> next) { return new S<LList<A>>() { public LList<A> compute() {
    return new LList<A>(seed,
                       new S<LList<A>>() {
                         public LList<A> compute() { return generate(next.call(seed), next).e(); }
                       });
   } }; }
}

class Unit {
  private Unit() {}
  public static final Unit INST = new Unit();
}
