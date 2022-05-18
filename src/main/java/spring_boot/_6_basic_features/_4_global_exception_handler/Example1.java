package spring_boot._6_basic_features._4_global_exception_handler;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;

import java.time.ZonedDateTime;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@SpringBootApplication
public class Example1 {

	public static void main(String[] args) {
		Properties properties = new Properties();
		properties.put("server.port", 7001);

		SpringApplication app = new SpringApplication(Example1.class);
		app.setDefaultProperties(properties);

		// adding both web and webflux starters will configure a Spring MVC web
		// application.
		// so, if we need to force it to be reactive app, then we have to
		// set the app type to WebApplicationType.REACTIVE
		app.setWebApplicationType(WebApplicationType.REACTIVE);

		app.run(args);
	}

}

//===========================  DTOs ==========================//
class Product {

	private final int productId;
	private final String name;
	private final int weight;

	public Product() {
		productId = 0;
		name = null;
		weight = 0;
	}

	public Product(int productId, String name, int weight) {
		this.productId = productId;
		this.name = name;
		this.weight = weight;
	}

	public int getProductId() {
		return productId;
	}

	public String getName() {
		return name;
	}

	public int getWeight() {
		return weight;
	}
}

//============================ APIs ==========================================//

interface ProductService {

	/**
	 * Sample usage: "curl $HOST:$PORT/product/1". <br>
	 * for example:- <br>
	 * curl http://localhost:7001/product/14
	 *
	 * @param productId Id of the product
	 * @return the product, if found, else null
	 */
	@GetMapping(value = "/product/{productId}", produces = "application/json")
	Product getProduct(@PathVariable int productId);
}

@RestController
class ProductServiceImpl implements ProductService {

	private static final Logger LOG = LoggerFactory.getLogger(ProductServiceImpl.class);

	@Override
	public Product getProduct(int productId) {
		LOG.debug("/product return the found product for productId={}", productId);

		if (productId < 1) {
			throw new InvalidInputException("Invalid productId: " + productId);
		}

		if (productId == 13) {
			throw new NotFoundException("No product found for productId: " + productId);
		}

		return new Product(productId, "name-" + productId, 123);
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

//===================== Global Exception Handler =============================//
@RestControllerAdvice
class GlobalControllerExceptionHandler {

	private static final Logger LOG = LoggerFactory.getLogger(GlobalControllerExceptionHandler.class);

	//for non-reactive app use the following  
	// org.springframework.http.server.ServerHttpRequest
	//instead of org.springframework.http.server.reactive.ServerHttpRequest
	@ResponseStatus(NOT_FOUND)
	@ExceptionHandler(NotFoundException.class)
	public @ResponseBody HttpErrorInfo handleNotFoundExceptions(ServerHttpRequest request, NotFoundException ex) {

		return createHttpErrorInfo(NOT_FOUND, request, ex);
	}

	@ResponseStatus(UNPROCESSABLE_ENTITY)
	@ExceptionHandler(InvalidInputException.class)
	public @ResponseBody HttpErrorInfo handleInvalidInputException(ServerHttpRequest request,
			InvalidInputException ex) {

		return createHttpErrorInfo(UNPROCESSABLE_ENTITY, request, ex);
	}

	private HttpErrorInfo createHttpErrorInfo(HttpStatus httpStatus, ServerHttpRequest request, Exception ex) {

		final String path = request.getPath().pathWithinApplication().value();
		final String message = ex.getMessage();

		LOG.debug("Returning HTTP status: {} for path: {}, message: {}", httpStatus, path, message);
		return new HttpErrorInfo(httpStatus, path, message);
	}
}