package se.lejon.statemachinetest.web.app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;
import se.lejon.statemachinetest.web.statemachine.Events;
import se.lejon.statemachinetest.web.statemachine.StateMachines;
import se.lejon.statemachinetest.web.statemachine.States;

import java.util.Optional;

import static java.lang.String.format;

@RestController
public class RestApi {
  private final Logger logger = LoggerFactory.getLogger(getClass());
  private final StateMachines stateMachines;

  @Autowired
  public RestApi(StateMachines stateMachines) {
    this.stateMachines = stateMachines;
  }

  @GetMapping(value = "{vin}")
  public String startMachine(@PathVariable(name = "vin") final String vin,
                             @RequestParam(name = "start", defaultValue = "true") final boolean start) {
    logger.info("Will {} the machine", start ? "start" : "stop");
    StateMachine<States, Events> stateMachine = stateMachines.get(new StateMachines.RepoKey(vin));

    if (start) {
      stateMachine.start();
    } else {
      stateMachine.stop();
    }

    return stateMachine.getId();
  }

  @GetMapping(value = "{vin}/{event}")
  public DeferredResult<ResponseEntity<String>> sendEvent(@PathVariable(name = "vin") final String vin,
                                                          @PathVariable(name = "event") final String rawEvent) {
    DeferredResult<ResponseEntity<String>> result = new DeferredResult<>();
    Optional<Events> eventTyped = parseEvent(rawEvent);

    if (eventTyped.isPresent()) {
      StateMachine<States, Events> stateMachine = stateMachines.get(new StateMachines.RepoKey(vin));
      Events event = eventTyped.get();
      boolean eventAccepted = stateMachine.sendEvent(MessageBuilder.withPayload(event).setHeader("vin", vin).build());
      logger.info("Event {} accepted: {}", event, eventAccepted);
      result.setResult(ResponseEntity.ok(event.name() + ": " + eventAccepted));
    } else {
      result.setResult(ResponseEntity.badRequest().body(format("Failed to parse '%s'", rawEvent)));
    }

    return result;
  }

  private Optional<Events> parseEvent(final String raw) {
    try {
      Events events = Events.valueOf(raw.trim().toUpperCase());
      return Optional.of(events);
    } catch (Exception e) {
      return Optional.empty();
    }
  }
}
