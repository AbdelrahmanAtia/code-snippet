/*
package spring_data.jpa.reactive_repository.h2;

 * package spring_data.jpa.reactive_repository;
 * 
 * import java.util.Properties;
 * 
 * import javax.persistence.Entity; import javax.persistence.Id; import
 * javax.persistence.Table; import javax.persistence.Version;
 * 
 * import org.springframework.boot.CommandLineRunner; import
 * org.springframework.boot.SpringApplication; import
 * org.springframework.boot.autoconfigure.SpringBootApplication; import
 * org.springframework.boot.r2dbc.ConnectionFactoryBuilder; import
 * org.springframework.context.annotation.Bean; import
 * org.springframework.context.annotation.Configuration; import
 * org.springframework.core.io.ClassPathResource; import
 * org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration; import
 * org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
 * import org.springframework.data.repository.reactive.ReactiveCrudRepository;
 * import org.springframework.r2dbc.connection.init.CompositeDatabasePopulator;
 * import
 * org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer;
 * import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator;
 * 
 * import io.r2dbc.h2.H2ConnectionConfiguration; import
 * io.r2dbc.h2.H2ConnectionFactory; import io.r2dbc.spi.ConnectionFactory;
 * import io.r2dbc.spi.ConnectionFactoryOptions; import
 * io.r2dbc.spi.ConnectionFactoryOptions.Builder; import
 * reactor.core.publisher.Flux;
 * 
 * @SpringBootApplication public class Example1 {
 * 
 * public static void main(String[] args) {
 * 
 * Properties properties = new Properties();
 * 
 * // with this property you can access the h2 database web console with the //
 * following URL:- // http://localhost:8080/h2-console
 * properties.put("spring.h2.console.enabled", true);
 * 
 * // with this property u can override the default URL "h2-console"
 * properties.put("spring.h2.console.path", "/my-h2-console");
 * 
 * SpringApplication app = new SpringApplication(Example1.class);
 * app.setDefaultProperties(properties); app.run(args);
 * 
 * }
 * 
 * @Bean public CommandLineRunner data(RecommendationRepository repository) {
 * return (args) -> {
 * 
 * // set up repository.save(new RecommendationEntity("1", 1, 1,
 * "first author name", 4, "first recomendation content"));
 * System.out.println("saved first entity............");
 * 
 * repository .save(new RecommendationEntity("2", 2, 2, "second author name", 5,
 * "second recomendation content"));
 * System.out.println("saved second entity............");
 * 
 * Flux<RecommendationEntity> recommendation = repository.findByProductId(2);
 * recommendation.toStream().forEach(System.out::println);
 * 
 * }; }
 * 
 * @Bean public ConnectionFactory connectionFactory() {
 * 
 * //Builder options = ConnectionFactoryOptions.builder();
 * //options.option(io.r2dbc.spi.Option.valueOf(""), ""); ConnectionFactory
 * connectionFactory = ConnectionFactoryBuilder //.
 * withUrl("r2dbc:h2:file:C:\\Users\\ayoussef4\\OneDrive - DXC Production\\Desktop\\h2database;DB_CLOSE_DELAY=-1"
 * ) //.
 * withUrl("r2dbc:h2:file:C:\\Users\\ayoussef4\\OneDrive - DXC Production\\Desktop\\h2database.mv.db"
 * ) //.withUrl("r2dbc:h2:file://h2database.mv.db")
 * .withUrl("r2dbc:h2:file:///./data/h2db/testdb;DB_CLOSE_DELAY=-1")
 * .username("sa") .build();
 * 
 * return connectionFactory; }
 * 
 * //####################################################################### //
 * not working..fails with error:- Cannot determine database's type as //
 * ConnectionFactory is not options-capable
 * 
 * 
 * @Bean public H2ConnectionFactory connectionFactory() { return new
 * H2ConnectionFactory(H2ConnectionConfiguration.builder()
 * .url("file:C:\\Users\\ayoussef4\\OneDrive - DXC Production\\Desktop\\h2database;DB_CLOSE_DELAY=-1"
 * ) .username("sa") .build()); }
 * 
 * 
 * //####################################################################### //
 * not working..fails with error:- Cannot determine database's type as //
 * ConnectionFactory is not options-capable
 * 
 * @Bean public ConnectionFactory connectionFactory() { return
 * H2ConnectionFactory.inMemory(
 * "r2dbc:h2:mem:mydb;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false"); }
 * 
 * //#######################################################################
 * 
 * 
 * 
 * @Bean public ConnectionFactoryInitializer initializer(ConnectionFactory
 * connectionFactory) {
 * 
 * ConnectionFactoryInitializer initializer = new
 * ConnectionFactoryInitializer();
 * initializer.setConnectionFactory(connectionFactory);
 * 
 * CompositeDatabasePopulator populator = new CompositeDatabasePopulator();
 * populator.addPopulators(new ResourceDatabasePopulator(new
 * ClassPathResource("schema.sql"))); // populator.addPopulators(new
 * ResourceDatabasePopulator(new // ClassPathResource("data.sql")));
 * initializer.setDatabasePopulator(populator);
 * 
 * return initializer; }
 * 
 * @Bean public H2ConnectionFactory connectionFactory() { return new
 * H2ConnectionFactory( H2ConnectionConfiguration.builder() //.
 * url("r2dbc:h2:file:C:\\\\Users\\\\ayoussef4\\\\OneDrive - DXC Production\\\\Desktop\\\\h2databasetestdb;DB_CLOSE_DELAY=-1"
 * )
 * .url("file:C:\\Users\\ayoussef4\\OneDrive - DXC Production\\Desktop\\h2database"
 * ) .username("sa").build()); }
 * 
 * /*
 * 
 * @Bean public ConnectionFactory connectionFactory() {
 * System.out.println(">>>>>>>>>> Using H2 in mem R2DBC connection factory");
 * return H2ConnectionFactory.inMemory("testdb"); }
 * 
 * @Configuration
 * 
 * @EnableR2dbcRepositories class R2DBCConfiguration extends
 * AbstractR2dbcConfiguration {
 * 
 * @Bean public H2ConnectionFactory connectionFactory() {
 * 
 * return new H2ConnectionFactory( H2ConnectionConfiguration.builder()
 * //.url("mem:testdb;DB_CLOSE_DELAY=-1;")
 * .url("r2dbc:h2:file:/testdb;DB_CLOSE_DELAY=-1;") .username("sa") .build() );
 * 
 * } }
 * 
 * 
 * }
 * 
 * // ====================== Entities ===========================//
 * 
 * @Entity
 * 
 * @Table(name = "RECOMMENDATION_ENTITY") class RecommendationEntity {
 * 
 * @Id private String id;
 * 
 * @Version private int version;
 * 
 * private int productId; private int recommendationId; private String author;
 * private int rate; private String content;
 * 
 * public RecommendationEntity(String id, int productId, int recommendationId,
 * String author, int rate, String content) { super(); this.id = id;
 * this.productId = productId; this.recommendationId = recommendationId;
 * this.author = author; this.rate = rate; this.content = content; }
 * 
 * public String getId() { return id; }
 * 
 * public void setId(String id) { this.id = id; }
 * 
 * public int getVersion() { return version; }
 * 
 * public void setVersion(int version) { this.version = version; }
 * 
 * public int getProductId() { return productId; }
 * 
 * public void setProductId(int productId) { this.productId = productId; }
 * 
 * public int getRecommendationId() { return recommendationId; }
 * 
 * public void setRecommendationId(int recommendationId) { this.recommendationId
 * = recommendationId; }
 * 
 * public String getAuthor() { return author; }
 * 
 * public void setAuthor(String author) { this.author = author; }
 * 
 * public int getRate() { return rate; }
 * 
 * public void setRate(int rate) { this.rate = rate; }
 * 
 * public String getContent() { return content; }
 * 
 * public void setContent(String content) { this.content = content; }
 * 
 * @Override public String toString() { StringBuilder builder = new
 * StringBuilder(); builder.append("RecommendationEntity [id=");
 * builder.append(id); builder.append(", version="); builder.append(version);
 * builder.append(", productId="); builder.append(productId);
 * builder.append(", recommendationId="); builder.append(recommendationId);
 * builder.append(", author="); builder.append(author);
 * builder.append(", rate="); builder.append(rate);
 * builder.append(", content="); builder.append(content); builder.append("]");
 * return builder.toString(); }
 * 
 * }
 * 
 * // ====================== Repositories
 * =====================================//
 * 
 * interface RecommendationRepository extends
 * ReactiveCrudRepository<RecommendationEntity, String> {
 * Flux<RecommendationEntity> findByProductId(int productId); }
 */