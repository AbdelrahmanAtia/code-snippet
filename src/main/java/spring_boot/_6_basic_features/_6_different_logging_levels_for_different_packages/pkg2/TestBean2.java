package spring_boot._6_basic_features._6_different_logging_levels_for_different_packages.pkg2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class TestBean2 {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	public void logSomething() {
		System.out.println("=========== test bean 2: has trace logging level ===============");
		logger.info("this is an info message");
		logger.debug("this is a debug message");
		logger.trace("this is a trace message");
		System.out.println("================================================================");
	}

}
