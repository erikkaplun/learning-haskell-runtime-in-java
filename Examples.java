public final class Examples {
  public static void main(String[] args) {
    IO.putStrLn(Thunk.ready("let's demonstrate generating, mapping and filtering of lists:"));
    Thunk<List<Integer>> nums    = Fn.apply2(List.generate(),
                                             Thunk.ready(0),
                                             Fn.apply(Num.addI(), Thunk.ready(1)));
    Thunk<List<Integer>> dblNums = Fn.apply2(List.map(),
                                             Fn.apply(Num.mulI(), Thunk.ready(2)),
                                             nums);

    Thunk<List<Integer>> evens = Fn.apply2(List.filter(), even(), nums);
    IO.print(Fn.apply2(List.take(), Thunk.ready(3), evens));

    IO.putStrLn(Thunk.ready("let's demonstrate generating an infinite sequence of primes"));
    Thunk<List<Integer>> primes_ = primes();
    Thunk<List<Integer>> doublePrimes = Fn.apply2(List.map(),
                                                  Fn.apply(Num.mulI(), Thunk.ready(2)),
                                                  primes_);
    IO.print(Fn.apply2(List.take(), Thunk.ready(10), doublePrimes));

    /////////////////////
    IO.putStrLn(Thunk.ready("let's demonstrate the if function:"));

    Thunk<Integer> value =
      If.if_(Thunk.ready(true), Thunk.ready(3), Thunk.ready(4));
    IO.print(value);

    /////////////////////
    IO.putStrLn(Thunk.ready("let's build an inifinite, but boring list of lists:"));

    // Although List.cons is a 2-argument function, we only apply it to 1 argument.
    // The result is another 1-argument function, which will be  passed into List.generate:
    Thunk<Fn<List<Integer>, List<Integer>>> prepend1 =
      Fn.apply(List.cons(), Thunk.ready(1));

    Thunk<List<List<Integer>>> listOfList =
      Fn.apply2(List.generate(),
                List.nil(),
                prepend1);

    IO.putStrLn(Fn.apply(List.pretty(),
                         Fn.apply2(List.take(),
                                   Thunk.ready(10),
                                   listOfList)));

    ////////////////////
    IO.putStrLn(Thunk.ready("let's build another inifinite, slightly less boring list of lists:"));
    // [], [0], [0,1], [0,1,2], [0,1,2,3], ...

    Thunk<Fn<Integer, List<Integer>>> takeNNums =
      Thunk.ready(x -> Fn.apply2(List.take(), x, nums));

    Thunk<List<List<Integer>>> numsPrefixes =
      Fn.apply2(List.map(), takeNNums, nums);

    IO.print(Fn.apply2(List.take(), Thunk.ready(10), numsPrefixes));

    /////////////////////
    IO.putStr  (Thunk.ready("let's build another inifinite list, but this "));
    IO.putStrLn(Thunk.ready("time each of its elements is in turn another infinite list:"));
    // [0,1,2...], [1,2,3...], [2,3,4...], [3,4,5...], ...

    Thunk<Fn<Integer, List<Integer>>> incrNumsBy =
      Thunk.ready(x -> Fn.apply2(List.map(), Fn.apply(Num.addI(), x), nums));

    Thunk<List<List<Integer>>> numsIncremented =
      Fn.apply2(List.map(), incrNumsBy, nums);

    // let's take 10 of each infinite nested list, and then 10 of the top-level list.
    IO.print(Fn.apply2(List.take(),
                       Thunk.ready(10),
                       Fn.apply2(List.map(),
                                 Fn.apply(List.take(), Thunk.ready(10)),
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
    final Ref<Thunk<List<Integer>>> fibs = new Ref<Thunk<List<Integer>>>();
    fibs.ref = // there is no way to make the list self-referential any other way
      Fn.apply2(List.cons(),
                Thunk.ready(0),
                Fn.apply2(List.cons(),
                          Thunk.ready(1),
                          Fn.apply3(List.zipWith(),
                                    Num.addI(),
                                    Thunk.lazy(__ -> fibs.ref.eval()),
                                    Fn.apply(List.tail(), Thunk.lazy(__ -> fibs.ref.eval())))));

    // prints [0,1,1,2,3,5,8,13,21,34,55,89,144,233,377,610,987,1597,2584,4181]
    IO.print(Fn.apply2(List.take(),
                       Thunk.ready(20),
                       fibs.ref));

    // prints 102334155
    IO.print(Fn.apply2(List.elemAt(),
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
  Thunk<Fn<List<Double>, Double>>
  sum() { return Thunk.ready(xs ->
    If.if_(Fn.apply(List.isNil(), xs),
           Thunk.ready(0.0),
           Thunk.lazy(__ -> Fn.apply(List.head(), xs).eval() + Fn.apply(sum(), Fn.apply(List.tail(), xs)).eval()))
  ); }

  static
  Thunk<Fn<List<Double>, Double>>
  avg() { return Thunk.ready(xs -> Thunk.lazy(__ ->  {
    final Integer n = Fn.apply(List.len(), xs).eval();
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
  Thunk<List<Integer>>
   primes() {
    Thunk<List<Integer>> intsFrom2 =
      Fn.apply2(List.generate(),
                Thunk.ready(2),
                Fn.apply(Num.addI(),
                         Thunk.ready(1)));

    return Fn.apply(sieve(),
                    intsFrom2);
  }
  static
  Thunk<Fn<List<Integer>, List<Integer>>>
  sieve() { return Thunk.ready(xs_ -> {
    Thunk<Integer>        p = Fn.apply(List.head(), xs_);
    Thunk<List<Integer>> xs = Fn.apply(List.tail(), xs_);

    Thunk<Fn<Integer, Boolean>> pred =
      Thunk.ready(x -> Fn.apply2(gt(),
                                 Fn.apply2(mod(), x, p),
                                 Thunk.ready(0)));

    return Fn.apply2(List.cons(),
                     p,
                     Fn.apply(sieve(),
                              Fn.apply2(List.filter(), pred, xs)));
  }); }
}

class Unit {
  private Unit() {}
  public static final Unit INST = new Unit();
}
