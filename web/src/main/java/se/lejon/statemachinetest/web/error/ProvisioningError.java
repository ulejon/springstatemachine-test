package se.lejon.statemachinetest.web.error;

public class ProvisioningError extends RuntimeException {
  public ProvisioningError(String message) {
    super(message);
  }

  public ProvisioningError(String message, Throwable cause) {
    super(message, cause);
  }
}
