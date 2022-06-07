package microservices.example_1.ms_recommendation;

import java.util.Properties;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import org.springframework.web.bind.annotation.ExceptionHandler;
/*import se.magnus.api.core.recommendation.Recommendation;
import se.magnus.api.core.recommendation.RecommendationService;
import se.magnus.api.exceptions.InvalidInputException;
import se.magnus.util.http.ServiceUtil;*/
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@SpringBootApplication
//@ComponentScan("se.magnus")
public class RecommendationServiceApplication {

	public static void main(String[] args) {
		// SpringApplication.run(RecommendationServiceApplication.class, args);

		Properties properties = new Properties();
		properties.put("server.port", 7002);
		properties.put("logging.level.root", "INFO");

		SpringApplication app = new SpringApplication(RecommendationServiceApplication.class);
		app.setWebApplicationType(WebApplicationType.REACTIVE);
		app.setDefaultProperties(properties);
		app.run(args);
	}
}

//===========================  DTOs ==========================//

class Recommendation {
	private final int productId;
	private final int recommendationId;
	private final String author;
	private final int rate;
	private final String content;
	private final String serviceAddress;

	public Recommendation() {
		productId = 0;
		recommendationId = 0;
		author = null;
		rate = 0;
		content = null;
		serviceAddress = null;
	}

	public Recommendation(int productId, int recommendationId, String author, int rate, String content,
			String serviceAddress) {

		this.productId = productId;
		this.recommendationId = recommendationId;
		this.author = author;
		this.rate = rate;
		this.content = content;
		this.serviceAddress = serviceAddress;
	}

	public int getProductId() {
		return productId;
	}

	public int getRecommendationId() {
		return recommendationId;
	}

	public String getAuthor() {
		return author;
	}

	public int getRate() {
		return rate;
	}

	public String getContent() {
		return content;
	}

	public String getServiceAddress() {
		return serviceAddress;
	}
}

//============================ APIs ==========================================//

interface RecommendationService {

	/**
	 * Sample usage: "curl $HOST:$PORT/recommendation?productId=1".
	 *
	 * @param productId Id of the product
	 * @return the recommendations of the product
	 */
	@GetMapping(value = "/recommendation", produces = "application/json")
	List<Recommendation> getRecommendations(@RequestParam(value = "productId", required = true) int productId);
}

@RestController
class RecommendationServiceImpl implements RecommendationService {

	private static final Logger LOG = LoggerFactory.getLogger(RecommendationServiceImpl.class);

	private final ServiceUtil serviceUtil;

	@Autowired
	public RecommendationServiceImpl(ServiceUtil serviceUtil) {
		this.serviceUtil = serviceUtil;
	}

	@Override
	public List<Recommendation> getRecommendations(int productId) {

		if (productId < 1) {
			throw new InvalidInputException("Invalid productId: " + productId);
		}

		if (productId == 113) {
			LOG.debug("No recommendations found for productId: {}", productId);
			return new ArrayList<>();
		}

		List<Recommendation> list = new ArrayList<>();
		list.add(new Recommendation(productId, 1, "Author 1", 1, "Content 1", serviceUtil.getServiceAddress()));
		list.add(new Recommendation(productId, 2, "Author 2", 2, "Content 2", serviceUtil.getServiceAddress()));
		list.add(new Recommendation(productId, 3, "Author 3", 3, "Content 3", serviceUtil.getServiceAddress()));

		LOG.debug("/recommendation response size: {}", list.size());

		return list;
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

//in real life, this utility classes should be added to a jar and shared between different micro-services
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