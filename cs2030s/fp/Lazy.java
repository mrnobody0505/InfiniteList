package cs2030s.fp;

/**
 * This class implements an abstraction that supports lazy values.
 *
 * @param <T> Type parameter
 * @author Do Gia Hien
 * @version CS2030S AY22/23 Sem2
 */
public class Lazy<T> {


  /**
   * The producer to produce values of type that is a
   * subtype of T.
   */
  private Producer<? extends T> producer;

  /**
   * Value of the Lazy instance that have type of Maybe.
   */
  private Maybe<T> value;
  
  /**
   * Constructor for class Lazy using value of type T.
   *
   * @param v A value of type T for a Lazy instance.
   */
  private Lazy(T v) {
    this.value = Maybe.some(v);
    this.producer = () -> v;
  }
  
  /**
   * Another constructor for class Lazy using Producer.
   *
   * @param s A producer that produce value for the Lazy instance.
   */
  private Lazy(Producer<? extends T> s) {
    this.value = Maybe.of(null);
    this.producer = s;
  }

  /**
   * Factory method for class Lazy.
   *
   * @param <T> Type of Lazy instance.
   * @param v A value for a lazy instance.
   * @return Lazy, Lazy of type T constructed using
   *         a value.
   */
  public static <T> Lazy<T> of(T v) {
    return new Lazy<>(v);
  }
  
  /**
   * Another factory method for class Lazy.
   *
   * @param <T> Type of Lazy instance.
   * @param s A producer that produce value for Lazy instance.
   * @return Lazy, Lazy of type T constructed using 
   *         a producer.
   */
  public static <T> Lazy<T> of(Producer<? extends T> s) {
    return new Lazy<>(s);
  }

  /**
   * Method that is called when the value is needed.
   *
   * @return the value if it is available, otherwise
   *         compute the value and return it.
   */
  public T get() {
    T t = this.value.orElseGet(this.producer);
    this.value = Maybe.some(t);
    return t;
  }


  /**
   * Overriden toString method for this class.
   *
   * @return "?" if the value is not yet available,
   *         otherwise returns the string representation 
   *         of the value.
   */
  @Override
  public String toString() {
    Transformer<T, String> t = x -> String.valueOf(x);
    return this.value.map(t).orElse("?");
  }

  /**
   * A method to transform the value of a Lazy instance.
   * Transformer t is only evaluated when get() is called.
   *
   * @param <U> Parameter type.
   * @param t A transformer used to transform the value of
   *        a Lazy instance.
   * @return A transformed Lazy.
   */
  public <U> Lazy<U> map(Transformer<? super T, ? extends U> t) {
    Producer<? extends U> p = () -> t.transform(this.get());
    return Lazy.of(p);
  }

  /**
   * A map method that deal with the case where the value 
   * of a Lazy instance is also a Lazy instance.
   *
   * @param <U> Parameter type.
   * @param t A transformer that transform the value of a 
   *        Lazy instance into another Lazy instance.
   * @return A flatten transformed Lazy(a Lazy that hold  
   *         value of type ? extends U).
   */
  public <U> Lazy<U> flatMap(Transformer<? super T, ? extends Lazy<? extends U>> t) {
    Producer<? extends U> p = () -> t.transform(this.get()).get();
    return Lazy.of(p);
  }
  
  /**
   * A filter method that tests if the value passes the test 
   * or not.
   *
   * @param b A BooleanCondition used to test the value.
   * @return A Lazy object that holds Boolean value.
   */
  public Lazy<Boolean> filter(BooleanCondition<? super T> b) {
    Producer<Boolean> p = () -> b.test(this.get());
    return Lazy.of(p);
  }
  
  /**
   * A overriden equals method.
   * 
   * @param obj An object that is compared to the Lazy
   *        instance.
   * @return true if both are Lazy instance with equal 
   *         value, false otherwise.
   */
  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (obj instanceof Lazy<?>) {
      Lazy<?> lazy = (Lazy<?>) obj;
      if (this.get() == null) {
        if (lazy.get() == null) {
          return true;
        }
        return false;
      }
      if (this.get() == lazy.get()) {
        return true;
      }
      return this.get().equals(lazy.get());
    }
    return false;
  }
  
  /**
   * A combine method to combine value of this instance and
   * one other Lazy instance.
   *
   * @param combiner A combiner to combine value.
   * @param <S> Type parameter.
   * @param <R> Type parameter.
   * @param lazy A lazy instance that holds value of type ? extends S.
   * @return A lazy instance of type R that holds the combined value
   *         of this Lazy instance and the parameter Lazy instance.
   */
  public <S, R> Lazy<R> combine(Lazy<? extends S> lazy, 
                                Combiner<? super T, ? super S, ? extends R> combiner) {
    Producer<? extends R> p = () -> combiner.combine(this.get(), lazy.get());
    return Lazy.of(p);
  } 
}
