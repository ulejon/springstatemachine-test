package se.lejon.statemachinetest.web.client;

import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class MNOClient {
  public CompletableFuture<String> getMsisdn(final String vin) {
    return CompletableFuture.completedFuture("msisdn-" + vin);
  }
}
