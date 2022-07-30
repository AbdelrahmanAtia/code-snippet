package spring_data.jpa.compound_index;

import java.util.Properties;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.persistence.Version;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
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
		// http://localhost:8080/h2-console
		properties.put("spring.h2.console.enabled", true);

		// with this property u can override the default URL "h2-console"
		properties.put("spring.h2.console.path", "/my-h2-console");

		// using file storage instead of memory storage
		// file storage is non-voltile unlike the memory storage
		// the database state will be stored in the following file "h2database.mv.db" in
		// the path specified after "file:"
		properties.put("spring.datasource.url",
				"jdbc:h2:file:C:\\Users\\ayoussef4\\OneDrive - DXC Production\\Desktop\\h2database");

		// automatically drops the database tables if they exist and
		// then recreates them
		properties.put("spring.jpa.hibernate.ddl-auto", "create-drop");

		SpringApplication app = new SpringApplication(Example1.class);
		app.setDefaultProperties(properties);
		app.run(args);

	}

}

// ====================== Entities ===========================//

/*
 * u will see in the logs that there is a query to create the index:- 
    >> alter table reviews 
       			add constraint reviews_unique_idx unique (product_id, review_id)
 */

@Entity
@Table(name = "reviews", indexes = {
		@Index(name = "reviews_unique_idx", unique = true, columnList = "productId,reviewId") })
class ReviewEntity {

	@Id
	@GeneratedValue
	private int id;

	@Version
	private int version;

	private int productId;
	private int reviewId;
	private String author;
	private String subject;
	private String content;

	public int getId() {
		return id;
	}

	public void setId(int id) {
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

	public int getReviewId() {
		return reviewId;
	}

	public void setReviewId(int reviewId) {
		this.reviewId = reviewId;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

}

// ====================== Repositories =====================================//

interface ReviewRepository extends CrudRepository<ReviewEntity, Integer> {

}
