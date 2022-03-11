package interview_questions.constructor;

/**
Q1) what is a constructor ?
>> it is like a method that is used to initialize the state 
of the object.
>> it is invoked at the time of the object creation.


Q2) what is the purpose of default constructor ?
>> it provides default values to the objects.
>> the java compiler creates a default constructor if there is 
no constructor in the class.


Q3) Does constructor return any value ?
>> There are no “return value” statements in the constructor, 
but the constructor returns the current class instance.

Q4) is constructor Inherited ?
No, constructor is not inherited.

*/
public class Example_1 {

	public static void main(String[] args) {
		ex1();
		
		ex2();

	}
	
	public static void ex1() {
		
		//compile time error
		//the constructor should match the defined 
		//constructor in the class. Or u can add another
		//default constructor to your employee object.
		//Employee e = new Employee();		
		
	}

	public static void ex2() {
		//check Employee & Manager classes they 
		//demonstrates that there is no constructor inheritance
	}
	

}

class Employee {
	
	private int id;
	private String name;
	
	public Employee(int id, String name) {
		this.id = id;
		this.name = name;
	}
	
}

//compile time error
//u should say how to initialize the parent 
//class since u can't inherit it.
/*
class Manager extends Employee {
	
}

//works fine
//invokes the parent constructor
class Manager extends Employee {
	private String role;

	public Manager(String role) {
		super(0, ""); 
		this.role = role;
	}
}

*/

