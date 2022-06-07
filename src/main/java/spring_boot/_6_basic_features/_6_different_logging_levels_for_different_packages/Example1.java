package spring_boot._6_basic_features._6_different_logging_levels_for_different_packages;


import java.util.Properties;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import spring_boot._6_basic_features._6_different_logging_levels_for_different_packages.pkg1.TestBean1;
import spring_boot._6_basic_features._6_different_logging_levels_for_different_packages.pkg2.TestBean2;
@SpringBootApplication
public class Example1 {

	public static void main(String[] args) {
		
		Properties properties = new Properties();
		
		properties.put("logging.level.root", "INFO");
		properties.put("logging.level.spring_boot._6_basic_features._6_different_logging_levels_for_different_packages.pkg1", "DEBUG");
		properties.put("logging.level.spring_boot._6_basic_features._6_different_logging_levels_for_different_packages.pkg2", "TRACE");

		//SpringApplication.run(Example1.class, args);
		SpringApplication app = new SpringApplication(Example1.class);
		app.setDefaultProperties(properties);
		app.run(args);
	}

	@Bean
	public CommandLineRunner data(TestBean1 testBean1, TestBean2 testBean2) {
		return (args) -> {
			testBean1.logSomething();
			testBean2.logSomething();
		};
	}
}