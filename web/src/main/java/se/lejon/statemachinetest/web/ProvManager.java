package se.lejon.statemachinetest.web;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.lejon.statemachinetest.web.client.MDClient;
import se.lejon.statemachinetest.web.client.MNOClient;
import se.lejon.statemachinetest.web.error.RecoverableError;
import se.lejon.statemachinetest.web.error.UnrecoverableError;
import se.lejon.statemachinetest.web.model.Device;
import se.lejon.statemachinetest.web.model.Vehicle;

import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;

@Service
public class ProvManager {
  private final Logger logger = LoggerFactory.getLogger(getClass());

  private final MNOClient mnoClient;
  private final RetryService retryService;
  private final MDClient mdClient;

  @Autowired
  public ProvManager(MNOClient mnoClient, RetryService retryService, MDClient mdClient) {
    this.mnoClient = mnoClient;
    this.retryService = retryService;
    this.mdClient = mdClient;
  }

  public CompletableFuture<String> requestMsisdn(String vin) {
    return
      CompletableFuture.supplyAsync(getStartCheckpointLogSupplier("Start request msisdn"))
        .thenCompose(v -> mnoClient.getMsisdn(vin))
        .thenApply(getSleepFunc())
        .thenApply(getEndCheckpointLogFunc("End request msisdn"))
        .thenApply(getRandomResultFunc())
        .exceptionally(getHandleErrorFunc(vin, "Error requesting msisdn"));
  }

  public CompletableFuture<Vehicle> findVehicle(String vin) {
    return
      CompletableFuture.supplyAsync(getStartCheckpointLogSupplier("Start find vehicle"))
        .thenCompose(v -> mdClient.findVehicle(vin))
        .thenApply(getSleepFunc())
        .thenApply(getEndCheckpointLogFunc("End find vehicle"))
        .thenApply(getRandomResultFunc())
        .exceptionally(getHandleErrorFunc(vin, "Error finding vehicle"));
  }

  public CompletableFuture<Void> bindDeviceToVehicle(Vehicle vehicle, Device device) {
    return
      CompletableFuture.supplyAsync(getStartCheckpointLogSupplier("Start bind device to vehicle"))
        .thenCompose(v -> mdClient.bindDeviceToVehicle(vehicle, device))
        .thenApply(getSleepFunc())
        .thenApply(getEndCheckpointLogFunc("End bind device to vehicle"))
        .thenApply(getRandomResultFunc())
        .exceptionally(getHandleErrorFunc(vehicle.getVin(), "Error binding device to vehicle"));
  }

  private Supplier<Object> getStartCheckpointLogSupplier(String logMessage) {
    return () -> {
      logger.info(logMessage);
      return null;
    };
  }

  private <T> Function<T, T> getSleepFunc() {
    return obj -> {
      doSleep(5000);
      return obj;
    };
  }

  private void doSleep(long sleep) {
    try {
      Thread.sleep(sleep);
    } catch (InterruptedException e) {
      logger.error("Error sleeping", e);
    }
  }

  private <T> Function<T, T> getEndCheckpointLogFunc(String logMessage) {
    return obj -> {
      logger.info(logMessage);
      return obj;
    };
  }

  private boolean getBool(boolean randomResponse) {
    return !randomResponse || new Random().nextBoolean();
  }

  private <T> Function<T, T> getRandomResultFunc() {
    return obj -> {
      if (getBool(true)) {
        return obj;
      } else {
        if (getBool(true)) {
          throw new RecoverableError("This is recoverable");
        } else {
          throw new UnrecoverableError("This is unrecoverable");
        }
      }
    };
  }

  private <T> Function<Throwable, T> getHandleErrorFunc(String vin, String message) {
    return throwable -> {
      if (ExceptionUtils.getRootCause(throwable) instanceof RecoverableError) {
        retryService.add(vin);
      }

      throw new IllegalStateException(message, throwable);
    };
  }
}
