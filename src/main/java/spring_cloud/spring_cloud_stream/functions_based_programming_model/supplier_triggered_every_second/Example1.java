
package spring_cloud.spring_cloud_stream.functions_based_programming_model.supplier_triggered_every_second;

import java.util.Date;
import java.util.Properties;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Example1 {

	// ################################################################
	// #################### NOT WORKING ###############################
	// ################################################################

	public static void main(String[] args) {
		// SpringApplication.run(Example1.class, args);

		Properties properties = new Properties();

		properties.put("spring.cloud.stream.bindings.myPublisher-out-0.destination", "myProcessor-in");
		properties.put("spring.cloud.stream.bindings.myProcessor-in-0.destination", "myProcessor-in");
		properties.put("spring.cloud.stream.bindings.myProcessor-out-0.destination", "myProcessor-out");
		properties.put("spring.cloud.stream.bindings.mySubscriber-in-0.destination", "myProcessor-out");

		SpringApplication app = new SpringApplication(Example1.class);
		app.setDefaultProperties(properties);
		app.run(args);

	}

	@Bean
	public Supplier<String> myPublisher() {
		return () -> new Date().toString();
	}

	@Bean
	public Function<String, String> myProcessor() {
		return s -> "ML PROCESSED: " + s;
	}

	@Bean
	public Consumer<String> mySubscriber() {
		return s -> System.out.println("ML RECEIVED: " + s);
	}
}
