package se.lejon.statemachinetest.web;

public enum Events {
  REQUEST_MSISDN,
  FIND_VEHICLE,
  BIND_DEVICE_TO_VEHICLE,
  COMPLETE,
  TERMINATE,
  AWAIT_RESTART,

  // DEBUG
  RESET_FLOW
}