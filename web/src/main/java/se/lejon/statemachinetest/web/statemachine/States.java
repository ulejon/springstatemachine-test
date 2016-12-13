package se.lejon.statemachinetest.web.statemachine;

public enum States {
  WAITING,
  COMMCHECK_REQUEST_MSISDN,
  COMMCHECK_FIND_VEHICLE,
  COMMCHECK_BIND_DEVICE_TO_VEHICLE,
  DONE,
  FAILED,
  WAITING_FOR_RESTART_TRIGGER
}
