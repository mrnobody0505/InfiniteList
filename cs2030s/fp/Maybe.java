package cs2030s.fp;
/**
 * CS2030S Lab 5
 * AY22/23 Semester 2
 *
 * @author Do Gia Hien (L14B)
*/

import java.util.NoSuchElementException;

public abstract class Maybe<T> {

  private static class None extends Maybe<Object> {

    @Override
    public String toString() {
      return "[]";
    }

    @Override
    public boolean equals(Object obj) {
      return obj instanceof None;
    }

    @Override
    protected Object get() {
      throw new NoSuchElementException();
    }

    @Override
    public Maybe<Object> filter(BooleanCondition<? super Object> booleanCondition) {
      return this;
    }

    @Override
    public <U> Maybe<U> map(Transformer<? super Object, ? extends U> transformer) {
      return Maybe.<U>none();
    }

    @Override
    public <U> Maybe<U> flatMap(Transformer<? super Object, 
                                ? extends Maybe<? extends U>> transformer) {
      return Maybe.<U>none();
    }

    @Override
    public Object orElse(Object obj) {
      return obj;
    }

    @Override
    public Object orElseGet(Producer<? extends Object> producer) {
      return producer.produce();
    } 

    @Override
    public void ifPresent(Consumer<? super Object> consumer) {

    }
  }

  private static final class Some<T> extends Maybe<T> {

    private final T t;

    public Some(T t) {
      this.t = t;
    }

    @Override
    public String toString() {
      return "[" + this.t + "]";
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == this) {
        return true;
      }

      if (obj == null) {
        return false;
      }

      if (obj instanceof Some<?>) {
        Some<?> some = (Some<?>) obj;
        if (this.t == null) {
          if (some.t == null) {
            return true;
          } 
          return false;
        }
        if (this.t == some.t) {
          return true;
        }
        return this.t.equals(some.t);
      }
      return false;
    }

    @Override
    protected T get() {
      return this.t;
    }

    @Override
    public Maybe<T> filter(BooleanCondition<? super T> booleanCondition) {
      if (this.t != null && !booleanCondition.test(this.t)) {
        return Maybe.<T>none();
      } 
      return this;
    }

    @Override
    public <U> Maybe<U> map(Transformer<? super T, ? extends U> transformer) {
      try {
        transformer.transform(this.t);
      } catch (NullPointerException e) {
        System.out.println(e);
      } 
      U u = transformer.transform(this.t);
      return new Some<>(u);
    }

    @Override
    public <U> Maybe<U> flatMap(Transformer<? super T, ? extends Maybe<? extends U>> transformer) {
      if (transformer.transform(this.t) instanceof None) {
        return Maybe.<U>none();
      }
      @SuppressWarnings("unchecked")
      //type-safe since Maybe<U> <: Maybe<? extends U> for all types U
      Maybe<U> maybeU = (Maybe<U>) transformer.transform(this.t); 
      return maybeU;
    }

    @Override
    public T orElse(T t) {
      return this.t;
    }

    @Override
    public T orElseGet(Producer<? extends T> producer) {
      return this.t;
    }

    @Override
    public void ifPresent(Consumer<? super T> consumer) {
      consumer.consume(this.t);
    }
  }

  private static final Maybe<?> NONE = new None();

  public static <T> Maybe<T> none() {
    @SuppressWarnings("unchecked")
    Maybe<T> clone = (Maybe<T>) NONE;
    return clone;
  }

  public static <T> Maybe<T> some(T t) {
    return new Some<>(t);
  }

  public abstract String toString();

  public abstract boolean equals(Object obj); 

  public static <T> Maybe<T> of(T t) {
    if (t == null) {
      return Maybe.<T>none();
    } 
    return new Some<>(t);
  }

  protected abstract T get();

  public abstract Maybe<T> filter(BooleanCondition<? super T> booleanCondition);

  public abstract <U> Maybe<U> map(Transformer<? super T, ? extends U> transformer);

  public abstract <U> Maybe<U> flatMap(Transformer<? super T, 
                                      ? extends Maybe<? extends U>> transformer);

  public abstract T orElse(T t);

  public abstract T orElseGet(Producer<? extends T> producer);

  public abstract void ifPresent(Consumer<? super T> consumer);
}

