package interview_questions.local_and_instance_variables;

/*
Q1) what is the default values of the local variables ?
There is no default values for the local variables (neither primitive
nor objects). to use the local variables, u have to initialize them.

Q2) what is the default value of instance variables ?
>> instance variables of type Object is initialized to null.
>> instance variables of type primitive is initialized to their default
   values. for example 
   int is initialized to 0
   float is initialized to 0.0
   

*/

public class Main {

	public static void main(String[] args) {

		ex1();
		
		ex2(); //print initial values of instance variables
	}

	private static void ex1() {
		int x;  //primitive local variable
		Rate rate;  //object local variable
		// System.out.println(x); // compile time error
		// System.out.println(rate); // compile time error
	}
	
	public static void ex2() {
		Rate rate = new Rate();
		rate.printInitialValues();
	}

	

}
class Rate {
	private int id;  //primitive instance variable
	private String base; //Object instance variable
	private float rate;
	private String code;

	public Rate() {

	}

	public String getBase() {
		return base;
	}

	public void setBase(String base) {
		this.base = base;
	}

	public float getRate() {
		return rate;
	}

	public void setRate(float rate) {
		this.rate = rate;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}
	
	public void printInitialValues() {
		System.out.println("primitive instance var - initial value of id: " + id);
		System.out.println("Object instance var - initial value of base: " + base);
		System.out.println("primitive instance var - initial value of rate: " + rate);

	}

}