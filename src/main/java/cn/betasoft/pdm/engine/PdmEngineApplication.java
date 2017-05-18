package cn.betasoft.pdm.engine;

import cn.betasoft.pdm.engine.scheduler.JobTestService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class PdmEngineApplication {

	public static void main(String[] args) {
		SpringApplication.run(PdmEngineApplication.class, args);
	}

	@Bean
	public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
		return args -> {
			JobTestService jobTestService = (JobTestService)ctx.getBean("jobTestServiceImpl");
			jobTestService.testActorModel();
		};
	}
}
