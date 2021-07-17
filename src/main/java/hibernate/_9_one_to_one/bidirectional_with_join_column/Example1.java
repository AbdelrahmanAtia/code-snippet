package hibernate._9_one_to_one.bidirectional_with_join_column;

import java.util.Date;
import java.util.Properties;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
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
		properties.put(Environment.HBM2DDL_AUTO, "create-drop");

		Configuration configuration = new Configuration();
		configuration.setProperties(properties);
		configuration.addAnnotatedClass(Post.class);
		configuration.addAnnotatedClass(PostDetails.class);

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
	
	@OneToOne(mappedBy = "post", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private PostDetails postDetails;

	public Post() {

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
	
	public PostDetails getPostDetails() {
		return postDetails;
	}

	public void setPostDetails(PostDetails details) {
		//both sides of the relation should be set
		this.postDetails = details;		
		details.setPost(this);
	}

	@Override
	public String toString() {
		return "Post [id=" + id + ", title=" + title + ", postDetails=" + postDetails + "]";
	}

}

@Entity
@Table(name = "post_details")
class PostDetails {

	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String createdBy;

	private Date createdOn;

	@OneToOne
	@JoinColumn(name="post_id")
	private Post post;

	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public Date getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(Date createdOn) {
		this.createdOn = createdOn;
	}

	public Post getPost() {
		return post;
	}

	public void setPost(Post post) {
		this.post = post;
	}

	@Override
	public String toString() {
		return "PostDetails [id=" + id + ", createdBy=" + createdBy + ", createdOn=" + createdOn + "]";
	}
	
}

public class Example1 {

	public static void main(String[] args) {

		// creates a post & postDetails and saves it to the DB
		setUp();
		
		long postId = 1L;
		Post thePost = getPost(postId);

		System.out.println("retrieved post: " + thePost);
	}

	

	public static void setUp() {

		SessionFactory sf = HibernateUtil.getSessionFactory();
		Session session = sf.openSession();
		session.setHibernateFlushMode(FlushMode.COMMIT);
		Transaction tx = session.beginTransaction();

		Post post = new Post();
		post.setId(1L);
		post.setTitle("My First Post");
		
		PostDetails postDetails = new PostDetails();
		postDetails.setCreatedBy("Abdelrahman");
		postDetails.setCreatedOn(new Date());
		
		post.setPostDetails(postDetails);
		
		session.save(post);

		System.out.println("Flush is triggered before commit time");
		tx.commit();
		session.close();

		System.out.println("*************** setup done ****************");

	}	
	
	private static Post getPost(long postId) {
		SessionFactory sf = HibernateUtil.getSessionFactory();
		Session session = sf.openSession();
		session.setHibernateFlushMode(FlushMode.COMMIT);
		Transaction tx = session.beginTransaction();
		
		Post post = session.find(Post.class, postId);
				
		System.out.println("Flush is triggered before commit time");
		tx.commit();
		session.close();

		System.out.println("*************** setup done ****************");

		return post;
	}

}
