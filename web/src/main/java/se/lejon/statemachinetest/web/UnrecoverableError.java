package se.lejon.statemachinetest.web;

public class UnrecoverableError extends ProvisioningError {
  public UnrecoverableError(String message) {
    super(message);
  }

  public UnrecoverableError(String message, Throwable cause) {
    super(message, cause);
  }
}
