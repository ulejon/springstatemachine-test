package se.lejon.statemachinetest.web.app;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import se.lejon.statemachinetest.web.statemachine.StateMachineConf;

@Configuration
@ComponentScan(basePackages = "se.lejon.statemachinetest.web")
@Import(StateMachineConf.class)
public class MainConf {
}
