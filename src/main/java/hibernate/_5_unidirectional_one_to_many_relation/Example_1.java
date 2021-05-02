package hibernate._5_unidirectional_one_to_many_relation;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
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
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String title;

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	private List<PostComment> comments = new ArrayList<>();
	
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

	public List<PostComment> getComments() {
		return comments;
	}

	public void setComments(List<PostComment> comments) {
		this.comments = comments;
	}
	
	/*

	// no need for helper methods in unidirectional one-to-many relation
	
	public void addComment(PostComment comment) {
		comments.add(comment);
		comment.setPost(this);
	}
	
	public void removeComment(PostComment comment) {
		comments.remove(comment);
		comment.setPost(null);
	}
	
	*/

}


@Entity
@Table(name = "post_comment")
class PostComment {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String review;
	
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

}



public class Example_1 {
	
	public static void main(String [] args) {
		
		
		//write your code here
		
		Post post = new Post("First Post");
		
		post.getComments().add(new PostComment("My first review"));
		post.getComments().add(new PostComment("My second review"));
		post.getComments().add(new PostComment("My third review"));
		
		long postId = savePost(post);
		removeFirstComment(postId);		
		
	}
	
	
	public static long savePost(Post post) {
		SessionFactory sf = HibernateUtil.getSessionFactory();
		Session session = sf.openSession();
		session.setHibernateFlushMode(FlushMode.COMMIT);
		Transaction tx = session.beginTransaction();
		
		long postId = (long) session.save(post);		
		
		System.out.println("Flush is triggered before commit time");
		tx.commit();
		session.close();
		
		System.out.println("***************save done****************");
		return postId;
		
	}
	
	public static void removeFirstComment(long postId) {

		SessionFactory sf = HibernateUtil.getSessionFactory();
		Session session = sf.openSession();
		session.setHibernateFlushMode(FlushMode.COMMIT);
		Transaction tx = session.beginTransaction();
		
		Post post = session.get(Post.class, postId);
		
		post.getComments().remove(0);
		
		System.out.println("Flush is triggered before commit time");
		tx.commit();
		session.close();
		System.out.println("***************remove done****************");

	}
	

}
