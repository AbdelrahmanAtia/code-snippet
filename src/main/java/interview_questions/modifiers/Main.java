package interview_questions.modifiers;

/*
Q1) what are modifiers ?
	keywords that u add to the definitions to change their meanings.

Q2) what are types of modifiers in java ?
	two types:-
	access modifiers & non-access modifiers
	
Q3) what are access modifiers ?
		1- public >> visible to the world
		2- protected >> visible to the package and all subclasses.
		3- private  >> Visible to the class only 
		5- the default. No modifiers are needed. >> visible to the package
		
Q4) what are non-access modifier ?
	1- static
	2- abstract >> the class it self should be abstract to have 
	               an abstract function
	3- final
	
	
*/
public abstract class Main {

	public static void main(String[] args) {
				

		
	}

	//public & static can be in any order. see ex1()
	static public void ex2() {

	}

	// public access modifier
	public static void ex1() {

	}

	// protected access modifier
	protected static void ex3() {

	}

	//private access modifier
	private static void ex4() {

	}
	
	// default access modifier - no modifier
	void ex6() {

	}

	//abstract non-access modifier
	public abstract void ex5();
	

}
