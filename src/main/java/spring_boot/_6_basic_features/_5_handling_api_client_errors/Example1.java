package spring_boot._6_basic_features._5_handling_api_client_errors;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.Properties;

import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootApplication
public class Example1 {

	public static void main(String[] args) {
		Properties properties = new Properties();
		properties.put("server.port", 7001);

		SpringApplication app = new SpringApplication(Example1.class);
		app.setDefaultProperties(properties);

		app.run(args);
	}

	@Bean
	CommandLineRunner run(FactsService factsService) {
		return args -> {
			
			Assert.assertThrows(NotFoundException.class, () -> {
				factsService.getAnyFact();
			});
			
			System.out.println("assertion success");
		};
	}
	
	@Bean
	RestTemplate restTemplate() {
		return new RestTemplate();
	}

}

//===================================== model =============================//

class Fact {

	private String fact;
	private int length;

	public String getFact() {
		return fact;
	}

	public void setFact(String fact) {
		this.fact = fact;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Fact [fact=");
		builder.append(fact);
		builder.append(", length=");
		builder.append(length);
		builder.append("]");
		return builder.toString();
	}
	
}

//=====================================Service=============================//
@Service
class FactsService {
	private static final Logger LOG = LoggerFactory.getLogger(FactsService.class);

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private ObjectMapper mapper;

	public Fact getAnyFact() {

		try {
			//String url = "https://catfact.ninja/fact"; working
			String url = "https://catfact.ninja/factg"; //will return 404

			Fact fact = restTemplate.getForObject(url, Fact.class);
			System.out.println(fact);
			return fact;
		} catch (HttpClientErrorException ex) {
			switch (ex.getStatusCode()) {
			case NOT_FOUND:
				throw new NotFoundException(getErrorMessage(ex));

			case UNPROCESSABLE_ENTITY:
				throw new InvalidInputException(getErrorMessage(ex));

			default:
				LOG.warn("Got an unexpected HTTP error: {}, will rethrow it", ex.getStatusCode());
				LOG.warn("Error body: {}", ex.getResponseBodyAsString());
				throw ex;
			}
		}

	}

	private String getErrorMessage(HttpClientErrorException ex) {
		try {
			return mapper.readValue(ex.getResponseBodyAsString(), HttpErrorInfo.class).getMessage();
		} catch (IOException ioex) {
			return ex.getMessage();
		}
	}

}
//======================================= Exceptions ===========================================//

class InvalidInputException extends RuntimeException {

	public InvalidInputException() {
	}

	public InvalidInputException(String message) {
		super(message);
	}

	public InvalidInputException(String message, Throwable cause) {
		super(message, cause);
	}

	public InvalidInputException(Throwable cause) {
		super(cause);
	}
}

class NotFoundException extends RuntimeException {

	public NotFoundException() {
	}

	public NotFoundException(String message) {
		super(message);
	}

	public NotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

	public NotFoundException(Throwable cause) {
		super(cause);
	}
}
//============================ Utility Models ===========================//

class HttpErrorInfo {
	private final ZonedDateTime timestamp;
	private final String path;
	private final HttpStatus httpStatus;
	private final String message;

	public HttpErrorInfo() {
		timestamp = null;
		this.httpStatus = null;
		this.path = null;
		this.message = null;
	}

	public HttpErrorInfo(HttpStatus httpStatus, String path, String message) {
		timestamp = ZonedDateTime.now();
		this.httpStatus = httpStatus;
		this.path = path;
		this.message = message;
	}

	public ZonedDateTime getTimestamp() {
		return timestamp;
	}

	public String getPath() {
		return path;
	}

	public int getStatus() {
		return httpStatus.value();
	}

	public String getError() {
		return httpStatus.getReasonPhrase();
	}

	public String getMessage() {
		return message;
	}
}