package spring_data.jpa.h2_db.in_memory_based_storage;

import java.util.List;
import java.util.Properties;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.repository.CrudRepository;

@SpringBootApplication
public class Example1 {

	public static void main(String[] args) {

		Properties properties = new Properties();

		// log SQL queries and it's parameters
		properties.put("logging.level.org.hibernate.SQL", "DEBUG");
		properties.put("logging.level.org.hibernate.type.descriptor.sql.BasicBinder", "TRACE");

		// format SQL queries
		properties.put("spring.jpa.properties.hibernate.format_sql", true);

		// with this property you can access the h2 database web console with the
		// following URL:-
		//       http://localhost:8080/h2-console
		properties.put("spring.h2.console.enabled", true);

		// with this property u can override the default URL "h2-console"
		properties.put("spring.h2.console.path", "/my-h2-console");
		
		//use JDBC URL with a fixed database name "mydb" instead of automatically generating it 
		//each time the application starts. u can get the JDBC URL with the generated database name 
		//form the logs. for example you can find the following log:-
		//2022-04-28 21:34:19.514 INFO  [] --- [main] o.s.b.a.h.H2ConsoleAutoConfiguration H2 console available at '/my-h2-console'. Database available at 'jdbc:h2:mem:085fee69-95b8-457e-9d49-a9164545d109'
		properties.put("spring.datasource.url", "jdbc:h2:mem:mydb");


		SpringApplication app = new SpringApplication(Example1.class);
		app.setDefaultProperties(properties);
		app.run(args);

	}

	@Bean
	public CommandLineRunner data(RecommendationRepository repository) {
		return (args) -> {

			// set up
			repository.save(new RecommendationEntity("1", 1, 1, "first author name", 4, "first recomendation content"));
			System.out.println("saved first entity............");

			repository
					.save(new RecommendationEntity("2", 2, 2, "second author name", 5, "second recomendation content"));
			System.out.println("saved second entity............");

			List<RecommendationEntity> recommendation = repository.findByProductId(2);
			System.out.println(">> recommendation: " + recommendation);

		};
	}

}

// ====================== Entities ===========================//

@Entity
@Table(name = "RECOMMENDATION_ENTITY")
class RecommendationEntity {

	@Id
	private String id;

	@Version
	private int version;

	private int productId;
	private int recommendationId;
	private String author;
	private int rate;
	private String content;

	public RecommendationEntity() {

	}

	public RecommendationEntity(String id, int productId, int recommendationId, String author, int rate,
			String content) {
		super();
		this.id = id;
		this.productId = productId;
		this.recommendationId = recommendationId;
		this.author = author;
		this.rate = rate;
		this.content = content;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public int getProductId() {
		return productId;
	}

	public void setProductId(int productId) {
		this.productId = productId;
	}

	public int getRecommendationId() {
		return recommendationId;
	}

	public void setRecommendationId(int recommendationId) {
		this.recommendationId = recommendationId;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public int getRate() {
		return rate;
	}

	public void setRate(int rate) {
		this.rate = rate;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("RecommendationEntity [id=");
		builder.append(id);
		builder.append(", version=");
		builder.append(version);
		builder.append(", productId=");
		builder.append(productId);
		builder.append(", recommendationId=");
		builder.append(recommendationId);
		builder.append(", author=");
		builder.append(author);
		builder.append(", rate=");
		builder.append(rate);
		builder.append(", content=");
		builder.append(content);
		builder.append("]");
		return builder.toString();
	}

}

// ====================== Repositories =====================================//

interface RecommendationRepository extends CrudRepository<RecommendationEntity, String> {
	List<RecommendationEntity> findByProductId(int productId);
}
