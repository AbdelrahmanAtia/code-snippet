package hibernate._10_connection_pooling.Hikari_connection_provider;

import java.util.Properties;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
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
		
		//properties.put(Environment.DRIVER, Constant.DB_DRIVER); //no need for setting this property
		
		properties.put(Environment.URL, Constant.DB_URL);
		properties.put(Environment.USER, Constant.DB_USERNAME);
		properties.put(Environment.PASS, Constant.DB_PASSWORD);
		
		properties.put(Environment.DIALECT, "org.hibernate.dialect.MySQL8Dialect");
		
		properties.put(Environment.SHOW_SQL, "true");
		properties.put(Environment.HBM2DDL_AUTO, "create-drop");
		
		/**
		 * >> make sure that u have the two dependencies of Hikari in pom.xml file
		 * >> setting this property will make hibernate pick the hikari impl
		 * since i have multiple connection pooling implementation dependencies
		 * such as C3P0 and Hikari..without having this, hibernate will not choose any
		 * implementation and will fall back to it's trivial implementation which is 
		 * the DriverManagerConnectionProvider
		 */
		properties.put("hibernate.hikari.maximumPoolSize", "20");


		Configuration configuration = new Configuration();
		configuration.setProperties(properties);
		configuration.addAnnotatedClass(Post.class);

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

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Post [id=");
		builder.append(id);
		builder.append(", title=");
		builder.append(title);
		builder.append("]");
		return builder.toString();
	}

}


public class Example1 {

	public static void main(String [] args) {
		
		Post p = new Post("My First Post");
		savePost(p);
		
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
		
}