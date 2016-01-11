public class LazyEvaluator {
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

  static Fn<Double, Double> incrByD(final double d) { return arg -> Thunk.ready(arg.eval() + d); }
  static Fn<Double, Double> mulByD(final double factor) { return arg -> Thunk.ready(arg.eval() * factor); }
  static Fn<Integer, Integer> incrByI(final int d) { return arg ->Thunk.ready(arg.eval() + d); }
  static Thunk<Fn<Integer, Integer>> mulByI(final int factor) { return Thunk.ready(arg -> Thunk.ready(arg.eval() * factor)); }

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

  static Thunk<LList<Integer>> primes() {
    return sieve(LList.generate(Thunk.ready(2), incrByI(1)));
  }
  static Thunk<LList<Integer>> sieve(Thunk<LList<Integer>> xs_) { return Thunk.lazy(__ -> {
    final Thunk<Integer>  p  = LList.head(xs_);
    Thunk<LList<Integer>> xs = LList.tail(xs_);

    Thunk<Fn<Integer, Boolean>> pred = Thunk.ready(x -> gt(mod(x, p), Thunk.ready(0)));

    return LList.cons(p, sieve(LList.filter(pred, xs))).eval();
  }); }
}

class Unit {
  private Unit() {}
  public static final Unit INST = new Unit();
}
