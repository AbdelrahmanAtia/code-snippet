package microservices.example_1.ms_product;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.ZonedDateTime;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
/*import se.magnus.api.core.product.Product;
import se.magnus.api.core.product.ProductService;
import se.magnus.api.exceptions.InvalidInputException;
import se.magnus.api.exceptions.NotFoundException;
import se.magnus.util.http.ServiceUtil;
import se.magnus.api.exceptions.InvalidInputException;
import se.magnus.api.exceptions.NotFoundException;
*/
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;

import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@SpringBootApplication
//@ComponentScan("se.magnus")
public class ProductServiceApplication {

	public static void main(String[] args) {
		Properties properties = new Properties();
		properties.put("server.port", 7001);
		properties.put("logging.level.root", "INFO");

		SpringApplication app = new SpringApplication(ProductServiceApplication.class);
		app.setWebApplicationType(WebApplicationType.REACTIVE);
		app.setDefaultProperties(properties);
		app.run(args);
	}

	/*
	 * @Bean CommandLineRunner simple(ServiceUtil serviceUtil) { return args -> {
	 * System.out.println("==================================");
	 * System.out.println(serviceUtil.getServiceAddress());
	 * System.out.println("=================================="); }; }
	 */
}

//=========================== API DTO ==========================//
class Product {

	private final int productId;
	private final String name;
	private final int weight;
	private final String serviceAddress;

	public Product() {
		productId = 0;
		name = null;
		weight = 0;
		serviceAddress = null;
	}

	public Product(int productId, String name, int weight, String serviceAddress) {
		this.productId = productId;
		this.name = name;
		this.weight = weight;
		this.serviceAddress = serviceAddress;
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

	public String getServiceAddress() {
		return serviceAddress;
	}
}

//============================ APIs ==========================================//

interface ProductService {

	/**
	 * Sample usage: "curl $HOST:$PORT/product/1".
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

	private final ServiceUtil serviceUtil;

	@Autowired
	public ProductServiceImpl(ServiceUtil serviceUtil) {
		this.serviceUtil = serviceUtil;
	}

	@Override
	public Product getProduct(int productId) {
		LOG.debug("/product return the found product for productId={}", productId);

		if (productId < 1) {
			throw new InvalidInputException("Invalid productId: " + productId);
		}

		if (productId == 13) {
			throw new NotFoundException("No product found for productId: " + productId);
		}

		return new Product(productId, "name-" + productId, 123, serviceUtil.getServiceAddress());
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

//in real life, this utility class should be added to a jar and shared between different micro-services
//============================ Utility classes ===========================//

@Component
class ServiceUtil {
	private static final Logger LOG = LoggerFactory.getLogger(ServiceUtil.class);

	private final String port;

	private String serviceAddress = null;

	@Autowired
	public ServiceUtil(@Value("${server.port}") String port) {
		this.port = port;
	}

	public String getServiceAddress() {
		if (serviceAddress == null) {
			serviceAddress = findMyHostname() + "/" + findMyIpAddress() + ":" + port;
		}
		return serviceAddress;
	}

	private String findMyHostname() {
		try {
			return InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			return "unknown host name";
		}
	}

	private String findMyIpAddress() {
		try {
			return InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			return "unknown IP address";
		}
	}
}

//in real life, this model should be added to a jar and shared between different micro-services
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

//in real life, this exception handler should be added to a jar and shared between different micro-services
//===================== Global Exception Handler =============================//
@RestControllerAdvice
class GlobalControllerExceptionHandler {

	private static final Logger LOG = LoggerFactory.getLogger(GlobalControllerExceptionHandler.class);

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