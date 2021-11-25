package spring_boot._2_simple_app;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.stream.Stream;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jms.activemq.ActiveMQAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonIgnore;

@SpringBootApplication(exclude = ActiveMQAutoConfiguration.class)
public class Example1 {

	public static void main(String[] args) {
		
		Properties properties = new Properties();
		//properties.put("spring.jpa.show-sql", true);
		//properties.put("logging.level.org.hibernate.SQL", "DEBUG");

		//SpringApplication.run(Example1.class, args);
		SpringApplication app = new SpringApplication(Example1.class);
		app.setDefaultProperties(properties);
		app.run(args);
	}

	@Bean
	public CommandLineRunner data(RateRepository repository) {
		return (args) -> {
			repository.save(new Rate("EUR", 0.88857F, new Date()));
			repository.save(new Rate("JPY", 102.17F, new Date()));
			repository.save(new Rate("MXN", 19.232F, new Date()));
			repository.save(new Rate("GBP", 0.75705F, new Date()));
		};
	}
}


@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
@interface ToUpper {
	
}

@Entity
class Rate {

	@Id
	private String code;
	private Float rate;

	@JsonIgnore
	@Temporal(TemporalType.DATE)
	private Date date;

	public Rate() {
	}

	public Rate(String base, Float rate, Date date) {
		super();
		this.code = base;
		this.rate = rate;
		this.date = date;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String base) {
		this.code = base;
	}

	public Float getRate() {
		return rate;
	}

	public void setRate(Float rate) {
		this.rate = rate;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	@Override
	public String toString() {
		String format = new SimpleDateFormat("yyyy-MM-dd").format(date);
		return "Rate [code=" + code + ", rate=" + rate + ", date=" + format + "]";
	}

}

class CurrencyConversion {

	private String base;
	private String code;
	private float amount;
	private float total;
	
	public CurrencyConversion(){}
	
	public CurrencyConversion(String base, String code, float amount, float total) {
		super();
		this.base = base;
		this.code = code;
		this.amount = amount;
		this.total = total;
	}
	
	public String getBase() {
		return base;
	}
	public void setBase(String base) {
		this.base = base;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public float getAmount() {
		return amount;
	}
	public void setAmount(float amount) {
		this.amount = amount;
	}
	public float getTotal() {
		return total;
	}
	public void setTotal(float total) {
		this.total = total;
	}	
}

class CurrencyExchange {

	public static final String BASE_CODE = "USD";
	private String base;
	private String date;
	private Rate[] rates;
	
	public CurrencyExchange(String base, String date, Rate[] rates) {
		super();
		this.base = base;
		this.date = date;
		this.rates = rates;
	}
	
	public String getBase() {
		return base;
	}
	public void setBase(String base) {
		this.base = base;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public Rate[] getRates() {
		return rates;
	}
	public void setRates(Rate[] rates) {
		this.rates = rates;
	}
	
	
	
}

interface RateRepository extends JpaRepository<Rate, String> {
	List<Rate> findByDate(Date date);

	Rate findByDateAndCode(Date date, String code);
}


@Service
class CurrencyConversionService {
	
	@Autowired
	RateRepository repository;
	
	public CurrencyConversion convertFromTo(@ToUpper String base, @ToUpper String code,Float amount) throws Exception{
		Rate baseRate = new Rate(CurrencyExchange.BASE_CODE,1.0F,new Date());
		Rate codeRate = new Rate(CurrencyExchange.BASE_CODE,1.0F,new Date());
		
		if(!CurrencyExchange.BASE_CODE.equals(base))
			baseRate = repository.findByDateAndCode(new Date(), base);
		
		if(!CurrencyExchange.BASE_CODE.equals(code))
			codeRate = repository.findByDateAndCode(new Date(), code);
		
		if(null == codeRate || null == baseRate)
			throw new Exception("Bad Code Base.");
		
		return new CurrencyConversion(base,code,amount,(codeRate.getRate()/baseRate.getRate()) * amount);
	}
	
	public Rate[] calculateByCode(@ToUpper String code, Date date) throws Exception{
		List<Rate> rates = repository.findByDate(date);
		if(code.equals(CurrencyExchange.BASE_CODE))
			return rates.toArray(new Rate[0]);
		
		Rate baseRate = rates.stream()
			                 .filter(rate -> rate.getCode().equals(code))
			                 .findFirst()
			                 .orElse(null);
		
		if(null == baseRate)
			throw new Exception("Bad Base Code");
		
		return Stream.concat(rates.stream()
			 .filter(n -> !n.getCode().equals(code))
			 .map(n -> new Rate(n.getCode(),n.getRate()/baseRate.getRate(),date)),Stream.of(new Rate(CurrencyExchange.BASE_CODE,1/baseRate.getRate(),date)))
			 .toArray(size -> new Rate[size]);
	}
	
	public void saveRates(Rate[] rates, Date date){
		Arrays.stream(rates).forEach(rate -> repository.save(new Rate(rate.getCode(),rate.getRate(),date)));
	}
	
}


@RestController
@RequestMapping("/currency")
class CurrencyController {
	

	private static final Logger log = LoggerFactory.getLogger(CurrencyController.class);
	
	@Autowired 
	CurrencyConversionService service;
	
	@RequestMapping("/latest")
	public ResponseEntity<CurrencyExchange> getLatest(
			@RequestParam(name = "base", defaultValue = CurrencyExchange.BASE_CODE) String base) throws Exception {
		return new ResponseEntity<CurrencyExchange>(new CurrencyExchange(base,
				new SimpleDateFormat("yyyy-MM-dd").format(new Date()), service.calculateByCode(base, new Date())),
				HttpStatus.OK);
	}
	
	@RequestMapping("/{date}")
	public ResponseEntity<CurrencyExchange> getByDate(@PathVariable("date") @DateTimeFormat(pattern="yyyy-MM-dd") Date date,@RequestParam(name="base",defaultValue=CurrencyExchange.BASE_CODE)String base) throws Exception{
		return new ResponseEntity<CurrencyExchange>(new CurrencyExchange(base,new SimpleDateFormat("yyyy-MM-dd").format(date),service.calculateByCode(base,date)),HttpStatus.OK);
	}
	
	@RequestMapping("/{amount}/{base}/to/{code}")
	public ResponseEntity<CurrencyConversion> conversion(@PathVariable("amount")Float amount,@PathVariable("base")String base,@PathVariable("code")String code) throws Exception{
		CurrencyConversion conversionResult = service.convertFromTo(base, code, amount);
		return new ResponseEntity<CurrencyConversion>(conversionResult,HttpStatus.OK);
	}
	
	@RequestMapping(path="/new",method = {RequestMethod.POST})
	public ResponseEntity<CurrencyExchange> addNewRates(@RequestBody CurrencyExchange currencyExchange) throws Exception{
		try{
			final Date date = new SimpleDateFormat("yyyy-MM-dd").parse(currencyExchange.getDate());
			final Rate[] rates = currencyExchange.getRates();
			service.saveRates(rates,date);
		}catch(Exception ex){
			log.error(ex.getMessage());
			throw ex;
		}
		return new ResponseEntity<CurrencyExchange>(HttpStatus.CREATED);
	}
	
}
