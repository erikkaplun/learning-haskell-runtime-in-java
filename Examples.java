public class Examples {
  public static void main(String[] args) {
    IO.putStrLn(Thunk.ready("let's demonstrate generating, mapping and filtering of lists:"));
    Thunk<LList<Integer>> nums    = Fn.apply2(LList.generate(), 
                                              Thunk.ready(0),
                                              Fn.apply(Num.addI(), Thunk.ready(1)));
    Thunk<LList<Integer>> dblNums = Fn.apply2(LList.map(),
                                              Fn.apply(Num.mulI(), Thunk.ready(2)),
                                              nums);

    Thunk<LList<Integer>> evens = Fn.apply2(LList.filter(), even(), nums);
    IO.print(Fn.apply2(LList.take(), Thunk.ready(3), evens));

    IO.putStrLn(Thunk.ready("let's demonstrate generating an infinite sequence of primes"));
    Thunk<LList<Integer>> primes_ = primes();
    Thunk<LList<Integer>> doublePrimes = Fn.apply2(LList.map(),
                                                   Fn.apply(Num.mulI(), Thunk.ready(2)),
                                                   primes_);
    IO.print(Fn.apply2(LList.take(), Thunk.ready(10), doublePrimes));

    /////////////////////
    IO.putStrLn(Thunk.ready("let's demonstrate the if function:"));

    Thunk<Integer> value =
      If.if_(Thunk.ready(true), Thunk.ready(3), Thunk.ready(4));
    IO.print(value);

    /////////////////////
    IO.putStrLn(Thunk.ready("let's build an inifinite, but boring list of lists:"));

    // Although LList.cons is a 2-argument function, we only apply it to 1 argument.
    // The result is another 1-argument function, which will be  passed into LList.generate:
    Thunk<Fn<LList<Integer>, LList<Integer>>> prepend1 =
      Fn.apply(LList.cons(), Thunk.ready(1));

    Thunk<LList<LList<Integer>>> listOfList =
      Fn.apply2(LList.generate(), 
                LList.nil(),
                prepend1);

    IO.putStrLn(Fn.apply(LList.pretty(),
                         Fn.apply2(LList.take(),
                                   Thunk.ready(10),
                                   listOfList)));

    ////////////////////
    IO.putStrLn(Thunk.ready("let's build another inifinite, slightly less boring list of lists:"));
    // [], [0], [0,1], [0,1,2], [0,1,2,3], ...

    Thunk<Fn<Integer, LList<Integer>>> takeNNums =
      Thunk.ready(x -> Fn.apply2(LList.take(), x, nums));

    Thunk<LList<LList<Integer>>> numsPrefixes =
      Fn.apply2(LList.map(), takeNNums, nums);

    IO.print(Fn.apply2(LList.take(), Thunk.ready(10), numsPrefixes));

    /////////////////////
    IO.putStr  (Thunk.ready("let's build another inifinite list, but this "));
    IO.putStrLn(Thunk.ready("time each of its elements is in turn another infinite list:"));
    // [0,1,2...], [1,2,3...], [2,3,4...], [3,4,5...], ...

    Thunk<Fn<Integer, LList<Integer>>> incrNumsBy =
      Thunk.ready(x -> Fn.apply2(LList.map(), Fn.apply(Num.addI(), x), nums));

    Thunk<LList<LList<Integer>>> numsIncremented =
      Fn.apply2(LList.map(), incrNumsBy, nums);

    // let's take 10 of each infinite nested list, and then 10 of the top-level list.
    IO.print(Fn.apply2(LList.take(),
                       Thunk.ready(10),
                       Fn.apply2(LList.map(),
                                Fn.apply(LList.take(), Thunk.ready(10)),
                                numsIncremented)));

    // Let's build the infinite list of all Fibonacci numbers.
    // We use a list that is recursively defined on itself:
    // * the first 2 elements in the list are predefined (and we could
    //   predefine more, but 2 is the minimum)
    // * the 3rd element is computed from the first 2
    // * the 4th element is computed from the 2nd and 3rd, etc.
    //
    // This works thanks to laziness: by the time we want the nth value,
    // the (n-1)th and (n-2)th are already available.
    // Technically, what happens is that the list gets layered on top
    // of itself, but with one element eliminated from the beginning.
    // This way, the nth element is matched with the (n-1)th element.
    // So we obtain a list of pairs. The sum of a pair containing
    // elements (n-1) and (n-2) is the value of the element n.
    //
    // The Haskell code it imitates is:
    //
    //     fibs = 0 : 1 : zipWith (+) fibs
    //
    final Ref<Thunk<LList<Integer>>> fibs = new Ref<Thunk<LList<Integer>>>();
    fibs.ref = // there is no way to make the list self-referential any other way
      Fn.apply2(LList.cons(),
                Thunk.ready(0),
                Fn.apply2(LList.cons(),
                          Thunk.ready(1),
                          Fn.apply3(LList.zipWith(),
                                    Num.addI(),
                                    Thunk.lazy(__ -> fibs.ref.eval()),
                                    Fn.apply(LList.tail(), Thunk.lazy(__ -> fibs.ref.eval())))));

    // prints [0,1,1,2,3,5,8,13,21,34,55,89,144,233,377,610,987,1597,2584,4181]
    IO.print(Fn.apply2(LList.take(),
                       Thunk.ready(20),
                       fibs.ref));

    // prints 102334155
    IO.print(Fn.apply2(LList.elemAt(),
                       // we could go as high as 150000 or higher, but
                       // we'd need to switch to using BigInt, as well as
                       // increasing the JVM stack size to 16MB and higher
                       Thunk.ready(40),
                       fibs.ref));
  };

  static class Ref<A> { public A ref = null; }

  static Thunk<Fn<Integer, Boolean>>
  even() { return Thunk.ready(i ->
    Fn.apply2(eq(),
              Fn.apply2(mod(), i, Thunk.ready(2)),
              Thunk.ready(0))
  ); }

  static
  Thunk<Fn<Integer, Fn<Integer, Boolean>>>
  eq() { return Thunk.ready(a -> Thunk.ready(b -> Thunk.lazy(__ ->
    a.eval() == b.eval()
  ))); }

  static
  Thunk<Fn<LList<Double>, Double>>
  sum() { return Thunk.ready(xs ->
    If.if_(Fn.apply(LList.isNil(), xs),
           Thunk.ready(0.0),
           Thunk.lazy(__ -> Fn.apply(LList.head(), xs).eval() + Fn.apply(sum(), Fn.apply(LList.tail(), xs)).eval()))
  ); }

  static
  Thunk<Fn<LList<Double>, Double>>
  avg() { return Thunk.ready(xs -> Thunk.lazy(__ ->  {
    final Integer n = Fn.apply(LList.len(), xs).eval();
    return n == 0
      ? 0.0
      : Fn.apply(sum(), xs).eval() / n;
  })); }

  static
  Thunk<Fn<Integer, Fn<Integer, Integer>>>
  mod() { return Thunk.ready(dividend -> Thunk.ready(divisor -> Thunk.lazy(__ ->
    dividend.eval() % divisor.eval()
  ))); }

  static
  Thunk<Fn<Integer, Fn<Integer, Boolean>>>
  gt() { return Thunk.ready(a -> Thunk.ready(b -> Thunk.lazy(__ ->
    a.eval() > b.eval()
  ))); }

  static
  Thunk<LList<Integer>>
   primes() {
    Thunk<LList<Integer>> intsFrom2 =
      Fn.apply2(LList.generate(),
                Thunk.ready(2),
                Fn.apply(Num.addI(),
                         Thunk.ready(1)));

    return Fn.apply(sieve(),
                    intsFrom2);
  }
  static
  Thunk<Fn<LList<Integer>, LList<Integer>>>
  sieve() { return Thunk.ready(xs_ -> {
    Thunk<Integer>        p  = Fn.apply(LList.head(), xs_);
    Thunk<LList<Integer>> xs = Fn.apply(LList.tail(), xs_);

    Thunk<Fn<Integer, Boolean>> pred =
      Thunk.ready(x -> Fn.apply2(gt(),
                                 Fn.apply2(mod(), x, p),
                                 Thunk.ready(0)));

    return Fn.apply2(LList.cons(),
                     p,
                     Fn.apply(sieve(),
                              Fn.apply2(LList.filter(), pred, xs)));
  }); }
}

class Unit {
  private Unit() {}
  public static final Unit INST = new Unit();
}
