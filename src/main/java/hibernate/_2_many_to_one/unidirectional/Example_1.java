package hibernate._2_many_to_one.unidirectional;

import java.util.Properties;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
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
	@GeneratedValue(strategy = GenerationType.TABLE)
	private Long id;
	
	private String title;

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
	
}

@Entity
@Table(name = "post_comment")
class PostComment {
	
	@Id
	@GeneratedValue(strategy = GenerationType.TABLE)
	private Long id;
	
	private String review;
	
	@ManyToOne
	@JoinColumn(name = "post_id")
	private Post post;
	
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
		
}


public class Example_1 {

	public static void main(String [] args) {
		
		Post post = new Post();
		post.setTitle("my post");
		long postId = savePost(post);
		
		saveComment(postId, "my review");
		
	}
	
	private static long savePost(Post post) {
		SessionFactory sf = HibernateUtil.getSessionFactory();
		Session session = sf.openSession();
		session.setHibernateFlushMode(FlushMode.COMMIT);
		                                                 
		Transaction tx = session.beginTransaction();

		long id = (Long) session.save(post);
		
		System.out.println("Flush is triggered before commit time");

		tx.commit();
		session.close();
		
		return id;
	}
	
	
	private static void saveComment(long postId, String review) {
		
		SessionFactory sf = HibernateUtil.getSessionFactory();
		Session session = sf.openSession();
		session.setHibernateFlushMode(FlushMode.COMMIT);
		Transaction tx = session.beginTransaction();

		Post post = session.get(Post.class, postId);
		
		PostComment comment = new PostComment();
		comment.setReview(review);
		comment.setPost(post);
		
		session.save(comment);
		
		comment.setPost(null); //this will trigger an update statement for the post comment
						
		System.out.println("Flush is triggered before commit time");
		tx.commit();
		session.close();
		
	}
}
