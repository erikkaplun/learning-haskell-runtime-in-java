import prelude.*;

import static prelude.List.*;
import static prelude.Fn.*;
import static prelude.Thunk.*;
import static prelude.Num.*;
import static prelude.If.*;

public final class Examples {
  public static void main(String[] args) {
    IO.putStrLn(ready("let's demonstrate generating, mapping and filtering of lists:"));
    Thunk<List<Integer>> nums    = apply2(generate(),
                                          ready(0),
                                          apply(addI(), ready(1)));
    Thunk<List<Integer>> dblNums = apply2(map(),
                                          apply(mulI(), ready(2)),
                                          nums);

    Thunk<List<Integer>> evens = apply2(filter(), even(), nums);
    IO.print(apply2(take(), ready(3), evens));

    IO.putStrLn(ready("let's demonstrate generating an infinite sequence of primes"));
    Thunk<List<Integer>> primes_ = primes();
    Thunk<List<Integer>> doublePrimes = apply2(map(),
                                               apply(mulI(), ready(2)),
                                               primes_);
    IO.print(apply2(take(), ready(10), doublePrimes));

    /////////////////////
    IO.putStrLn(ready("let's demonstrate the if function:"));

    Thunk<Integer> value = if_(ready(true), ready(3), ready(4));
    IO.print(value);

    /////////////////////
    IO.putStrLn(ready("let's build an inifinite, but boring list of lists:"));

    // Although cons is a 2-argument function, we only apply it to 1 argument.
    // The result is another 1-argument function, which will be  passed into generate:
    Thunk<Fn<List<Integer>, List<Integer>>> prepend1 =
      apply(cons(), ready(1));

    Thunk<List<List<Integer>>> listOfList =
      apply2(generate(),
             nil(),
             prepend1);

    IO.putStrLn(apply(pretty(),
                      apply2(take(),
                             ready(10),
                             listOfList)));

    ////////////////////
    IO.putStrLn(ready("let's build another inifinite, slightly less boring list of lists:"));
    // [], [0], [0,1], [0,1,2], [0,1,2,3], ...

    Thunk<Fn<Integer, List<Integer>>> takeNNums =
      ready(x -> apply2(take(), x, nums));

    Thunk<List<List<Integer>>> numsPrefixes =
      apply2(map(), takeNNums, nums);

    IO.print(apply2(take(), ready(10), numsPrefixes));

    /////////////////////
    IO.putStr  (ready("let's build another inifinite list, but this "));
    IO.putStrLn(ready("time each of its elements is in turn another infinite list:"));
    // [0,1,2...], [1,2,3...], [2,3,4...], [3,4,5...], ...

    Thunk<Fn<Integer, List<Integer>>> incrNumsBy =
      ready(x -> apply2(map(), apply(addI(), x), nums));

    Thunk<List<List<Integer>>> numsIncremented =
      apply2(map(), incrNumsBy, nums);

    // let's take 10 of each infinite nested list, and then 10 of the top-level list.
    IO.print(apply2(take(),
                    ready(10),
                    apply2(map(),
                           apply(take(), ready(10)),
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
      apply2(cons(),
             ready(0),
             apply2(cons(),
                    ready(1),
                    apply3(zipWith(),
                           addI(),
                           lazy(__ -> fibs.ref.eval()),
                           apply(tail(), lazy(__ -> fibs.ref.eval())))));

    // prints [0,1,1,2,3,5,8,13,21,34,55,89,144,233,377,610,987,1597,2584,4181]
    IO.print(apply2(take(),
                    ready(20),
                    fibs.ref));

    // prints 102334155
    IO.print(apply2(elemAt(),
                    // we could go as high as 150000 or higher, but
                    // we'd need to switch to using BigInt, as well as
                    // increasing the JVM stack size to 16MB and higher
                    ready(40),
                    fibs.ref));
  };

  static class Ref<A> { public A ref = null; }

  static Thunk<Fn<Integer, Boolean>>
  even() { return ready(i ->
    apply2(eq(),
           apply2(mod(), i, ready(2)),
           ready(0))
  ); }

  static
  Thunk<Fn<Integer, Fn<Integer, Boolean>>>
  eq() { return ready(a -> ready(b -> lazy(__ ->
    a.eval() == b.eval()
  ))); }

  static
  Thunk<Fn<List<Double>, Double>>
  sum() { return ready(xs ->
    if_(apply(isNil(), xs),
        ready(0.0),
        lazy(__ -> apply(head(), xs).eval() + apply(sum(), apply(tail(), xs)).eval()))
  ); }

  static
  Thunk<Fn<List<Double>, Double>>
  avg() { return ready(xs -> lazy(__ ->  {
    final Integer n = apply(len(), xs).eval();
    return n == 0
      ? 0.0
      : apply(sum(), xs).eval() / n;
  })); }

  static
  Thunk<Fn<Integer, Fn<Integer, Integer>>>
  mod() { return ready(dividend -> ready(divisor -> lazy(__ ->
    dividend.eval() % divisor.eval()
  ))); }

  static
  Thunk<Fn<Integer, Fn<Integer, Boolean>>>
  gt() { return ready(a -> ready(b -> lazy(__ ->
    a.eval() > b.eval()
  ))); }

  static
  Thunk<List<Integer>>
   primes() {
    Thunk<List<Integer>> intsFrom2 =
      apply2(generate(),
             ready(2),
             apply(addI(),
                   ready(1)));

    return apply(sieve(),
                 intsFrom2);
  }
  static
  Thunk<Fn<List<Integer>, List<Integer>>>
  sieve() { return ready(xs_ -> {
    Thunk<Integer>        p = apply(head(), xs_);
    Thunk<List<Integer>> xs = apply(tail(), xs_);

    Thunk<Fn<Integer, Boolean>> pred =
      ready(x -> apply2(gt(),
                        apply2(mod(), x, p),
                        ready(0)));

    return apply2(cons(),
                  p,
                  apply(sieve(),
                        apply2(filter(), pred, xs)));
  }); }
}

class Unit {
  private Unit() {}
  public static final Unit INST = new Unit();
}
