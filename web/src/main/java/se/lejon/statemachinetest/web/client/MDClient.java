package se.lejon.statemachinetest.web.client;

import org.springframework.stereotype.Service;
import se.lejon.statemachinetest.web.Device;
import se.lejon.statemachinetest.web.Vehicle;

import java.util.concurrent.CompletableFuture;

@Service
public class MDClient {
  public CompletableFuture<Vehicle> findVehicle(final String vin) {
    return CompletableFuture.completedFuture(new Vehicle(vin));
  }

  public CompletableFuture<Void> bindDeviceToVehicle(Vehicle vehicle, Device device) {
    return CompletableFuture.completedFuture(null);
  }
}
