package hibernate._13_hibernate_statistics;

import java.util.Properties;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;
import javax.sql.DataSource;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jms.activemq.ActiveMQAutoConfiguration;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import common.constants.Constant;

@Entity
@Table(name = "post")
class Post {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String title;

	@Version
	private long version;

	public Post() {

	}

	public Post(Long id) {
		this.id = id;
	}

	public Post(String title) {
		this.title = title;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public long getVersion() {
		return version;
	}

	public void setVersion(long version) {
		this.version = version;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Post [id=");
		builder.append(id);
		builder.append(", title=");
		builder.append(title);
		builder.append(", version=");
		builder.append(version);
		builder.append("]");
		builder.append("\n");
		return builder.toString();
	}

}

@Repository
interface PostRepository extends JpaRepository<Post, Long> {

}

@Service
class PostService {
	
	@Lazy
	@Autowired
	private PostRepository postRepository;
	
	@Transactional
	public void addPost(Post post) {
		postRepository.save(post);
	}
	
}

@Configuration
class JpaConfig {

	@Bean
	public DataSource getDataSource() {
		DataSourceBuilder<?> dataSourceBuilder = DataSourceBuilder.create();
		// dataSourceBuilder.driverClassName("org.h2.Driver");
		dataSourceBuilder.url(Constant.DB_URL);
		dataSourceBuilder.username(Constant.DB_USERNAME);
		dataSourceBuilder.password(Constant.DB_PASSWORD);
		return dataSourceBuilder.build();
	}
}


@Component
class CommandLineAppStartupRunner implements CommandLineRunner {

	@Autowired
	private PostService postService;

	@Override
	public void run(String... args) throws Exception {

		Post p1 = new Post("this is my first post");
		Post p2 = new Post("this is my second post");
		
		postService.addPost(p1);
		postService.addPost(p2);
		
	}
}

@SpringBootApplication(exclude = ActiveMQAutoConfiguration.class)
public class Example1 {

	public static void main(String[] args) {

		// we can also set these properties throug a 
		Properties properties = new Properties();
		properties.put("spring.jpa.hibernate.ddl-auto", "create-drop"); 
		//properties.put("spring.jpa.show-sql", true); 
		properties.put("logging.level.org.hibernate.SQL", "DEBUG");
		
		//this will print the statistics of each session
		properties.put("spring.jpa.properties.hibernate.generate_statistics", true); 
		
		/**
		 * this property org.hibernate.stat is supposed to print the time of 
		 * each query and the number of rows affected as follows:
		 * HQL: insert into post (title, version) values (?, ?) , time: 67ms, rows: 56
		 * but it is not working these are the links that i checked:-
		 *  1- https://thorben-janssen.com/how-to-activate-hibernate-statistics-to-analyze-performance-issues/
		 *  2- https://marktjbrown.com/hibernate-statistics
		 *  3- https://www.baeldung.com/hibernate-logging-levels
		 *  4- https://thorben-janssen.com/hibernate-tips-log-execution-time-query/
		 *  5- https://stackoverflow.com/questions/40357402/hibernate-log-using-hibernate-stat
		 * 
		 */
		//properties.put("logging.level.org.hibernate.stat", "DEBUG"); //not working!!
		
		SpringApplication app = new SpringApplication(Example1.class);
		app.setDefaultProperties(properties);
		app.run(args);
	}

}
