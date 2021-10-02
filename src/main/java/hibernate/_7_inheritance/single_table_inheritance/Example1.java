package hibernate._7_inheritance.single_table_inheritance;

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
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
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
		configuration.addAnnotatedClass(Topic.class);
		configuration.addAnnotatedClass(Post.class);
		configuration.addAnnotatedClass(Announcement.class);
		configuration.addAnnotatedClass(TopicStatistics.class);


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
	private List<Topic> topics = new ArrayList<>();
	
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
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Board [id=");
		builder.append(id);
		builder.append(", name=");
		builder.append(name);
		builder.append(", topics=");
		builder.append(topics);
		builder.append("]");
		return builder.toString();
	}
		
}

@Entity(name = "Topic")
@Table(name = "topic")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
class Topic {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	private String title;
	
	private String owner;
	
	@Temporal(TemporalType.TIMESTAMP)
	private Date createdOn = new Date();
	
	@ManyToOne(fetch = FetchType.LAZY)
	private Board board;

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

	public Board getBoard() {
		return board;
	}

	public void setBoard(Board board) {
		this.board = board;
	}
		
}

@Entity
@Table(name = "post")
class Post  extends Topic {
	
	private String content;

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Post [getContent()=").append(getContent()).append(", getId()=").append(getId())
				.append(", getTitle()=").append(getTitle()).append(", getOwner()=").append(getOwner())
				.append(", getCreatedOn()=").append(getCreatedOn()).append("]");
		return builder.toString();
	}	
	
}

@Entity
@Table(name = "announcement")
class Announcement extends Topic {
	
	@Temporal(TemporalType.TIMESTAMP)
	private Date validUntil;

	public Date getValidUntil() {
		return validUntil;
	}

	public void setValidUntil(Date validUntil) {
		this.validUntil = validUntil;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Announcement [getValidUntil()=").append(getValidUntil()).append(", getId()=").append(getId())
				.append(", getTitle()=").append(getTitle()).append(", getOwner()=").append(getOwner())
				.append(", getCreatedOn()=").append(getCreatedOn()).append("]");
		return builder.toString();
	}	
	
}


@Entity
@Table(name = "topic_statistics")
class TopicStatistics {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY) //i don't think that this annotation is useful because of @MapsId
	private Long id;
	
	@OneToOne
	@JoinColumn(name = "id")
	@MapsId
	private Topic topic;
	
	private long views;	
	
	public TopicStatistics() {
		
	}

	public TopicStatistics(Topic topic) {
		this.topic = topic;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Topic getTopic() {
		return topic;
	}

	public void setTopic(Topic topic) {
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

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("TopicStatistics [id=");
		builder.append(id);
		builder.append(", topic=");
		builder.append(topic);
		builder.append(", views=");
		builder.append(views);
		builder.append("]");
		return builder.toString();
	}
	
}

public class Example1 {

	public static void main(String[] args) {
		
		createAndSave(); 
		
		List<Topic> topics = getAllTopics();
		System.out.println(topics);
		
		Long topicId = 1L;
		TopicStatistics statistics = getTopicStatistics(topicId);
		System.out.println(statistics);
		
		Long boardId = 1L;
		Board board = getBoard(boardId);
		System.out.println(board);

	}

	private static Board getBoard(Long boardId) {
		System.out.println("*************** starting getBoard() ****************");
	
		SessionFactory sf = HibernateUtil.getSessionFactory();
		Session session = sf.openSession();
		session.setHibernateFlushMode(FlushMode.COMMIT);
		Transaction tx = session.beginTransaction();
		
		Board board = session.createQuery(
				"select b from Board b join fetch b.topics where b.id = :id", Board.class)
				.setParameter("id", boardId)
				.getSingleResult();

		
		System.out.println("Flush is triggered before commit time");
		tx.commit();
		session.close();

		System.out.println("*************** board retrieval done ****************");

		return board;
	}

	private static TopicStatistics getTopicStatistics(Long topicId) {
		SessionFactory sf = HibernateUtil.getSessionFactory();
		Session session = sf.openSession();
		session.setHibernateFlushMode(FlushMode.COMMIT);
		Transaction tx = session.beginTransaction();
		
		TopicStatistics statistics = session.createQuery(
				"select s from TopicStatistics s join fetch s.topic t where t.id = :topicId", TopicStatistics.class)
		        .setParameter("topicId", topicId)   
				.getSingleResult();
		
		System.out.println("Flush is triggered before commit time");
		tx.commit();
		session.close();

		System.out.println("*************** retrieval done ****************");

		return statistics;
	}



	private static List<Topic> getAllTopics() {
		System.out.println("********************* starting getAllTopics() *********************");

		SessionFactory sf = HibernateUtil.getSessionFactory();
		Session session = sf.openSession();
		session.setHibernateFlushMode(FlushMode.COMMIT);
		Transaction tx = session.beginTransaction();
		
		List<Topic> topics = session.createQuery(
				"select t from Topic t where t.board.id = :boardId", Topic.class)
				.setParameter("boardId", 1L)
				.getResultList();
		
		System.out.println("Flush is triggered before commit time");
		tx.commit();
		session.close();

		System.out.println("*************** retrieval done ****************");
		
		return topics;
	}



	private static void createAndSave() {
		
		System.out.println("********************* starting createAndSave() *********************");

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
		
		
		TopicStatistics postStatistics = new TopicStatistics(post);
		postStatistics.incrementViews();
		session.persist(postStatistics);
		
		TopicStatistics announcementStatistics = new TopicStatistics(announcement);
		announcementStatistics.incrementViews();
		session.persist(announcementStatistics);
		

		System.out.println("Flush is triggered before commit time");
		tx.commit();
		session.close();

		System.out.println("*************** save done ****************");
		
	}

}
