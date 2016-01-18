import prelude.*;

import static prelude.List.*;
import static prelude.Fn.*;
import static prelude.Thunk.*;
import static prelude.Num.*;
import static prelude.If.*;

public final class Examples {
  public static void main(String[] args) {
    IO.putStrLn(thunk("let's demonstrate generating, mapping and filtering of lists:"));
    Thunk<List<Integer>> nums    = apply2(iterate(),
                                          thunk(0),
                                          infixL(thunk(1), addI()));
    Thunk<List<Integer>> dblNums = apply2(map(),
                                          infixL(thunk(2), mulI()),
                                          nums);

    Thunk<List<Integer>> evens = apply2(filter(), even(), nums);
    IO.print(apply2(take(), thunk(3), evens));

    IO.putStrLn(thunk("let's demonstrate generating an infinite sequence of primes"));
    Thunk<List<Integer>> primes_ = primes();
    Thunk<List<Integer>> doublePrimes = apply2(map(),
                                               infixL(thunk(2), mulI()),
                                               primes_);
    IO.print(apply2(take(), thunk(10), doublePrimes));

    /////////////////////
    IO.putStrLn(thunk("let's demonstrate the if function:"));

    Thunk<Integer> value = if_(thunk(true), thunk(3), thunk(4));
    IO.print(value);

    /////////////////////
    IO.putStrLn(thunk("let's build an inifinite, but boring list of lists:"));

    // Although cons is a 2-argument function, we only apply it to 1 argument.
    // The result is another 1-argument function, which will be  passed into iterate:
    Thunk<Fn<List<Integer>, List<Integer>>> prepend1 =
      infixL(thunk(1), cons());

    Thunk<List<List<Integer>>> listOfList =
      apply2(iterate(),
             nil(),
             prepend1);

    IO.putStrLn(apply(pretty(),
                      apply2(take(),
                             thunk(10),
                             listOfList)));

    ////////////////////
    IO.putStrLn(thunk("let's build another inifinite, slightly less boring list of lists:"));
    // [], [0], [0,1], [0,1,2], [0,1,2,3], ...

    Thunk<Fn<Integer, List<Integer>>> takeNNums =
      fn(x -> apply2(take(), x, nums));

    Thunk<List<List<Integer>>> numsPrefixes =
      apply2(map(), takeNNums, nums);

    IO.print(apply2(take(), thunk(10), numsPrefixes));

    /////////////////////
    IO.putStr  (thunk("let's build another inifinite list, but this "));
    IO.putStrLn(thunk("time each of its elements is in turn another infinite list:"));
    // [0,1,2...], [1,2,3...], [2,3,4...], [3,4,5...], ...

    Thunk<Fn<Integer, List<Integer>>> incrNumsBy =
      fn(x -> apply2(map(), apply(addI(), x), nums));

    Thunk<List<List<Integer>>> numsIncremented =
      apply2(map(), incrNumsBy, nums);

    // let's take 10 of each infinite nested list, and then 10 of the top-level list.
    IO.print(apply2(take(),
                    thunk(10),
                    apply2(map(),
                           apply(take(), thunk(10)),
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
    //     fibs = 0 : 1 : zipWith (+) fibs (tail fibs)
    //
    final Ref<Thunk<List<Integer>>> fibs = new Ref<Thunk<List<Integer>>>();
    fibs.ref = // there is no way to make the list self-referential any other way
      infix(thunk(0),
            cons(),
            infix(thunk(1),
                  cons(),
                  apply3(zipWith(),
                         addI(),
                         thunk(__ -> fibs.ref.eval()),
                         apply(tail(), thunk(__ -> fibs.ref.eval())))));

    // prints [0,1,1,2,3,5,8,13,21,34,55,89,144,233,377,610,987,1597,2584,4181]
    IO.print(apply2(take(),
                    thunk(20),
                    fibs.ref));

    // prints 102334155
    IO.print(apply2(elemAt(),
                    // we could go as high as 150000 or higher, but
                    // we'd need to switch to using BigInt, as well as
                    // increasing the JVM stack size to 16MB and higher
                    thunk(40),
                    fibs.ref));

    // prints 4.5
    IO.print(apply(avg(), apply2(take(), thunk(10),
                                 apply2(map(), fromInteger(), nums))));
  };

  static class Ref<A> { public A ref = null; }

  static Thunk<Fn<Integer, Boolean>>
  even() { return fn(i ->
    apply2(eq(),
           apply2(mod(), i, thunk(2)),
           thunk(0))
  ); }

  static
  Thunk<Fn<Integer, Fn<Integer, Boolean>>>
  eq() { return fn(a -> fn(b -> thunk(__ ->
    a.eval() == b.eval()
  ))); }

  static
  Thunk<Fn<List<Double>, Double>>
  sum() { return fn(xs ->
    if_(apply(isNil(), xs),
        thunk(0.0),
        infix(apply(head(), xs),
              Num.addD(),
              apply(sum(), apply(tail(), xs))))
  ); }

  static
  Thunk<Fn<List<Double>, Double>>
  avg() { return fn(xs -> {
    Thunk<Integer> n = apply(len(), xs);
    return if_(infix(thunk(0), eq(), n),
               thunk(0.0),
               infix(apply(sum(), xs),
                     divD(),
                     apply(fromInteger(), n)));
  }); }

  static
  Thunk<Fn<Integer, Fn<Integer, Integer>>>
  mod() { return fn(dividend -> fn(divisor -> lazy(__ ->
    dividend.eval() % divisor.eval()
  ))); }

  static
  Thunk<Fn<Integer, Fn<Integer, Boolean>>>
  gt() { return fn(a -> fn(b -> thunk(__ ->
    a.eval() > b.eval()
  ))); }

  static
  Thunk<List<Integer>>
   primes() {
    Thunk<List<Integer>>
    intsFrom2 = apply2(iterate(), thunk(2), apply(addI(), thunk(1)));
    return apply(sieve(), intsFrom2);
  }
  static
  Thunk<Fn<List<Integer>, List<Integer>>>
  sieve() { return fn(xs_ -> {
    Thunk<Integer>        p = apply(head(), xs_);
    Thunk<List<Integer>> xs = apply(tail(), xs_);

    Thunk<Fn<Integer, Boolean>> pred =
      fn(x -> infix(infix(x, mod(), p), gt(), thunk(0)));

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
