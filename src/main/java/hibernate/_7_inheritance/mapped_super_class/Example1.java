package hibernate._7_inheritance.mapped_super_class;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.MapsId;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

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
		configuration.addAnnotatedClass(Board.class);
		configuration.addAnnotatedClass(Post.class);
		configuration.addAnnotatedClass(Announcement.class);
		configuration.addAnnotatedClass(PostStatistics.class);
		configuration.addAnnotatedClass(AnnouncementStatistics.class);

		ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
				.applySettings(configuration.getProperties()).build();

		sessionFactory = configuration.buildSessionFactory(serviceRegistry);
		return sessionFactory;

	}
}

@Entity(name = "Board")
@Table(name = "board")
class Board {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	private String name;
	
	@OneToMany(mappedBy = "board")
	private List<Post> posts = new ArrayList<>();
	
	@OneToMany(mappedBy = "board")
	private List<Announcement> announcements = new ArrayList<>();

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
		
}

@MappedSuperclass
abstract class Topic {

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE)
	private Long id;

	private String title;

	private String owner;

	@Temporal(TemporalType.TIMESTAMP)
	private Date createdOn = new Date();
	
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

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public Date getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(Date createdOn) {
		this.createdOn = createdOn;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Topic [id=");
		builder.append(id);
		builder.append(", title=");
		builder.append(title);
		builder.append(", owner=");
		builder.append(owner);
		builder.append(", createdOn=");
		builder.append(createdOn);
		builder.append("]");
		return builder.toString();
	}

}

@Entity
@Table(name = "post")
class Post extends Topic {

	private String content;
	
	@ManyToOne(fetch = FetchType.LAZY)
	private Board board;

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
	
	public Board getBoard() {
		return board;
	}

	public void setBoard(Board board) {
		this.board = board;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Post [content=");
		builder.append(content);
		builder.append("]");
		return builder.toString();
	}
	
}

@Entity
@Table(name = "announcement")
class Announcement extends Topic {

	@Temporal(TemporalType.TIMESTAMP)
	private Date validUntil;
	
	@ManyToOne(fetch = FetchType.LAZY)
	private Board board;

	public Date getValidUntil() {
		return validUntil;
	}

	public void setValidUntil(Date validUntil) {
		this.validUntil = validUntil;
	}
	
	public Board getBoard() {
		return board;
	}

	public void setBoard(Board board) {
		this.board = board;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Announcement [validUntil=");
		builder.append(validUntil);
		builder.append("]");
		return builder.toString();
	}

}

@MappedSuperclass
abstract class TopicStatistics {

	@Id
	private Long id;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public abstract void incrementViews();

}

@Entity
@Table(name = "post_statistics")
class PostStatistics extends TopicStatistics {
		
	@OneToOne
	@JoinColumn(name = "id")
	@MapsId
	private Post topic;
	
	private long views;	
	
	public PostStatistics() {
		
	}

	public PostStatistics(Post topic) {
		this.topic = topic;
	}

	public Topic getTopic() {
		return topic;
	}

	public void setTopic(Post topic) {
		this.topic = topic;
	}

	public long getViews() {
		return views;
	}

	public void setViews(long views) {
		this.views = views;
	}	
	
	public void incrementViews() {
		this.views++;
	}
	
}

@Entity
@Table(name = "announcement_statistics")
class AnnouncementStatistics extends TopicStatistics {
		
	@OneToOne
	@JoinColumn(name = "id")
	@MapsId
	private Announcement topic;
	
	private long views;	
	
	public AnnouncementStatistics() {
		
	}

	public AnnouncementStatistics(Announcement topic) {
		this.topic = topic;
	}

	public Topic getTopic() {
		return topic;
	}

	public void setTopic(Announcement topic) {
		this.topic = topic;
	}

	public long getViews() {
		return views;
	}

	public void setViews(long views) {
		this.views = views;
	}	
	
	public void incrementViews() {
		this.views++;
	}
	
}

public class Example1 {

	public static void main(String[] args) {
		
		SessionFactory sf = HibernateUtil.getSessionFactory();
		Session session = sf.openSession();
		session.setHibernateFlushMode(FlushMode.COMMIT);
		Transaction tx = session.beginTransaction();
		
		Board board = new Board();
		board.setName("My Board");
		session.persist(board);
		
		Post post = new Post();
		post.setOwner("John Doe");
		post.setTitle("Inheritance");
		post.setContent("Best Practices");    //post specific property
		post.setBoard(board);
		session.persist(post);

		Announcement announcement = new Announcement();
		announcement.setOwner("John Doe");
		announcement.setTitle("Release x.y.z.final");
		announcement.setValidUntil(Timestamp.valueOf(LocalDateTime.now().plusMonths(1)));   //announcement specific property
		announcement.setBoard(board);
		session.persist(announcement);
		
		TopicStatistics postStatistics = new PostStatistics(post);
		postStatistics.incrementViews();
		session.persist(postStatistics);
		
		
		TopicStatistics announcementStatistics = new AnnouncementStatistics(announcement);
		announcementStatistics.incrementViews();
		session.persist(announcementStatistics);
		
		
		System.out.println("Flush is triggered before commit time");
		tx.commit();
		session.close();
	}

}
