package microservices.example_1.ms_product_composite;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ExceptionHandler;
/*import se.magnus.api.composite.product.*;
import se.magnus.api.core.product.Product;
import se.magnus.api.core.recommendation.Recommendation;
import se.magnus.api.core.review.Review;
import se.magnus.api.exceptions.NotFoundException;
import se.magnus.util.http.ServiceUtil;
import se.magnus.api.core.product.Product;
import se.magnus.api.core.product.ProductService;
import se.magnus.api.core.recommendation.Recommendation;
import se.magnus.api.core.recommendation.RecommendationService;
import se.magnus.api.core.review.Review;
import se.magnus.api.core.review.ReviewService;
import se.magnus.api.exceptions.InvalidInputException;
import se.magnus.api.exceptions.NotFoundException;
import se.magnus.util.http.HttpErrorInfo;
*/
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootApplication
//@ComponentScan("se.magnus")
public class ProductCompositeServiceApplication {

	public static void main(String[] args) {
		// SpringApplication.run(ProductCompositeServiceApplication.class, args);

		Properties properties = new Properties();
		properties.put("server.port", 7000);
		
		properties.put("app.product-service.host", "localhost");
		properties.put("app.product-service.port", "7001");
		
		properties.put("app.recommendation-service.host", "localhost");
		properties.put("app.recommendation-service.port", "7002");
		
		properties.put("app.review-service.host", "localhost");
		properties.put("app.review-service.port", "7003");

		SpringApplication app = new SpringApplication(ProductCompositeServiceApplication.class);
		app.setWebApplicationType(WebApplicationType.REACTIVE);
		app.setDefaultProperties(properties);
		app.run(args);
		
	}

	@Bean
	RestTemplate restTemplate() {
		return new RestTemplate();
	}

}

//===========================  API DTO ==========================//

class ProductAggregate {
	private final int productId;
	private final String name;
	private final int weight;
	private final List<RecommendationSummary> recommendations;
	private final List<ReviewSummary> reviews;
	private final ServiceAddresses serviceAddresses;

	public ProductAggregate(int productId, String name, int weight, List<RecommendationSummary> recommendations,
			List<ReviewSummary> reviews, ServiceAddresses serviceAddresses) {

		this.productId = productId;
		this.name = name;
		this.weight = weight;
		this.recommendations = recommendations;
		this.reviews = reviews;
		this.serviceAddresses = serviceAddresses;
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

	public List<RecommendationSummary> getRecommendations() {
		return recommendations;
	}

	public List<ReviewSummary> getReviews() {
		return reviews;
	}

	public ServiceAddresses getServiceAddresses() {
		return serviceAddresses;
	}
}

class RecommendationSummary {

	private final int recommendationId;
	private final String author;
	private final int rate;

	public RecommendationSummary(int recommendationId, String author, int rate) {
		this.recommendationId = recommendationId;
		this.author = author;
		this.rate = rate;
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
}

class ReviewSummary {

	private final int reviewId;
	private final String author;
	private final String subject;

	public ReviewSummary(int reviewId, String author, String subject) {
		this.reviewId = reviewId;
		this.author = author;
		this.subject = subject;
	}

	public int getReviewId() {
		return reviewId;
	}

	public String getAuthor() {
		return author;
	}

	public String getSubject() {
		return subject;
	}
}

class ServiceAddresses {
	private final String cmp;
	private final String pro;
	private final String rev;
	private final String rec;

	public ServiceAddresses() {
		cmp = null;
		pro = null;
		rev = null;
		rec = null;
	}

	public ServiceAddresses(String compositeAddress, String productAddress, String reviewAddress,
			String recommendationAddress) {

		this.cmp = compositeAddress;
		this.pro = productAddress;
		this.rev = reviewAddress;
		this.rec = recommendationAddress;
	}

	public String getCmp() {
		return cmp;
	}

	public String getPro() {
		return pro;
	}

	public String getRev() {
		return rev;
	}

	public String getRec() {
		return rec;
	}
}

//========================== CLI DTO ===================================//

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

class Review {
	private final int productId;
	private final int reviewId;
	private final String author;
	private final String subject;
	private final String content;
	private final String serviceAddress;

	public Review() {
		productId = 0;
		reviewId = 0;
		author = null;
		subject = null;
		content = null;
		serviceAddress = null;
	}

	public Review(int productId, int reviewId, String author, String subject, String content, String serviceAddress) {

		this.productId = productId;
		this.reviewId = reviewId;
		this.author = author;
		this.subject = subject;
		this.content = content;
		this.serviceAddress = serviceAddress;
	}

	public int getProductId() {
		return productId;
	}

	public int getReviewId() {
		return reviewId;
	}

	public String getAuthor() {
		return author;
	}

	public String getSubject() {
		return subject;
	}

	public String getContent() {
		return content;
	}

	public String getServiceAddress() {
		return serviceAddress;
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

//============================ APIs ==========================================//

interface ProductCompositeService {

	/**
	 * Sample usage: "curl $HOST:$PORT/product-composite/1".
	 *
	 * @param productId Id of the product
	 * @return the composite product info, if found, else null
	 */
	@GetMapping(value = "/product-composite/{productId}", produces = "application/json")
	ProductAggregate getProduct(@PathVariable int productId);
}

@RestController
class ProductCompositeServiceImpl implements ProductCompositeService {

	private final ServiceUtil serviceUtil;
	private ProductCompositeIntegration integration;

	@Autowired
	public ProductCompositeServiceImpl(ServiceUtil serviceUtil, ProductCompositeIntegration integration) {
		this.serviceUtil = serviceUtil;
		this.integration = integration;
	}

	@Override
	public ProductAggregate getProduct(int productId) {

		Product product = integration.getProduct(productId);
		if (product == null) {
			throw new NotFoundException("No product found for productId: " + productId);
		}

		List<Recommendation> recommendations = integration.getRecommendations(productId);

		List<Review> reviews = integration.getReviews(productId);

		return createProductAggregate(product, recommendations, reviews, serviceUtil.getServiceAddress());
	}

	private ProductAggregate createProductAggregate(Product product, List<Recommendation> recommendations,
			List<Review> reviews, String serviceAddress) {

		// 1. Setup product info
		int productId = product.getProductId();
		String name = product.getName();
		int weight = product.getWeight();

		// 2. Copy summary recommendation info, if available
		List<RecommendationSummary> recommendationSummaries = (recommendations == null) ? null
				: recommendations.stream()
						.map(r -> new RecommendationSummary(r.getRecommendationId(), r.getAuthor(), r.getRate()))
						.collect(Collectors.toList());

		// 3. Copy summary review info, if available
		List<ReviewSummary> reviewSummaries = (reviews == null) ? null
				: reviews.stream().map(r -> new ReviewSummary(r.getReviewId(), r.getAuthor(), r.getSubject()))
						.collect(Collectors.toList());

		// 4. Create info regarding the involved microservices addresses
		String productAddress = product.getServiceAddress();
		String reviewAddress = (reviews != null && reviews.size() > 0) ? reviews.get(0).getServiceAddress() : "";
		String recommendationAddress = (recommendations != null && recommendations.size() > 0)
				? recommendations.get(0).getServiceAddress()
				: "";
		ServiceAddresses serviceAddresses = new ServiceAddresses(serviceAddress, productAddress, reviewAddress,
				recommendationAddress);

		return new ProductAggregate(productId, name, weight, recommendationSummaries, reviewSummaries,
				serviceAddresses);
	}
}

//============================CLI API==========================================//

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

interface ReviewService {

	/**
	 * Sample usage: "curl $HOST:$PORT/review?productId=1".
	 *
	 * @param productId Id of the product
	 * @return the reviews of the product
	 */
	@GetMapping(value = "/review", produces = "application/json")
	List<Review> getReviews(@RequestParam(value = "productId", required = true) int productId);
}

//============================ Services ==========================================//

@Component
class ProductCompositeIntegration implements ProductService, RecommendationService, ReviewService {

	private static final Logger LOG = LoggerFactory.getLogger(ProductCompositeIntegration.class);

	private final RestTemplate restTemplate;
	private final ObjectMapper mapper;

	private final String productServiceUrl;
	private final String recommendationServiceUrl;
	private final String reviewServiceUrl;

	@Autowired
	public ProductCompositeIntegration(RestTemplate restTemplate, ObjectMapper mapper,
			@Value("${app.product-service.host}") String productServiceHost,
			@Value("${app.product-service.port}") int productServicePort,
			@Value("${app.recommendation-service.host}") String recommendationServiceHost,
			@Value("${app.recommendation-service.port}") int recommendationServicePort,
			@Value("${app.review-service.host}") String reviewServiceHost,
			@Value("${app.review-service.port}") int reviewServicePort) {

		this.restTemplate = restTemplate;
		this.mapper = mapper;

		productServiceUrl = "http://" + productServiceHost + ":" + productServicePort + "/product/";
		recommendationServiceUrl = "http://" + recommendationServiceHost + ":" + recommendationServicePort
				+ "/recommendation?productId=";
		reviewServiceUrl = "http://" + reviewServiceHost + ":" + reviewServicePort + "/review?productId=";
	}

	public Product getProduct(int productId) {

		try {
			String url = productServiceUrl + productId;
			LOG.debug("Will call getProduct API on URL: {}", url);

			Product product = restTemplate.getForObject(url, Product.class);
			LOG.debug("Found a product with id: {}", product.getProductId());

			return product;

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

	public List<Recommendation> getRecommendations(int productId) {

		try {
			String url = recommendationServiceUrl + productId;

			LOG.debug("Will call getRecommendations API on URL: {}", url);
			List<Recommendation> recommendations = restTemplate
					.exchange(url, GET, null, new ParameterizedTypeReference<List<Recommendation>>() {
					}).getBody();

			LOG.debug("Found {} recommendations for a product with id: {}", recommendations.size(), productId);
			return recommendations;

		} catch (Exception ex) {
			LOG.warn("Got an exception while requesting recommendations, return zero recommendations: {}",
					ex.getMessage());
			return new ArrayList<>();
		}
	}

	public List<Review> getReviews(int productId) {

		try {
			String url = reviewServiceUrl + productId;

			LOG.debug("Will call getReviews API on URL: {}", url);
			List<Review> reviews = restTemplate
					.exchange(url, GET, null, new ParameterizedTypeReference<List<Review>>() {
					}).getBody();

			LOG.debug("Found {} reviews for a product with id: {}", reviews.size(), productId);
			return reviews;

		} catch (Exception ex) {
			LOG.warn("Got an exception while requesting reviews, return zero reviews: {}", ex.getMessage());
			return new ArrayList<>();
		}
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
