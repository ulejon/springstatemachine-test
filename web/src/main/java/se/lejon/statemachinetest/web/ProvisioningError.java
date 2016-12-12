package se.lejon.statemachinetest.web;

public class ProvisioningError extends RuntimeException {
  public ProvisioningError(String message) {
    super(message);
  }

  public ProvisioningError(String message, Throwable cause) {
    super(message, cause);
  }
}
