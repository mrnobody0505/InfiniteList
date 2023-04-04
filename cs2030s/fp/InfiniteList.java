package cs2030s.fp;

import java.util.ArrayList;
import java.util.List;

/**
 * This class implements an abstraction that supports InfiniteList.
 *
 * @param <T> Type parameter
 * @author Do Gia Hien
 * @version CS2030S AY22/23 Sem2
 */
public class InfiniteList<T> {


  /**
   * Head value of the InfiniteList.
   * @param <T> Type parameter.
   */
  private final Lazy<Maybe<T>> head;

  /**
   * Tail of the InfiniteList that is also an InfiniteList.
   * Since it has to be lazy, Lazy data type is used.
   * @param <T> Type parameter.
   */
  private final Lazy<InfiniteList<T>> tail;

  /**
   * This class implements a special tail (aka a Sentinel)
   * to mark the end of the list.
   */
  private static final class Sentinel extends InfiniteList<Object> {

    /**
     * Overriden toString method.
     * @return "-".
     */
    @Override
    public String toString() {
      return "-";
    }

    /**
     * Constructor of a Sentinel.
     */
    public Sentinel() {
      super();
    }

    /**
     * An overriden method to get the head of the list.
     * Since there is nothing in the Sentinel, 
     * NoSuchElementException will be thrown.
     */
    @Override
    public Object head() {
      throw new java.util.NoSuchElementException();
    }

    /**
     * An overriden method to get the tail of the list.
     * Since the tail of a Sentinel is just itself, we
     * return a Sentinel.
     * @return A Sentinel.
     */
    @Override
    public InfiniteList<Object> tail() {
      return InfiniteList.sentinel();
    }

    /**
     * An overriden map method for Sentinel.
     * @return A Sentinel.
     */
    @Override
    public <R> InfiniteList<R> map(Transformer<? super Object, ? extends R> mapper) {
      return InfiniteList.sentinel();
    }

    /**
     * An overriden filter method for Sentinel.
     * @return A Sentinel.
     */
    @Override
    public InfiniteList<Object> filter(BooleanCondition<? super Object> predicate) {
      return InfiniteList.sentinel();
    }

    /**
     * An overriden limit method for Sentinel.
     * @return A Sentinel.
     */
    @Override
    public InfiniteList<Object> limit(long n) {
      return InfiniteList.sentinel();
    }

    /**
     * An overriden takeWhile method for Sentinel.
     * @return A Sentinel.
     */
    @Override
    public InfiniteList<Object> takeWhile(BooleanCondition<? super Object> pred) {
      return InfiniteList.sentinel();
    }

  }

  /**
   * One and only static one sentinel for the InfiniteList class.
   */
  private static final InfiniteList<?> sentinel = new Sentinel();

  /**
   * A constructor with no parameter for InfiniteList.
   * Used to create a Sentinel.
   */
  private InfiniteList() { 
    this.head = null; 
    this.tail = null;
  }

  /**
   * A generate method that generates an InfiniteList using a producer.
   * @param <T> Type parameter.
   * @param producer A producer to produce values.
   * @return An InfiniteList.
   */
  public static <T> InfiniteList<T> generate(Producer<T> producer) {
    return new InfiniteList<>(Lazy.of(() -> Maybe.some(producer.produce())), 
        Lazy.of(() -> InfiniteList.generate(producer)));
  }

  /**
   * An interate method that used to creat an InfiniteList.
   * @param <T> Type parameter.
   * @param seed Initial value of the List.
   * @param next A transformer 
   * @return An InfiniteList that has the form 
   *         seed, next(seed), next(next(seed)),...
   */
  public static <T> InfiniteList<T> iterate(T seed, Transformer<T, T> next) {
    return new InfiniteList<>(seed, () -> InfiniteList.iterate(next.transform(seed), next));
  }

  /**
   * A constructor for the class.
   * @param head Initial value.
   * @param tail A producer to create the tail.
   */
  private InfiniteList(T head, Producer<InfiniteList<T>> tail) {
    this.head = Lazy.of(Maybe.some(head));
    this.tail = Lazy.of(tail);
  }

  /**
   * A constructor for the class.
   * @param head Lazy value for the head.
   * @param tail Lazy value for the tail.
   */
  private InfiniteList(Lazy<Maybe<T>> head, Lazy<InfiniteList<T>> tail) {
    this.head = head;
    this.tail = tail;
  }

  /**
   * Method that return the head of the list, 
   * skipping Maybe.none() values.
   * @return Head of the list of type T
   */
  public T head() {
    return this.head.get().orElseGet(() -> this.tail.get().head());
  }

  /**
   * Method that return the tail of the list,
   * skipping Maybe.none() values.
   * @return Tail of the list, which is an InfiniteList.
   */
  public InfiniteList<T> tail() {
    Maybe<InfiniteList<T>> t = this.head.get().map(x -> this.tail.get());
    return t.orElseGet(() -> this.tail.get().tail());
  }

  /** 
   * Map method for our InfiniteList.
   * The method used to lazily apply transformer
   * mapper to the whole list.
   * @param mapper A Transformer.
   * @param <R> Type parameter.
   * @return A transformed InfiniteList.
   */
  public <R> InfiniteList<R> map(Transformer<? super T, ? extends R> mapper) {
    return new InfiniteList<>(Lazy.of(() -> Maybe.some(mapper.transform(this.head()))),
        Lazy.of(() -> this.tail().map(mapper)));
  }

  /**
   * A filter method that tests if values in the list 
   * passes the test or not.
   * @param predicate A BooleanCondition used to test the value.
   * @return A filtered InfiniteList with filtered values.
   */
  public InfiniteList<T> filter(BooleanCondition<? super T> predicate) {
    return new InfiniteList<>(Lazy.of(() -> Maybe.some(this.head()).filter(predicate)), 
        Lazy.of(() -> this.tail().filter(predicate)));
  }

  /**
   * Static method that returns a Sentinel.
   * @param <T> Type parameter.
   * @return A Sentinel.
   */
  public static <T> InfiniteList<T> sentinel() {
    @SuppressWarnings("unchecked")
    InfiniteList<T> endMark = (InfiniteList<T>) sentinel;
    return endMark;
  }

  /**
   * A method that limits the first n elements of the
   * InfiniteList.
   * @param n Number of the elements of the limited list.
   * @return A limited list.
   */
  public InfiniteList<T> limit(long n) {
    if (this.head.toString() == "?") {
      return n <= 0 ? InfiniteList.sentinel() 
        : new InfiniteList<>(Lazy.of(() -> Maybe.some(this.head())), 
            Lazy.of(() -> this.tail().limit(n - 1)));
    }
    return n <= 0 ? InfiniteList.sentinel()
      : new InfiniteList<>(this.head(), 
          () -> this.tail().limit(n - 1));
  }

  /**
   * A method that truncates the list as soon as 
   * it finds an element that evaluates the predicate 
   * to false.
   * @param predicate A BooleanCondition to test the values.
   * @return A list that has the first k elements of this list 
   *         that satisfy the predicate.
   */
  public InfiniteList<T> takeWhile(BooleanCondition<? super T> predicate) {
    if (this.isSentinel()) {
      return InfiniteList.sentinel();
    }
    Lazy<Boolean> temp = Lazy.of(() -> predicate.test(this.head()));    
    Producer<Maybe<T>> h = () -> temp.get()
        ? Maybe.some(this.head())
        : Maybe.none();
    Producer<InfiniteList<T>> t = () -> temp.get()
        ? this.tail().takeWhile(predicate)
        : InfiniteList.sentinel();
    return new InfiniteList<>(Lazy.of(h), Lazy.of(t));
  }

  /**
   * A method to check if this list is a Sentinel or not.
   * @return true if the list is Sentinel, false
   *         otherwise.
   */
  public boolean isSentinel() {
    return this instanceof Sentinel;
  }

  /** 
   * A method that resembles reduce method in
   * java.util.Stream
   * @param <U> Type parameter.
   * @param identity Indentity value.
   * @param accumulator Accumulation function.
   * @return Reduced value.
   */
  public <U> U reduce(U identity, Combiner<U, ? super T, U> accumulator) {
    try {
      return this.isSentinel()
        ? identity
        : accumulator.combine(this.tail().reduce(identity, accumulator), this.head());

    } catch (java.util.NoSuchElementException e) {
      return identity;
    }
  }

  /**
   * A method that count the elements of the List.
   * @return Number of elements in the list.
   */
  public long count() {
    return this.reduce(0, (x, y) -> x + 1);
  }

  /**
   * A method that collects elements in the InfiniteList
   * into a java.util.List.
   * @return A java.util.List.
   */
  public List<T> toList() {
    List<T> list = new ArrayList<>();
    try {
      InfiniteList<T> clone = this;
      while (!clone.isSentinel()) {
        clone.head.get().ifPresent(list::add);
        //list.add(clone.head());
        clone = clone.tail.get();
      }
    } catch (java.util.NoSuchElementException e) {
      return list;
    } finally {
      return list;
    }

  }

  /**
   * A toString method for the class.
   * @return String representation of the list.
   */
  public String toString() {
    return "[" + this.head + " " + this.tail + "]";
  }
}
