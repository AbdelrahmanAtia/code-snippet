package spring_boot._6_basic_features._6_different_logging_levels_for_different_packages.pkg1;

import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class TestBean1 {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	public void logSomething() {
		System.out.println("=========== test bean 1: has debug logging level ===============");
		logger.info("this is an info message");
		logger.debug("this is a debug message");
		logger.trace("this is a trace message");
		System.out.println("===============================================================");
	}

}
