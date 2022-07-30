package spring_data.jpa.remove_all_child_entities;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@SpringBootApplication
public class UsingArrayClearFunction {

	public static void main(String[] args) {

		Properties properties = new Properties();
		properties.put("spring.jpa.show-sql", true);

		properties.put("spring.datasource.url", "jdbc:mysql://localhost:3306/code_snippet");
		properties.put("spring.datasource.username", "root");
		properties.put("spring.datasource.password", "System");

		properties.put("spring.jpa.hibernate.ddl-auto", "create-drop");

		// properties.put("logging.level.org.hibernate.SQL", "DEBUG");

		// SpringApplication.run(Example1.class, args);
		SpringApplication app = new SpringApplication(UsingArrayClearFunction.class);
		app.setDefaultProperties(properties);
		app.run(args);
	}

	@Bean
	public CommandLineRunner data(PostService postService) {
		return (args) -> {

			postService.setUpDB();
			
			postService.deleteAllPostCommentsAndAddNewOnes(1L);
			
			postService.deleteAllPostCommentsUsingMoreEfficientWay(1L);

		};
	}

}

//=================================== Entities ========================================//
@Entity
@Table(name = "post")
class Post {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String title;

	@OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
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

	public void addComment(PostComment comment) {
		comments.add(comment);
		comment.setPost(this);
	}

	public void removeComment(PostComment comment) {
		comments.remove(comment);
		comment.setPost(null);
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

}

//=================================== Repositories ========================================//

@Repository
interface PostRepository extends JpaRepository<Post, Long> {
	
}

@Repository
interface PostCommentRepository extends JpaRepository<PostComment, Long> {
	
	void deleteByPostId(Long postId); //this is not efficient also, as it 
	                                  //loads all child comments then deletes them
	
	// void removeByPostId(Long postId); //TODO: what is the difference between 
	                                     // delete & remove
	
	@Modifying
	@Query("delete from PostComment where post.id = :postId")
	void deleteByPostIdV2(Long postId);  //this is the most efficient delete 
	                                     // solution as it will cost you only one query

}

//=================================== Service ========================================//

@Service
class PostService {

	@Autowired
	private PostRepository postRepository;
	
	@Autowired
	private PostCommentRepository postCommentRepository;

	@Transactional
	public void setUpDB() {
		
		System.out.println();
		System.out.println("######################## setUpDB:-");
		Post post = new Post("My Post");
		post.addComment(new PostComment("my first review"));
		post.addComment(new PostComment("my second review"));
		postRepository.save(post);
	}

	@Transactional
	public void deleteAllPostCommentsAndAddNewOnes(Long postId) {
		
		System.out.println();
		System.out.println("######################## deleteAllPostCommentsAndAddNewOnes:-");
		Post savedPost = postRepository.findById(1L).get();
		savedPost.getComments().clear();
		
		/*
		   >> this flush will force trigger delete queries.
		   without repository flushing, the delete queries will be delayed till the
		   end of this function. 
		   
		   >> deleting sub-entities using array clear function is not efficient
		   since it issues a select statement to load the post comments, then issues 
		   multiple delete statements to delete each comment. so it will cost u 
		   (n + 1) statements where:-
		           n >> number of comments       [will cause n delete queries]
		           1 >> for the select statement [reloading of comments]
		           
		   also, it consumes more memory since u will have to load all the comments 
		   in the memory to be able to delete them..so if u have 100K comments, then
		   u have to load all of them in the memory to be able to delete them 
		           
		   >> it is better to use one single HQL query to delete the 
		   child entities instead of using the array clear function.
		 */
		postRepository.flush(); 
		
		savedPost.addComment(new PostComment("my third review"));
		savedPost.addComment(new PostComment("my fourth review"));
	}
	
	@Transactional
	public void deleteAllPostCommentsUsingMoreEfficientWay(Long postId) {
		
		System.out.println();
		System.out.println("######################## deleteAllPostCommentsUsingMoreEfficientWay:-");

		postCommentRepository.deleteByPostIdV2(postId);
	}


}
