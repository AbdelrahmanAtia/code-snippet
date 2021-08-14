package hibernate._6_many_to_many.alternative_one_to_many;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

import javax.persistence.CascadeType;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
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
		properties.put(Environment.HBM2DDL_AUTO, "create");

		Configuration configuration = new Configuration();
		configuration.setProperties(properties);
		configuration.addAnnotatedClass(Post.class);
		configuration.addAnnotatedClass(Tag.class);
		configuration.addAnnotatedClass(PostTag.class);


		ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
				.applySettings(configuration.getProperties()).build();

		sessionFactory = configuration.buildSessionFactory(serviceRegistry);
		return sessionFactory;

	}
}

@Embeddable
class PostTagId implements Serializable {
	

	private static final long serialVersionUID = 1L;

	
	private Long postId;
	private Long tagId;

	public PostTagId() {

	}
	
	public PostTagId(Long postId, Long tagId) {
		this.postId = postId;
		this.tagId = tagId;
	}

	public Long getPostId() {
		return postId;
	}

	public void setPostId(Long postId) {
		this.postId = postId;
	}

	public Long getTagId() {
		return tagId;
	}

	public void setTagId(Long tagId) {
		this.tagId = tagId;
	}

	@Override
	public String toString() {
		return "PostTagId [postId=" + postId + ", tagId=" + tagId + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(postId, tagId);
	}

	@Override
	public boolean equals(Object o) {

		if (this == o)
			return true;

		if (o == null || getClass() != o.getClass())
			return false;

		PostTagId other = (PostTagId) o;
		return Objects.equals(postId, other.postId) && Objects.equals(tagId, other.tagId);
	}
	
}

@Entity(name="PostTag") 
@Table(name="post_tag")
class PostTag {

	@EmbeddedId
	private PostTagId postTagId;

	@ManyToOne
	@MapsId("postId")
	private Post post;
	
	@ManyToOne
	@MapsId("tagId")
	private Tag tag;	
	
	public PostTag() {
		
	}
	
	public PostTag(Post post, Tag tag) {
		this.post = post;
		this.tag = tag;
		this.postTagId = new PostTagId(post.getId(), tag.getId());
	}

	public PostTagId getPostTagId() {
		return postTagId;
	}

	public void setPostTagId(PostTagId postTagId) {
		this.postTagId = postTagId;
	}

	public Post getPost() {
		return post;
	}

	public void setPost(Post post) {
		this.post = post;
	}

	public Tag getTag() {
		return tag;
	}

	public void setTag(Tag tag) {
		this.tag = tag;
	}
	
}

@Entity
@Table(name = "post")
class Post {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String title;

	@OneToMany(mappedBy = "post" ,cascade = CascadeType.ALL, orphanRemoval = true)
	private List<PostTag> tags = new ArrayList<>();

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

	public List<PostTag> getTags() {
		return tags;
	}

	public void setTags(List<PostTag> tags) {
		this.tags = tags;
	}
	
	public void removeTag(Tag tag) {
		for (Iterator<PostTag> iterator = tags.iterator(); iterator.hasNext();) {
			PostTag postTag = iterator.next();
			if (postTag.getPost().equals(this) && postTag.getTag().equals(tag)) {
				iterator.remove();
				postTag.getTag().getPosts().remove(postTag);
				postTag.setPost(null);
				postTag.setTag(null);
				break;
			}
		}
	}
	
}

@Entity
@Table(name = "tag")
class Tag {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String name;

	@OneToMany(mappedBy = "tag", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<PostTag> posts = new ArrayList<>();

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

	public List<PostTag> getPosts() {
		return posts;
	}

	public void setPosts(List<PostTag> posts) {
		this.posts = posts;
	}	

}

public class Example1 {

	public static void main(String[] args) {

		createAndSavePosts(); 

		long postId = 1L;
		long tagId = 1L;
		
		removeTag(postId, tagId);
		
		
		
		
	}

	private static void createAndSavePosts() {
		System.out.println("********************* starting createAndSavePosts() *********************");
		Post post1 = new Post("JPA with hibernate");
		Post post2 = new Post("Native hibernate");

		Tag tag1 = new Tag("JAVA");
		Tag tag2 = new Tag("Hibernate");
		
		PostTag postTag1 = new PostTag(post1, tag1);
		PostTag postTag2 = new PostTag(post1, tag2);
		PostTag postTag3 = new PostTag(post2, tag1);
		
		post1.setTags(Arrays.asList(postTag1, postTag2));
		post2.setTags(Arrays.asList(postTag3));


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
	
	
	private static void removeTag(long postId, long tagId) {
		System.out.println("******************* starting removeTag() *****************");
		
		SessionFactory sf = HibernateUtil.getSessionFactory();
		Session session = sf.openSession();
		session.setHibernateFlushMode(FlushMode.COMMIT);
		Transaction tx = session.beginTransaction();

		Post post = session.find(Post.class, postId);
		Tag tag = session.find(Tag.class, tagId);

		post.removeTag(tag);

		System.out.println("Flush is triggered before commit time");
		tx.commit();
		session.close();

		System.out.println("*************** removal done ****************");

	}

}
