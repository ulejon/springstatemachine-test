package se.lejon.statemachinetest.web.conf;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ComponentScan(basePackages = "se.lejon.statemachinetest.web")
@Import(StateMachineConf.class)
public class MainConf {
}
