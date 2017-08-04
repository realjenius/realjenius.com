---
title: 'The Increased Importance of Generic Exceptions in API Design'
summary: 'Java supports throwing exceptions of a generic type, but it is rarely a useful trick. With Java 8, that is changing.'
tags: ["java", "java8", "lambdas", "exceptions"]
date: 2013-11-22
---

One of the more interesting things with Java 8 will be how it impacts the way API design is done. This is already being discussed a lot with lambdas, and indeed, they are the key driver for much of the API change. However, it's not as simple as making sure you accept single method interfaces everywhere. There are a lot of subtle changes to consider. I will walk through one here with exceptions.

The ability to generically define the exceptions in a throws clause for a method in Java isn't a new thing (although most devs don't seem to know or care about it). Pop open any Java 7 project, and you'll be able to do this:

```java
public abstract class Thingy<T extends Throwable> {
  public void doSomething() throws T {
    // ...
  }
}

public class FileThingy extends Thingy<FileNotFoundException> {
  @Override
  public void doSomething() throws FileNotFoundException {
    // ...
  }
}
```

Unfortunately, in practical terms this is of limited value in most cases, as the context of handling the exception usually needs to know the explicit type, making the polymorphic benefits of generics somewhat minimal.

In fact, the only case I've ever had where this provided legitimate value was in tunneling typed exceptions through a transaction-handling method.

### A Rare Useful Case Pre Java-8

```java
public void saveThingy(Thingy thingy) throws NotFoundException, AlreadyExistsException, InvalidInputException {
  try {
    inTransaction(new TransactionalOp() {
      public void exec() throws Throwable {
        // may save, or may throw one of many checked exceptions.
        throw new AlreadyExistsException();
      }
    });
  }
  catch(TransactionException e) {
    e.throwIf(NotFoundException.class)
     .or(AlreadyExistsException.class)
     .or(InvalidInputException.class)
     .elseRuntimeException();
  }
}

// the signature of the generic "inTransaction" method:
public void inTransaction(TransactionalOp op) throws TransactionException { ... }
```

In this case, we want our transactional op to be able to throw checked exceptions so our outer-method can declare them (and force API users to handle them), but we can't go changing the signature of our reusable `inTransaction` method.

To handle a "general" exception bubbling workflow, The `inTransaction` method catches all `Throwables`, and wraps them in a new checked exception that can then be unraveled using this fairly clean and fluid API. Effectively, the entire goal of this is to make it easier to type than a whole bunch of `if(e.getCause() instanceof XYZ) throw (XYZ)e.getCause();` switches.

The `TransactionException` implementation could look something like this:

```java
public class TransactionException extends Exception {
  public TransactionException(Throwable cause) {
    super(cause);
  }

  public <T extends Throwable> TransactionException throwIf(Class<T> type) throws T {
    if(type.isInstance(getCause())) throw type.cast(getCause());
      return this;
  }

  public <T extends Throwable> TransactionException or(Class<T> type) throws T { return throwIf(type); }

  public void elseRuntimeException() {
    throw new RuntimeException(getCause());
  }
}
```

So, like I said - that's the only time it's really benefited me.

### Coming Soon with Java 8: More Generic Exceptions

In Java 8 thanks to the utility and terse syntax of lambdas this generification of throws parameters is becoming much more useful, and is something you can start considering to assist developers in using your APIs. Consider, for example, the Java 8 `Optional` class:

```java
public void loadString() throws IOException {
  Optional<String> maybeStr = // load from somewhere.
  String str = maybeStr.orElseThrow(() -> new IOException());
}
```

In this case, we're asking the `Optional` to throw our custom exception (an `IOException`) if the String is not present. To support this flow, the optional class has a method that takes a supplier of exceptions of a certain type:

```java
public <E extends Throwable> T orElseThrow(Supplier<? extends E> supplier) throws E {
  // effective impl:
  if(get() != null) return get();
  else throw supplier.get();
}
```

Since you can specify an implementation of the exception supplier in a very brief lambda definition, this provides a very clear and concise flow, and makes `orElseThrow` fit your use case, instead of the typical scenario of checked exceptions, where you have to work around things to meet the need of the API that throws the exception.
