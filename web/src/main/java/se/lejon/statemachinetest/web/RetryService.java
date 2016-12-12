package se.lejon.statemachinetest.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class RetryService {
  private final Logger logger = LoggerFactory.getLogger(getClass());

  public void add(String vin) {
    logger.info("Added vin {} to the retry queue", vin);
  }
}
