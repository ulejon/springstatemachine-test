package se.lejon.statemachinetest.web;

public class StateMachineCallback {

  @FunctionalInterface
  public interface Succcess<T> {
    void apply(T val);
  }

  @FunctionalInterface
  public interface Error {
    void apply(Throwable throwable);
  }
}
