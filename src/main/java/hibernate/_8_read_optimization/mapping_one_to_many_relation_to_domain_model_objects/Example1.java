package hibernate._8_read_optimization.mapping_one_to_many_relation_to_domain_model_objects;


import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Consumer;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Version;

import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.service.ServiceRegistry;

import common.constants.Constant;

class HibernateUtil {

	private static SessionFactory sessionFactory;

	public static SessionFactory getSessionFactory() {

		if (sessionFactory != null)
			return sessionFactory;

		Properties properties = new Properties();
		properties.put(Environment.URL, Constant.DB_URL);
		properties.put(Environment.USER, Constant.DB_USERNAME);
		properties.put(Environment.PASS, Constant.DB_PASSWORD);
		properties.put(Environment.DIALECT, "org.hibernate.dialect.MySQL8Dialect");

		properties.put(Environment.SHOW_SQL, "true");
		properties.put(Environment.HBM2DDL_AUTO, "create");

		Configuration configuration = new Configuration();
		configuration.setProperties(properties);

		configuration.addAnnotatedClass(Post.class);
		configuration.addAnnotatedClass(PostComment.class);
		
		ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
				.applySettings(configuration.getProperties()).build();

		sessionFactory = configuration.buildSessionFactory(serviceRegistry);
		return sessionFactory;

	}
}

@Entity
@Table(name = "post")
class Post {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String title;

	@OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<PostComment> comments = new ArrayList<>();

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

	public List<PostComment> getComments() {
		return comments;
	}

	public void setComments(List<PostComment> comments) {
		this.comments = comments;
	}

	public void addComment(PostComment comment) {
		comments.add(comment);
		comment.setPost(this);
	}

	public void removeComment(PostComment comment) {
		comments.remove(comment);
		comment.setPost(null);
	}

	public long getVersion() {
		return version;
	}

	public void setVersion(long version) {
		this.version = version;
	}

}

@Entity
@Table(name = "post_comment")
class PostComment {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String review;

	@ManyToOne
	@JoinColumn(name = "post_id")
	private Post post;

	@Version
	private long version;

	public PostComment() {

	}

	public PostComment(String review) {
		this.review = review;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getReview() {
		return review;
	}

	public void setReview(String review) {
		this.review = review;
	}

	public Post getPost() {
		return post;
	}

	public void setPost(Post post) {
		this.post = post;
	}

	public long getVersion() {
		return version;
	}

	public void setVersion(long version) {
		this.version = version;
	}

}

public class Example1 {

	public static void main(String[] args) {

		setUp();
		
		int id = 1;
		int expectedCount = 2;
		
		doInJDBC(connection -> {
			try (PreparedStatement statement = connection.prepareStatement(
					"SELECT * FROM post AS p "
					+ "JOIN post_comment AS pc " 
					+ "ON p.id = pc.post_id " 
					+ "WHERE p.id BETWEEN ? AND ? + 1")) {

				statement.setInt(1, id);
				statement.setInt(2, id);

				try (ResultSet resultSet = statement.executeQuery()) {
					List<Post> posts = toPosts(resultSet);
					assertEquals(expectedCount, posts.size());
				}

			} catch (SQLException e) {
				e.printStackTrace();
			}

		});
		
		
		
		doInJPA(session -> {
			List<Post> posts = session.createQuery(
					" select distinct p " + "from Post p "
				  + " join fetch p.comments " + "where " 
				  + " p.id BETWEEN :id AND :id + 1", Post.class)
					.setParameter("id", (long)id).getResultList();
			
			assertEquals(expectedCount, posts.size());
		});

	}	
	
	private static void setUp() {
		
		System.out.println("********************* starting setUp() *********************");

		SessionFactory sf = HibernateUtil.getSessionFactory();
		Session session = sf.openSession();
		session.setHibernateFlushMode(FlushMode.COMMIT);
		Transaction tx = session.beginTransaction();
		
		//create first post
		Post post = new Post("My Post");
		PostComment comment1 = new PostComment("my first review");
		PostComment comment2 = new PostComment("my second review");		
		post.addComment(comment1);
		post.addComment(comment2);
		
		//create second post
		Post post2 = new Post("My Post");
		PostComment comment3 = new PostComment("my first review");
		PostComment comment4 = new PostComment("my second review");
		post2.addComment(comment3);
		post2.addComment(comment4);
		
		session.persist(post);
		session.persist(post2);

		
		System.out.println("Flush is triggered before commit time");
		
		tx.commit();
		session.close();
		
	}

	private static List<Post> toPosts(ResultSet resultSet) throws SQLException {
		Map<Long, Post> postMap = new LinkedHashMap<>();
		while (resultSet.next()) {

			Long postId = resultSet.getLong(1);
			Post post = postMap.get(postId);

			if (post == null) {
				post = new Post(postId);
				postMap.put(postId, post);
				post.setTitle(resultSet.getString(2));
				post.setVersion(resultSet.getInt(3));
			}

			PostComment comment = new PostComment();
			comment.setId(resultSet.getLong(4));
			comment.setReview(resultSet.getString(5));
			comment.setVersion(resultSet.getInt(6));
			post.addComment(comment);
		}

		return new ArrayList<>(postMap.values());
	}

	private static void doInJDBC(Consumer<Connection> consumer) {
		try {
			Connection connection = DriverManager.getConnection(Constant.DB_URL, Constant.DB_USERNAME,
					Constant.DB_PASSWORD);
			consumer.accept(connection);	
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}
	
	private static void doInJPA(Consumer<Session> consumer) {
		SessionFactory sf = HibernateUtil.getSessionFactory();
		Session session = sf.openSession();
		session.setHibernateFlushMode(FlushMode.COMMIT);
		Transaction tx = session.beginTransaction();
		
		consumer.accept(session);

		System.out.println("Flush is triggered before commit time");
		tx.commit();
		session.close();

	}

}
