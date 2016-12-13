package se.lejon.statemachinetest.web.error;

public class RecoverableError extends ProvisioningError {
  public RecoverableError(String message) {
    super(message);
  }

  public RecoverableError(String message, Throwable cause) {
    super(message, cause);
  }
}
