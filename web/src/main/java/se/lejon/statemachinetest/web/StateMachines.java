package se.lejon.statemachinetest.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class StateMachines {
  private final StateMachineFactory<States, Events> factory;
  private final Map<String, StateMachine<States, Events>> repo;

  @Autowired
  public StateMachines(StateMachineFactory<States, Events> factory) {
    this.factory = factory;
    repo = new ConcurrentHashMap<>();
  }

  StateMachine<States, Events> get(RepoKey repoKey) {
    return repo.computeIfAbsent(repoKey.key, k -> factory.getStateMachine());
  }

  static final class RepoKey {
    final String key;

    RepoKey(String key) {
      this.key = key;
    }
  }
}
