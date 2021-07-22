package hibernate._10_many_to_many.bidirectional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
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
		configuration.addAnnotatedClass(Tag.class);

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

	@ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE })

	@JoinTable(name = "post_tag", 
			   joinColumns = @JoinColumn(name = "post_id"), 
			   inverseJoinColumns = @JoinColumn(name = "tag_id"))
	private List<Tag> tags = new ArrayList<>();

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

	public List<Tag> getTags() {
		return tags;
	}

	public void setTags(List<Tag> tags) {
		this.tags = tags;
	}

	// in bidirectional relation, both sides must be in sync thats why 
	// we need to have these methods..we can add them to either Post
	// or Tag entity, u must choose the entity that u are most likely 
	// will interact with it
	public void addTag(Tag tag) {
		tags.add(tag);
		tag.getPosts().add(this);
	}

	public void removeTag(Tag tag) {
		tags.remove(tag);
		tag.getPosts().remove(this);
	}

	@Override
	public String toString() {
		return "Post [id=" + id + ", title=" + title + ", tags=" + tags + "]";
	}

}

@Entity
@Table(name = "tag")
class Tag {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String name;

	@ManyToMany(mappedBy = "tags")
	private List<Post> posts = new ArrayList<>();

	public Tag() {

	}

	public Tag(String name) {
		this.name = name;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Post> getPosts() {
		return posts;
	}

	public void setPosts(List<Post> posts) {
		this.posts = posts;
	}

	@Override
	public String toString() {
		return "Tag [id=" + id + ", name=" + name + "]";
	}

}

public class Example1 {

	public static void main(String[] args) {

		createAndSavePosts();

		long postId = 1L;
		List<Tag> orderedTags = getOrderedTags(postId);
		
		System.out.println(">> orderedTags: " + orderedTags);

	}

	private static List<Tag> getOrderedTags(long postId) {

		SessionFactory sf = HibernateUtil.getSessionFactory();
		Session session = sf.openSession();
		session.setHibernateFlushMode(FlushMode.COMMIT);
		Transaction tx = session.beginTransaction();

		Post post = session.find(Post.class, postId);
		
		
		//sorting will remove all the corresponding records in the 
		//junction table then reinserts them to make the in memory 
		//records and the data base records in the same order..
		//this behavior is a bit similar to the remove.. 
		post.getTags().sort(Collections.reverseOrder(Comparator.comparing(Tag::getId)));

		System.out.println("Flush is triggered before commit time");
		tx.commit();
		session.close();

		System.out.println("*************** removal done ****************");

		return post.getTags();
	}

	private static void createAndSavePosts() {
		Post post1 = new Post("JPA with hibernate");
		Post post2 = new Post("Native hibernate");

		Tag tag1 = new Tag("JAVA");
		Tag tag2 = new Tag("Hibernate");

		post1.getTags().add(tag1);
		post1.getTags().add(tag2);

		post2.getTags().add(tag1);

		SessionFactory sf = HibernateUtil.getSessionFactory();
		Session session = sf.openSession();
		session.setHibernateFlushMode(FlushMode.COMMIT);
		Transaction tx = session.beginTransaction();

		session.persist(post1);
		session.persist(post2);

		System.out.println("Flush is triggered before commit time");
		tx.commit();
		session.close();

		System.out.println("*************** save done ****************");

	}

}
