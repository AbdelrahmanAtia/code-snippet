package spring_boot._6_basic_features._2_logging_http_request_and_response_using_commons_logging_filter;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.servlet.Filter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jms.activemq.ActiveMQAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

@SpringBootApplication(exclude = ActiveMQAutoConfiguration.class)
public class Example1 {

	public static void main(String[] args) {

		Properties properties = new Properties();
		properties.put("logging.level.org.springframework.web", "DEBUG");
		
		// properties.put("spring.jpa.show-sql", true);
		// properties.put("logging.level.org.hibernate.SQL", "DEBUG");

		// SpringApplication.run(Example1.class, args);
		SpringApplication app = new SpringApplication(Example1.class);
		app.setDefaultProperties(properties);
		app.run(args);
	}

}

class Product {

	private String name;
	private double price;

	public Product() {

	}

	public Product(String name, double price) {
		super();
		this.name = name;
		this.price = price;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Product [name=");
		builder.append(name);
		builder.append(", price=");
		builder.append(price);
		builder.append("]");
		return builder.toString();
	}

}

@RestController
@RequestMapping("/products")
class ProductController {

	// private static final Logger log =
	// LoggerFactory.getLogger(CurrencyController.class);

	@GetMapping("/all")
	public List<Product> getAllProducts() {
		List<Product> products = new ArrayList<>();

		products.add(new Product("pizza", 200));
		products.add(new Product("pasta", 150));
		products.add(new Product("beaf", 400));

		return products;
	}
}

@Configuration
class AppConfig {

	//this bean is used to log the HTTP request & response
	//to see the request & response in the logs, u have to change 
	//logging level to debug as it is done in the main method
	@Bean
	public Filter logFilter() {
		CommonsRequestLoggingFilter filter = new CommonsRequestLoggingFilter();
		filter.setIncludeQueryString(true);
		filter.setIncludePayload(true);
		filter.setMaxPayloadLength(5120);
		filter.setIncludeHeaders(true);
		return filter;
	}
}