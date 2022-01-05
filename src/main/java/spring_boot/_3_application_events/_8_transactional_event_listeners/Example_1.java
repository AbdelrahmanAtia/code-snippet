package spring_boot._3_application_events._8_transactional_event_listeners;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.annotation.Resource;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jms.activemq.ActiveMQAutoConfiguration;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// the example is from the following link:-
// javacodegeeks.com/2017/10/asynchrouns-transactional-event-listeners-spring.html

@EnableAsync
@SpringBootApplication(exclude = ActiveMQAutoConfiguration.class)
public class Example_1 {

	public static void main(String[] args) {

		Properties properties = new Properties();
		properties.put("spring.jpa.show-sql", true);
		properties.put("management.endpoints.web.exposure.include", "*");

		// properties.put("logging.level.org.hibernate.SQL", "DEBUG");

		// SpringApplication.run(Example1.class, args);
		SpringApplication app = new SpringApplication(Example_1.class);
		app.setDefaultProperties(properties);
		app.run(args);
	}

}

//======================================  API ==================================//

@RestController
@RequestMapping("/tasks")
class TaskApi {

	@Autowired
	private TaskService taskService;

	@GetMapping
	public List<Task> getAllTasks() {
		return taskService.getAllTasks();
	}

	@PostMapping
	public String createTask(@RequestBody Map<String, String> reqBody) {
		taskService.createTask(reqBody.get("name"));
		return "task created successfully";
	}

}

//======================================  Service ==================================//

@Service
class TaskService {

	private static final Logger LOG = LoggerFactory.getLogger(TaskService.class);

	@Autowired
	private TaskRepository taskRepository;

	@Autowired
	private ApplicationEventPublisher eventPublisher;

	public List<Task> getAllTasks() {
		return taskRepository.findAll();
	}

	@Transactional
	public Task createTask(String name) {

		Task task = new Task();
		task.setName(name);
		task.setCreated(LocalDateTime.now());

		LOG.info("Publishing task created event: {}", task);

		eventPublisher.publishEvent(new TaskCreatedEvent(this.getClass(), task));

		try {
			return taskRepository.save(task);
		} finally {
			LOG.info("Event published. Saving task: {}", task);
		}
		
	}

}

//======================================  Repository ==================================//

@Repository
interface TaskRepository extends JpaRepository<Task, Integer> {

}

//======================================  Domain ==================================//

@Entity
class Task {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	private String name;
	private LocalDateTime created;

	public Task() {

	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public LocalDateTime getCreated() {
		return created;
	}

	public void setCreated(LocalDateTime created) {
		this.created = created;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Task [id=");
		builder.append(id);
		builder.append(", name=");
		builder.append(name);
		builder.append(", created=");
		builder.append(created);
		builder.append("]");
		return builder.toString();
	}

}

//======================================  Event ================================================================//

class TaskCreatedEvent extends ApplicationEvent {

	private static final long serialVersionUID = 1L;

	private Task task;

	public TaskCreatedEvent(Object source, Task task) {
		super(source);
		this.task = task;
	}

	public Task getTask() {
		return task;
	}

}

//======================================  Listener ================================================================//

@Service
class TaskCreatedEventListener {

	private static final Logger LOG = LoggerFactory.getLogger(TaskCreatedEventListener.class);

	@Resource
	EntityManager entityManager;

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handleEvent(TaskCreatedEvent taskCreatedEvent) throws InterruptedException {
		Task task = taskCreatedEvent.getTask();

		LOG.info("Is task transient? {}", isTransient(task)); // not saved in DB yet
		LOG.info("Is task managed? {}", isManaged(task)); // saved and the session is still open
		LOG.info("Is task detached? {}", isDetached(task)); // removed from the session or the session is closed
	}

	private boolean isTransient(Task task) {
		return task.getId() == null;
	}

	private boolean isManaged(Task task) {
		return entityManager.contains(task);
	}

	private boolean isDetached(Task task) {
		return !isTransient(task) && !isManaged(task) && exists(task);
	}

	private boolean exists(Task task) {
		return entityManager.find(Task.class, task.getId()) != null;
	}

}
