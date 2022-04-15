package spring_data.jpa.composite_key._1_using_id_class_annotation;

import java.io.Serializable;
import java.util.Collection;
import java.util.Optional;
import java.util.Properties;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.repository.CrudRepository;

@SpringBootApplication
public class Example1 {

	public static void main(String[] args) {

		Properties properties = new Properties();
		properties.put("spring.jpa.show-sql", true);
		// properties.put("logging.level.org.hibernate.SQL", "DEBUG");

		// SpringApplication.run(Example1.class, args);
		SpringApplication app = new SpringApplication(Example1.class);
		app.setDefaultProperties(properties);
		app.run(args);
	}

	@Bean
	public CommandLineRunner data(ReviewRepository repository) {
		return (args) -> {
			// set up
			repository.save(new ReviewEntity(1, 1, "marcus", null, "very good book"));
			repository.save(new ReviewEntity(1, 2, "marcus", null, "very easy to understand"));
			repository.save(new ReviewEntity(2, 1, "marcus", null, "highly recommended"));
			repository.save(new ReviewEntity(2, 2, "marcus", null, "nice tutorials"));

			// retrieve by composite primary key

			Optional<ReviewEntity> optionalReviewEntity = repository.findById(new ReviewEntityPK(2, 1));

			optionalReviewEntity.ifPresent(obj -> {
				System.out.println(obj);
				org.junit.Assert.assertEquals("highly recommended", obj.getContent());

			});

		};
	}
}

//====================== Entities ===========================//

/**
 * composite primary key class..it should follow the following rules:- 1- It
 * must have a no-arg constructor. 2- It must be Serializable. 3- It must define
 * the equals() and hashCode() methods.
 */
class ReviewEntityPK implements Serializable {

	private static final long serialVersionUID = 1L;
	public int productId;
	public int reviewId;

	public ReviewEntityPK() {

	}

	public ReviewEntityPK(int productId, int reviewId) {
		this.productId = productId;
		this.reviewId = reviewId;
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

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ReviewEntityPK [productId=");
		builder.append(productId);
		builder.append(", reviewId=");
		builder.append(reviewId);
		builder.append("]");
		return builder.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + productId;
		result = prime * result + reviewId;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ReviewEntityPK other = (ReviewEntityPK) obj;
		if (productId != other.productId)
			return false;
		if (reviewId != other.reviewId)
			return false;
		return true;
	}

}

@Entity
@IdClass(ReviewEntityPK.class)
@Table(name = "review")
class ReviewEntity {

	@Id
	private int productId;

	@Id
	private int reviewId;

	private String author;
	private String subject;
	private String content;

	public ReviewEntity() {

	}

	public ReviewEntity(int productId, int reviewId, String author, String subject, String content) {
		this.productId = productId;
		this.reviewId = reviewId;
		this.author = author;
		this.subject = subject;
		this.content = content;
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

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ReviewEntity [productId=");
		builder.append(productId);
		builder.append(", reviewId=");
		builder.append(reviewId);
		builder.append(", author=");
		builder.append(author);
		builder.append(", subject=");
		builder.append(subject);
		builder.append(", content=");
		builder.append(content);
		builder.append("]");
		return builder.toString();
	}

}

//====================== Repositories =======================//

interface ReviewRepository extends CrudRepository<ReviewEntity, ReviewEntityPK> {

	Collection<ReviewEntity> findByProductId(int productId);
}
