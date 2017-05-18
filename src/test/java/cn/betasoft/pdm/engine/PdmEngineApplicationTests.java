package cn.betasoft.pdm.engine;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import cn.betasoft.pdm.engine.scheduler.JobService;

@RunWith(SpringRunner.class)
@SpringBootTest
public class PdmEngineApplicationTests {

	@Autowired
	JobService jobService;

	@Test
	public void contextLoads() {

	}

}
