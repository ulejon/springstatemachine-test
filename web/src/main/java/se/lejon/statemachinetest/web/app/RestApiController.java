package se.lejon.statemachinetest.web.app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;
import se.lejon.statemachinetest.web.statemachine.Events;
import se.lejon.statemachinetest.web.statemachine.StateMachines;
import se.lejon.statemachinetest.web.statemachine.States;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
public class RestApiController {
  private final Logger logger = LoggerFactory.getLogger(getClass());
  private final StateMachines stateMachines;

  @Autowired
  public RestApiController(StateMachines stateMachines) {
    this.stateMachines = stateMachines;
  }

  @RequestMapping(value = "/vin/{vin}/{event}", method = GET)
  public DeferredResult<ResponseEntity<String>> sendEvent(@PathVariable(name = "vin") final String vin,
                                                          @PathVariable(name = "event") final String rawEvent) {
    DeferredResult<ResponseEntity<String>> result = new DeferredResult<>();
    Optional<Events> eventTyped = parseEvent(rawEvent);

    if (eventTyped.isPresent()) {
      StateMachine<States, Events> stateMachine = startMachineForVin(vin);
      Events event = eventTyped.get();
      boolean eventAccepted = stateMachine.sendEvent(MessageBuilder.withPayload(event).setHeader("vin", vin).build());
      logger.info("Event {} accepted: {}", event, eventAccepted);

      if (eventAccepted) {
        result.setResult(ResponseEntity.ok(format("Event '%s' accepted!", event.name().toLowerCase())));
      } else if (event == Events.REQUEST_MSISDN) {
        result.setResult(ResponseEntity.ok(format("Event '%s' was NOT accepted. State machine for vin '%s' is already processing request",
          event.name().toLowerCase(), vin)));
      } else {
        result.setResult(ResponseEntity.ok(format("Event '%s' was NOT accepted. " +
            "Note that the only valid entry point event for state machine is '%s'",
          event.name().toLowerCase(), Events.REQUEST_MSISDN.name().toLowerCase())));
      }
    } else {
      List<String> allEvents = Arrays.stream(Events.values()).map(event -> event.name().toLowerCase()).collect(Collectors.toList());
      result.setResult(ResponseEntity.badRequest().body(format("'%s' is not a valid event. " +
        "Valid events are: '%s'", rawEvent, String.join(", ", allEvents))));
    }

    return result;
  }

  private StateMachine<States, Events> startMachineForVin(final String vin) {
    logger.info("Will start the machine for vin {}", vin);
    StateMachine<States, Events> stateMachine = stateMachines.get(new StateMachines.RepoKey(vin));
    stateMachine.start();
    return stateMachine;
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
