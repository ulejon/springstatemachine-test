package se.lejon.statemachinetest.web;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.lejon.statemachinetest.web.client.MDClient;
import se.lejon.statemachinetest.web.client.MNOClient;

import java.util.Random;
import java.util.concurrent.CompletableFuture;

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
      CompletableFuture.supplyAsync(() -> {
        logger.info("Start request msisdn");
        return null;
      }).thenCompose(v -> mnoClient.getMsisdn(vin))
        .thenApply(msisdn -> {
          doSleep(5000);
          return msisdn;
        })
        .thenApply(msisdn -> {
          logger.info("End request msisdn");
          return msisdn;
        })
        .thenApply(msisdn -> {
          if (getBool(true)) {
            return msisdn;
          } else {
            if (getBool(true)) {
              throw new RecoverableError("This is recoverable");
            } else {
              throw new UnrecoverableError("This is unrecoverable");
            }
          }
        })
        .exceptionally(throwable -> {
          if (ExceptionUtils.getRootCause(throwable) instanceof RecoverableError) {
            retryService.add(vin);
          }

          throw new IllegalStateException("Error requesting msisdn", throwable);
        });
  }

  public CompletableFuture<Vehicle> findVehicle(String vin) {
    return
      CompletableFuture.supplyAsync(() -> {
        logger.info("Start find vehicle");
        return null;
      }).thenCompose(v -> mdClient.findVehicle(vin))
        .thenApply(vehicle -> {
          doSleep(5000);
          return vehicle;
        })
        .thenApply(vehicle -> {
          logger.info("End find vehicle");
          return vehicle;
        })
        .thenApply(vehicle -> {
          if (getBool(true)) {
            return vehicle;
          } else {
            if (getBool(true)) {
              throw new RecoverableError("This is recoverable");
            } else {
              throw new UnrecoverableError("This is unrecoverable");
            }
          }
        })
        .exceptionally(throwable -> {
          if (ExceptionUtils.getRootCause(throwable) instanceof RecoverableError) {
            retryService.add(vin);
          }

          throw new IllegalStateException("Error finding vehicle", throwable);
        });
  }

  public CompletableFuture<Void> bindDeviceToVehicle(Vehicle vehicle, Device device) {
    return
      CompletableFuture.supplyAsync(() -> {
        logger.info("Start bind device to vehicle");
        return null;
      }).thenCompose(v -> mdClient.bindDeviceToVehicle(vehicle, device))
        .thenApply(v -> {
          doSleep(5000);
          return v;
        })
        .thenApply(v -> {
          logger.info("End device to vehicle");
          return v;
        })
        .thenApply(v -> {
          if (getBool(true)) {
            return v;
          } else {
            if (getBool(true)) {
              throw new RecoverableError("This is recoverable");
            } else {
              throw new UnrecoverableError("This is unrecoverable");
            }
          }
        })
        .exceptionally(throwable -> {
          if (ExceptionUtils.getRootCause(throwable) instanceof RecoverableError) {
            retryService.add(vehicle.getVin());
          }

          throw new IllegalStateException("Error binding device to vehicle", throwable);
        });
  }

  private void doSleep(long sleep) {
    try {
      Thread.sleep(sleep);
    } catch (InterruptedException e) {
      logger.error("Error sleeping", e);
    }
  }

  private boolean getBool(boolean randomResponse) {
    return !randomResponse || new Random().nextBoolean();
  }
}
