package se.lejon.statemachinetest.web.conf;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.config.configurers.ExternalTransitionConfigurer;
import se.lejon.statemachinetest.web.*;

@Configuration
@EnableStateMachineFactory
public class StateMachineConf extends EnumStateMachineConfigurerAdapter<States, Events> {
  private final Logger logger = LoggerFactory.getLogger(getClass());
  private final ProvManager provManager;

  @Autowired
  public StateMachineConf(ProvManager provManager) {
    this.provManager = provManager;
  }

  @Override
  public void configure(StateMachineStateConfigurer<States, Events> states)
    throws Exception {
    states
      .withStates()
      .initial(States.WAITING)
      .state(States.COMMCHECK_REQUEST_MSISDN, requestMsisdnAction(), null)
      .state(States.COMMCHECK_FIND_VEHICLE, findVehicleAction(), null)
      .state(States.COMMCHECK_BIND_DEVICE_TO_VEHICLE, bindDeviceToVehicleAction(), null)

      .state(States.DONE, restartAction(), null)
      .state(States.WAITING_FOR_RESTART_TRIGGER, restartAction(), null)
      .state(States.FAILED, restartAction(), null);
  }

  private Action<States, Events> requestMsisdnAction() {
    return context -> provManager.requestMsisdn((String) context.getMessageHeaders().get("vin"))
      .thenAccept(msisdn -> {
        Message<Events> msisdnMessage = MessageBuilder.withPayload(Events.FIND_VEHICLE).setHeader("msisdn", msisdn).build();
        context.getStateMachine().sendEvent(msisdnMessage);
      })
      .exceptionally(throwable -> {
        if (ExceptionUtils.getRootCause(throwable) instanceof RecoverableError) {
          context.getStateMachine().sendEvent(Events.AWAIT_RESTART);
        } else {
          context.getStateMachine().sendEvent(Events.TERMINATE);
        }
        return null;
      }).thenApply(aVoid -> null);
  }

  private Action<States, Events> findVehicleAction() {
    return context -> provManager.findVehicle((String) context.getMessageHeaders().get("vin"))
      .thenAccept(vehicle -> {
        Message<Events> vehicleMessage = MessageBuilder.withPayload(Events.BIND_DEVICE_TO_VEHICLE).setHeader("vehicle", vehicle).build();
        context.getStateMachine().sendEvent(vehicleMessage);
      })
      .exceptionally(throwable -> {
        if (ExceptionUtils.getRootCause(throwable) instanceof RecoverableError) {
          context.getStateMachine().sendEvent(Events.AWAIT_RESTART);
        } else {
          context.getStateMachine().sendEvent(Events.TERMINATE);
        }
        return null;
      }).thenApply(aVoid -> null);
  }

  private Action<States, Events> bindDeviceToVehicleAction() {
    return context -> provManager.bindDeviceToVehicle((Vehicle) context.getMessageHeaders().get("vehicle"), new Device())
      .thenAccept(avoid -> context.getStateMachine().sendEvent(Events.COMPLETE))
      .exceptionally(throwable -> {
        if (ExceptionUtils.getRootCause(throwable) instanceof RecoverableError) {
          context.getStateMachine().sendEvent(Events.AWAIT_RESTART);
        } else {
          context.getStateMachine().sendEvent(Events.TERMINATE);
        }
        return null;
      }).thenApply(aVoid -> null);
  }

  private Action<States, Events> restartAction() {
    return context -> context.getStateMachine().sendEvent(Events.RESET_FLOW);
  }

  @Override
  public void configure(StateMachineTransitionConfigurer<States, Events> transitions)
    throws Exception {

    // SUCCESS FLOWS
    ExternalTransitionConfigurer<States, Events> waitingToRequestMsisdn = transitions
      .withExternal()
      .source(States.WAITING)
      .target(States.COMMCHECK_REQUEST_MSISDN)
      .event(Events.REQUEST_MSISDN);

    ExternalTransitionConfigurer<States, Events> requestMsisdnToFindVehicle = transitions
      .withExternal()
      .source(States.COMMCHECK_REQUEST_MSISDN)
      .target(States.COMMCHECK_FIND_VEHICLE)
      .event(Events.FIND_VEHICLE)
      .action(context -> System.out.println("Going to " + States.COMMCHECK_FIND_VEHICLE.name()));

    ExternalTransitionConfigurer<States, Events> findVehicleToBindDeviceToVehicle = transitions
      .withExternal()
      .source(States.COMMCHECK_FIND_VEHICLE)
      .target(States.COMMCHECK_BIND_DEVICE_TO_VEHICLE)
      .event(Events.BIND_DEVICE_TO_VEHICLE)
      .action(context -> System.out.println("Going to " + States.COMMCHECK_BIND_DEVICE_TO_VEHICLE.name()));

    ExternalTransitionConfigurer<States, Events> bindDeviceToDone = transitions
      .withExternal()
      .source(States.COMMCHECK_BIND_DEVICE_TO_VEHICLE)
      .target(States.DONE)
      .event(Events.COMPLETE)
      .action(context -> System.out.println("Going to " + States.DONE.name()));


    // ERROR FLOWS
    ExternalTransitionConfigurer<States, Events> requestMsisdnToWaitingManualrigger = transitions
      .withExternal()
      .source(States.COMMCHECK_REQUEST_MSISDN)
      .target(States.FAILED)
      .event(Events.TERMINATE)
      .action(context -> System.out.println("Going from " + States.COMMCHECK_REQUEST_MSISDN.name() + " to " + States.FAILED.name()));

    ExternalTransitionConfigurer<States, Events> requestMsisdnToWaitingRestartTrigger = transitions
      .withExternal()
      .source(States.COMMCHECK_REQUEST_MSISDN)
      .target(States.WAITING_FOR_RESTART_TRIGGER)
      .event(Events.AWAIT_RESTART)
      .action(context -> System.out.println("Going from " + States.COMMCHECK_REQUEST_MSISDN.name() + " to " + States.WAITING_FOR_RESTART_TRIGGER.name()));

    ExternalTransitionConfigurer<States, Events> findVehicleToWaitingManualrigger = transitions
      .withExternal()
      .source(States.COMMCHECK_FIND_VEHICLE)
      .target(States.FAILED)
      .event(Events.TERMINATE)
      .action(context -> System.out.println("Going from " + States.COMMCHECK_FIND_VEHICLE.name() + " to " + States.FAILED.name()));

    ExternalTransitionConfigurer<States, Events> findVehicleToWaitingRestartTrigger = transitions
      .withExternal()
      .source(States.COMMCHECK_FIND_VEHICLE)
      .target(States.WAITING_FOR_RESTART_TRIGGER)
      .event(Events.AWAIT_RESTART)
      .action(context -> System.out.println("Going from " + States.COMMCHECK_FIND_VEHICLE.name() + " to " + States.WAITING_FOR_RESTART_TRIGGER.name()));

    ExternalTransitionConfigurer<States, Events> bindVehicleToDeviceToWaitingManualrigger = transitions
      .withExternal()
      .source(States.COMMCHECK_BIND_DEVICE_TO_VEHICLE)
      .target(States.FAILED)
      .event(Events.TERMINATE)
      .action(context -> System.out.println("Going from " + States.COMMCHECK_BIND_DEVICE_TO_VEHICLE.name() + " to " + States.FAILED.name()));

    ExternalTransitionConfigurer<States, Events> bindVehicleToDeviceToWaitingRestartTrigger = transitions
      .withExternal()
      .source(States.COMMCHECK_BIND_DEVICE_TO_VEHICLE)
      .target(States.WAITING_FOR_RESTART_TRIGGER)
      .event(Events.AWAIT_RESTART)
      .action(context -> System.out.println("Going from " + States.COMMCHECK_BIND_DEVICE_TO_VEHICLE.name() + " to " + States.WAITING_FOR_RESTART_TRIGGER.name()));


    // Below is just to not having to restart the server
    // All will go back to initial state
    ExternalTransitionConfigurer<States, Events> doneToWaiting = transitions
      .withExternal()
      .source(States.DONE)
      .target(States.WAITING)
      .event(Events.RESET_FLOW)
      .action(context -> System.out.println("Going from " + States.DONE.name() + " to " + States.WAITING.name()));

    ExternalTransitionConfigurer<States, Events> waitigforManualTriggerToWaiting = transitions
      .withExternal()
      .source(States.FAILED)
      .target(States.WAITING)
      .event(Events.RESET_FLOW)
      .action(context -> System.out.println("Going from " + States.FAILED.name() + " to " + States.WAITING.name()));

    ExternalTransitionConfigurer<States, Events> waitigforRestartTriggerToWaiting = transitions
      .withExternal()
      .source(States.WAITING_FOR_RESTART_TRIGGER)
      .target(States.WAITING)
      .event(Events.RESET_FLOW)
      .action(context -> System.out.println("Going from " + States.WAITING_FOR_RESTART_TRIGGER.name() + " to " + States.WAITING.name()));
  }
}
