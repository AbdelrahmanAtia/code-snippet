package collections.singletone_methods;

//import static java.util.Collections.singletonList;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Example_1 {

	public static void main(String [] args) {
		
		//in mathematics: singleton term is used for a set that contains exactly one element
		//Returns an immutable list containing only one element. 
		List<Employee> employeesList = Collections.singletonList(new Employee(30, "IT")); 
		
		//that singleton list is immutable, u can't add or remove elements from it
		//employeesList.add(new Employee()); //throws UnsupportedOperationException
		
		
		//returns an immutable set containing only one element
		Set<Employee> employeesSet = Collections.singleton(new Employee(22, "Marketing")); 
		
		//returns an immutable map having only one key
		Map<Integer, Employee> employeesMap = Collections.singletonMap(1, new Employee(25, "Sales"));
		
		//employeesMap.put(1, null); //throws UnsupportedOperationException
		
	}

}

class Employee {

	private int age;
	private String department;
	
	public Employee() {
		
	}

	public Employee(int age, String department) {
		this.age = age;
		this.department = department;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public String getDepartment() {
		return department;
	}

	public void setDepartment(String department) {
		this.department = department;
	}

}
