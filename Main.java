package calculator;

import java.util.Scanner;
import java.text.DecimalFormat;

public class Main {
	
	public static void main(String args[]) {
		
		double Ans=0;
		int choice;
		Scanner input=new Scanner(System.in);
		System.out.println("Note: The angle for trigonometric functions will be in radian. Please Use proper brackets to ensure no syntax error occurs. Type all trigonometric functions in lower case.\n");
		
		while(true) {
			
			Calculator c=new Calculator();    // creating an object of calculator class
			CheckForErrors error=new CheckForErrors(c.getExpression());
			error.check();
		
			c.setAnswer(Ans);
			c.lookAroundBrackets();
			c.parseForStar();
			c.parseExp();			  // parsing the expression for choosing the sign gained after multiplication or division
			
			if(c.containsOperand()) {
		
				
				c.parseExpFurther();	 // further parsing where various functions will get replaced by its values
		
				while(c.containsBrackets()) {  // check for brackets as they have the highest precision
					c.solveBrackets();	
					c.lookAroundBrackets();
					// solving for brackets accordingly until no bracket is left and replacing the value with answer
				}
		
				c.parseExp();  		// once again parsing after solving for brackets
		
				while(c.calculateOrder()!=-1) {   // will run the loop until no more operator is left, except for + or - at 0 index
			
					c.calculateEq();          // check the definitions of these methods to see how they work
					c.calculateAnswer();
					c.makeNewExp();
				}
			}
			else {
				if(c.containsBrackets())
					c.removeBrackets();
			}
			double ans=c.getAnswer();   // getting the answer after all the calculations
		
			DecimalFormat df=new DecimalFormat("#.0000000000");			// formatting the answer up to 10 decimal places
			ans=Double.parseDouble(df.format(ans));					   // after formatting storing the answer in the variable
		
			System.out.printf("Answer: %s",c.formatAnswer(ans));	   // finally printing the formatted answer up to 10 decimal places
		
			Ans=c.getAnswer();
			System.out.print("\n\n\nEnter '0' to exit and '1' to continue.\nChoice: ");
			choice=input.nextInt();
			
			if(choice==0) {
				System.exit(0);
			}
		}	
	}
}

class Calculator {
	
	private Scanner input=new Scanner(System.in);   			// to take input from user
	private String expression;									// this will hold the expression entered by user	
	private String leftOperand="",rightOperand="";				// these will store the left and right operand of one operator at a time
	private int startInd,endInd;								// starting and ending index where new value is to be replaced
	private char operator;										// to store the type of operator (either + or - or *)
	private double answer=0,array[]=new double[2];				
	
	// answer will store the temporary answer of the operation performed, array will store the left and right operand in double format
	
	public final static char []operatorsList= {'^','/','*','+','-'};   
	public final static String[] functions= {"acos","asin","atan","ln","log","tanh","sinh","cosh","sqrt","cbrt","|","cos","sin","tan"};
	
	// here there are operators along with their precedence and below it are the other mathematics functions
	
	Calculator() {   // default constructor
		
		System.out.print("\nEnter an expression: ");
		expression=input.nextLine();
		expression=expression.replaceAll("\\s", "");   // to remove any spaces that was added either deliberately or mistakenly
	}
	
	Calculator(String expression) {   // parameterized constructor, which takes as an argument a string containing expression
		
		this.expression=expression.replaceAll("\\s", "");
	}

	public void parseForStar() {	 // this function adds multiplication sign between 2 and pi/Ans/e/sqrt if "2pi" is written
		
		int searchingIndex=0,index;
		String toReplace[]= {"Ans","pi","e","sqrt","cbrt"};
		
		for(int i=0;i<toReplace.length;i++) {
			
			for(int j=0;j<expression.length();j++) {
			
				index=expression.indexOf(toReplace[i],searchingIndex);
				if(index==-1)
					break;
				else if(index!=0) {
						if((expression.charAt(index-1)>='0' && expression.charAt(index-1) <='9') || expression.charAt(index-1)=='.') {
							StringBuilder str=new StringBuilder(expression);
							str.replace(index, index, "*");
							expression=str.toString();
						}
				}
				searchingIndex=index+3;
			}
		}
	}
	
	public void parseExp() {   	// parsing the expression for choosing the sign gained after multiplication or division
		
		expression=expression.replaceAll("e",Double.toString(Math.E));
		expression=expression.replaceAll("pi",Double.toString(Math.PI));
		expression=expression.replaceAll("Ans",Double.toString(answer));   // replaces previous answer with Ans if there is any in expression
		
		char find;
		for(int i=1;i<3;i++) {
			find=operatorsList[i];    // only checks for 2 operators which are division and multiplication
			for(int j=0;j<expression.length();j++) {
				if(find==expression.charAt(j)) {
					if(expression.charAt(j+1)=='-') {
						for(int k=j-1;k!=-1;k--) {
							if(expression.charAt(k)=='-') {
								StringBuilder str=new StringBuilder(expression);
								str.setCharAt(k, '+');
								str.deleteCharAt(j+1);
								expression=str.toString();
								break;
							}
							else if(expression.charAt(k)=='+') {
								StringBuilder str=new StringBuilder(expression);
								str.setCharAt(k,'-');
								str.deleteCharAt(j+1);
								expression=str.toString();
								break;
							}
							else if(k==0) {
								StringBuilder str=new StringBuilder(expression);
								str.deleteCharAt(j+1);
								str.replace(k,k,"-");
								expression=str.toString();
								break;
							}
						}
					}
				}
			}
		}
		for(int i=0;i<expression.length();i++) {    // this replaces "--" to "+"
			if(expression.charAt(i)=='-' && expression.charAt(i+1)=='-') {
				StringBuilder str=new StringBuilder(expression);
				str.deleteCharAt(i+1);
				str.setCharAt(i, '+');
				expression=str.toString();
			}
		}
	}
	
	public void parseExpFurther() {    // this method makes further replacements in the expression
		
		for(int i=0;i<functions.length;i++) {
			if(expression.contains(functions[i])) {   	// checks if math functions are there in expression
				replaceWithValue(i);  	   			   // if a function is found, it replaces it with it's value
				i=-1;	 						   	  // resets the value of i to start iteration from the beginning
			}
			
		}
	}
	
	public void replaceWithValue(int func) {  // replaces the function found in expression with its value
		
		int index1=expression.indexOf(functions[func]);   // starting index of function
		int index2=0,j,run=0;
		String shortExp="";								// short expression, to store the expression inside any function
		double ans=0,num;							   //  declaring necessary variables
		
		for(int i=index1;i<expression.length();i++) { 	// starts from the starting index of bracket
			if(expression.charAt(i)=='(') {
				for(j=i+1;expression.charAt(j)!=')';j++) {		// continue to form shortExp until right bracket is achieved
					if(expression.charAt(j)=='(') {
						run++;
					}
					shortExp+=expression.charAt(j);
				}
				if(run!=0) {				// in case more brackets are encountered
					for(int m=0;m<run;m++)
						shortExp+=expression.charAt(j+m);
				}
				index2=j+1+run;
				break;
			}
			else if(expression.charAt(i)=='|') {   			// special case for absolute function
				for(j=i+1;expression.charAt(j)!='|';j++) {
					shortExp+=expression.charAt(j);
				}
				index2=j+1;
				break;
			}
		}
		
		shortExp=lookAroundBrackets(shortExp);    // same as done above, will check for multiplication
		
		while(containsBrackets(shortExp)) {		// will solve brackets inside the trigonometric function
			
			shortExp=solveBrackets(shortExp);
			shortExp=lookAroundBrackets(shortExp);
		}
		
		while(calculateOrder(shortExp)!=-1) {
			calculateEq(shortExp);
			calculateAnswer();
			shortExp=makeNewExp(shortExp); 
		}
		
		num=Double.parseDouble(shortExp);
		
		switch(func) {    					// switch-case according to index of function, by the index of instance variable
		case 0:
			ans=Math.acos(num);
			break;
		case 1:
			ans=Math.asin(num);
			break;
		case 2:
			ans=Math.atan(num);
			break;
		case 3:
			ans=Math.log(num);
			break;
		case 4:
			ans=Math.log10(num);
			break;
		case 5:
			ans=Math.tanh(num);
			break;
		case 6:
			ans=Math.sinh(num);
			break;
		case 7:
			ans=Math.cosh(num);
			break;
		case 8:
			ans=Math.sqrt(num);
			break;
		case 9:
			ans=Math.cbrt(num);
			break;
		case 10:
			ans=Math.abs(num);
			break;
		case 11:
			ans=Math.cos(num);
			break;
		case 12:
			ans=Math.sin(num);
			break;
		case 13:
			ans=Math.tan(num);
			break;
		}
		
		makeNewExp(ans,index1,index2);    // makes a new expression by replacing the value
		answer=ans;						 //  temporary assigns answer
	}
	
	public boolean containsBrackets() {    			// checks if the entered expression has brackets or not
		
		for(int i=0;i<expression.length();i++) {
			if(expression.charAt(i)=='(')
				return true;
		}
		return false;
	}
	
	public boolean containsBrackets(String e) {    	// checks if the entered expression has brackets or not, overloaded version
		
		for(int i=0;i<e.length();i++) {
			if(e.charAt(i)=='(')
				return true;
		}
		return false;
	}
	
	public boolean lookAroundBrackets() {
		
		/* this function look towards left side of the bracket and add a multiplication sign there if there is no operator
		 * there so that the program get to know that it has to do multiplication */
		
		int order[]=getBracketIndex();
		StringBuilder str=new StringBuilder(expression);
		if(order[0]!=0) {
			for(int i=order[0]-1;i>=0;i--) {
				if(expression.charAt(i)!='(' && expression.charAt(i)!='+' && expression.charAt(i)!='-' && expression.charAt(i)!='*' && expression.charAt(i)!='^' && expression.charAt(i)!='/') {
					str.replace(i+1, i+1, "*");
					expression=str.toString();
					return true;
				}
				else if(expression.charAt(i)==')') {
					str.replace(i+1, i+1, "*");
					expression=str.toString();
					return true;
				}
				else if((expression.charAt(i)>='g' && expression.charAt(i)<='t') || expression.charAt(i)=='+' ||  expression.charAt(i)=='-' || expression.charAt(i)=='*' || expression.charAt(i)=='^' || expression.charAt(i)=='/') {
					break;
				}
			}
		}
		return false;
	}
	
	public String lookAroundBrackets(String exp) {  // overloaded version
		
		/* this function look towards left side of the bracket and add a multiplication sign there if there is no operator
		 * there so that the program get to know that it has to do multiplication */
		
		int order[]=getBracketIndex(exp);
		StringBuilder str=new StringBuilder(exp);
		if(order[0]!=0) {
			for(int i=order[0]-1;i>=0;i--) {
				if((exp.charAt(i)>='g' && exp.charAt(i)<='t') || exp.charAt(i)=='+' ||  exp.charAt(i)=='-' || exp.charAt(i)=='*' || exp.charAt(i)=='^' || exp.charAt(i)=='/') {
					break;
				}
				else if(exp.charAt(i)!='(' && exp.charAt(i)!='+' && exp.charAt(i)!='-' && exp.charAt(i)!='*' && exp.charAt(i)!='^' && exp.charAt(i)!='/') {
					str.replace(i+1, i+1, "*");
					return str.toString();
				}
			}
		}
		return str.toString();
	}
	
	public void solveBrackets() {   		// this method solves the bracket first and replaces it with its value
		
		String shortExp="";    			   // this shortExp contains the short expression that is inside expression having most precedence
		int order[]=getBracketIndex();
		for(int i=order[0]+1;i<order[1];i++) {
			shortExp+=expression.charAt(i);
		}
		
		if(calculateOrder(shortExp)==-1)
			answer=Double.parseDouble(shortExp);
		
		while(calculateOrder(shortExp)!=-1) {    // simplifies the shortExp until no operand is left
			calculateEq(shortExp);
			calculateAnswer();
			shortExp=makeNewExp(shortExp); 
		}
		
		StringBuilder str=new StringBuilder(expression);
		str.replace(order[0], order[1]+1, Double.toString(answer));
		expression=str.toString();    // replaces the expression with updated values with the help of StringBuilder class
		
	}
	
	public String solveBrackets(String exp) {   	// this method solves the bracket first and replaces it with its value
		
		String shortExp="";    			   // this shortExp contains the short expression that is inside expression having most precedence
		int order[]=getBracketIndex(exp);
		for(int i=order[0]+1;i<order[1];i++) {
			shortExp+=exp.charAt(i);
		}
		
		while(calculateOrder(shortExp)!=-1) {    // simplifies the shortExp until no operand is left
			calculateEq(shortExp);
			calculateAnswer();
			shortExp=makeNewExp(shortExp); 
		}
		
		StringBuilder str=new StringBuilder(exp);
		str.replace(order[0], order[1]+1, Double.toString(answer));
		return str.toString();    // replaces the expression with updated values with the help of StringBuilder class
		
	}
	
	public int[] getBracketIndex() {     // returns the starting and ending indexes of the inner most bracket in the form of array
		
		int arr[]=new int[2],i,j;   	// some necessary variables
		
		for(i=0;i<expression.length();i++) {
			if(expression.charAt(i)=='(') {    				  // parse through expression until left bracket is found
				arr[0]=i;					  				 //  stores the index
				for(j=i+1;expression.charAt(j)!=')';j++) {  //   parse through until closing bracket or right bracket is found
					if(expression.charAt(j)=='(') {		   // 	 if another left bracket is found, index is shifted
						arr[0]=j;
					}
				}
				if(expression.charAt(j)==')') {     // this stores the ending index of bracket in array
					arr[1]=j;
					break;
				}
			}
		}
		return arr;
	}
	
	public int[] getBracketIndex(String e) {     // returns the starting and ending indexes of the inner most bracket in the form of array
		// overloaded version
		int arr[]=new int[2],i,j;   	// some necessary variables
		
		for(i=0;i<e.length();i++) {
			if(e.charAt(i)=='(') {    				  // parse through expression until left bracket is found
				arr[0]=i;					  				 //  stores the index
				for(j=i+1;e.charAt(j)!=')';j++) {  //   parse through until closing bracket or right bracket is found
					if(e.charAt(j)=='(') {		   // 	 if another left bracket is found, index is shifted
						arr[0]=j;
					}
				}
				if(e.charAt(j)==')') {     // this stores the ending index of bracket in array
					arr[1]=j;
					break;
				}
			}
		}
		return arr;
	}
	
	public int calculateOrder() {    // it returns the index of operator as well as checks that if there is any operator
		
		char toSearchFor;
		for(int i=0;i<operatorsList.length;i++) {    // one by one checking each operator in accordance with their precedence
			toSearchFor=operatorsList[i];
			for(int j=0;j<expression.length();j++) {
				if((toSearchFor=='-' || toSearchFor=='+') && j==0)    // the zeroth index plus or minus is ignored
					continue;
				else if((j-1!=-1) && expression.charAt(j)=='-' && expression.charAt(j-1)=='E')  // for very small values like sin(2*pi)
					continue;
				else if(toSearchFor==expression.charAt(j) || expression.charAt(j)=='x' || expression.charAt(j)=='X') {
					
					// if after all checks, operator is found, it is stored as well as it's index is returned
					
					operator=expression.charAt(j);
					return j;
				}
			}
		}
		return -1;     // returns -1 is no operator is found
	}
	
	public int calculateOrder(String exp) {   // overloaded version of calculateOrder, working is same as above
		
		char toSearchFor;
		for(int i=0;i<operatorsList.length;i++) {    // checks for each operator one by one
			toSearchFor=operatorsList[i];
			for(int j=0;j<exp.length();j++) {
				if((toSearchFor=='-' || toSearchFor=='+') && j==0)
					continue;
				else if((j-1!=-1) && exp.charAt(j)=='-' && exp.charAt(j-1)=='E')
					continue;
				else if(toSearchFor==exp.charAt(j)) {
					operator=exp.charAt(j);
					return j;
				}
			}
		}
		return -1;
	}
	
	public void calculateEq() {     // it calculates the portion of the expression according to BODMAS
		
		// its working is connected with calculateOrder method
		
		leftOperand="";
		rightOperand="";     			// to store left and right operand against the operator
		int i,j=calculateOrder();	   //  stores the index/position of the operator
		
		for(i=j-1;i!=0;i--) {		  //  parse through left of the operator until end or another operator is reached
			if(expression.charAt(i)=='/' ||expression.charAt(i)=='*' || expression.charAt(i)=='+' || expression.charAt(i)=='-') {
				i++;
				break;
			}
		}
		startInd=i;  		 // stores the starting index of short expression, where we will replace our value
		for(;i!=j;i++) {    //  stores the left operand
			leftOperand+=expression.charAt(i);
		}
		
		for(i=j+1;i+1!=expression.length();i++) {    // parse through right of the operator until end or another operator is reached
			if(expression.charAt(j)=='^' && expression.charAt(i)=='-')
				continue;
			else if(expression.charAt(i)=='/' ||expression.charAt(i)=='*' || expression.charAt(i)=='+' || expression.charAt(i)=='-') {
				i--;
				break;
			}
		}
		endInd=i+1;					// stores the ending index of short expression, where we will replace our value
		for(j++;j!=i+1;j++) {	   //  stores the right operand
			rightOperand+=expression.charAt(j);
		}
	}
	
	public void calculateEq(String exp) {     // overloaded version of calculateEq, which takes the string as an expression and solve it
											 //  working is same as the other method described above
		leftOperand="";
		rightOperand="";
		int i,j=calculateOrder(exp);
		for(i=j-1;i!=0;i--) {
			if(exp.charAt(i)=='/' ||exp.charAt(i)=='*' || exp.charAt(i)=='+' || exp.charAt(i)=='-') {
				i++;
				break;
			}
		}
		startInd=i;
		for(;i!=j;i++) {
			leftOperand+=exp.charAt(i);
		}
		for(i=j+1;i+1!=exp.length();i++) {
			if(exp.charAt(i)=='/' ||exp.charAt(i)=='*' || exp.charAt(i)=='+' || exp.charAt(i)=='-') {
				i--;
				break;
			}
		}
		endInd=i+1;
		for(j++;j!=i+1;j++) {
			rightOperand+=exp.charAt(j);
		}
	}
	
	public void calculateAnswer() {
		
		array[0]=Double.parseDouble(leftOperand);      // converts the operands into double format
		if(rightOperand.length()!=0)
			array[1]=Double.parseDouble(rightOperand);    
		else
			array[1]=0;   // in case rightOperand has no value, 0 is assigned to it as a default value
		
		answer=array[0];   // temporarily stores the left operand as an answer, then use switch-case for operation to be performed
		
			switch(operator) {
			
			case '^':
				answer=Math.pow(array[0],array[1]);    // calculates power
				break;
			case '/':
				answer/=array[1];
				break;
			case '*':    // all these version of multiplication can be used
			case 'x':
			case 'X':
				answer*=array[1];
				break;
			case '+':
				answer+=array[1];
				break;
			case '-':
				answer-=array[1];
				break;
				
			}
		}
	
	public void makeNewExp() {  // it modifies the expression, by replacing the short expression by its respective answer
		
		StringBuilder exp=new StringBuilder(expression);
		this.expression=(exp.replace(startInd,endInd,Double.toString(answer))).toString();
	}
	
	public String makeNewExp(String exp1) {  // overloaded version of makeNewExp method
		
		StringBuilder exp=new StringBuilder(exp1);   // modifies the string given to it by replacing with answer
		return exp.replace(startInd,endInd,Double.toString(answer)).toString();
	}
	
	public void makeNewExp(double a,int start,int end) {   // overloaded version of makeNewExp method
		
		StringBuilder str=new StringBuilder(expression);   // It modifies the expression by replacing the function by its value
		str.replace(start, end, Double.toString(a));
		this.expression=str.toString();
	}
	
	public double getAnswer() { // to get the value of answer
		return this.answer;
	}
	
	public void setAnswer(double answer) {  // to set the value of answer
		this.answer=answer;
	}
	
	public String formatAnswer(double a) {     // formats answer so that it looks nicer
		
		StringBuilder str=new StringBuilder(Double.toString(a));
		
		for(int i=str.length()-1;str.charAt(i)=='0';i--) {
			str.deleteCharAt(i);
		}
		if(str.charAt(str.length()-1)=='.') {
			str.deleteCharAt(str.length()-1);
		}
		return str.toString();
	}

	public boolean containsOperand() {     // this method checks if the expression has any operator or not
		
		for(int i=0;i<expression.length();i++) {
			if(i!=0) {
				if(expression.charAt(i)=='+' || expression.charAt(i)=='-' || expression.charAt(i)=='*' || expression.charAt(i)=='/' || expression.charAt(i)=='^') {
					return true;
				}
			}
			else {
				for(int j=0;j<functions.length;j++) {
					if(expression.contains(functions[j])) {
						return true;
					}
				}
			}
		}
		if(!containsBrackets())
			answer=Double.parseDouble(expression);    // if there is no operator, the answer is assigned the only value expression has
		return false;
	}
	
	public String getExpression() {    // returns the expression
		return expression;
	}
	
	public boolean containsFunctions(String e) {	// check whether the given string has more functions like cosine/sin/tan etc
		
		for(int i=0;i<functions.length;i++) {
			if(e.contains(functions[i]))
				return true;
		}
		return false;
	}
	
	public void removeBrackets() {   // this method removes bracket from the given expression
		
		int arr[]=getBracketIndex();
		StringBuilder str=new StringBuilder(expression);
		str.deleteCharAt(arr[1]);
		str.deleteCharAt(arr[0]);
		expression=str.toString();
		answer=Double.parseDouble(expression);
	}
}


class CheckForErrors {
	
	private String expToBeChecked;
	private char[] exp;
	
	CheckForErrors(String exp) {
		expToBeChecked=exp;
		this.exp=expToBeChecked.toCharArray();
	}
	
	public void check() {
		
		checkLength();
		checkBrackets();
	//	checkDivideByZero();        // commented this deliberately
		checkDecimalPoint();
		checkEmptyBrackets();
	//	checkRightOfBracket();
		checkPoint();
		checkSqrt();
	}
	
	private void checkLength() {    // checks the length of the entered expression, it must not be zero
		
		if(expToBeChecked.length()==0) {
			System.out.println("Error: The expression has no parameters. Please try again.\n");
			System.exit(0);
		}
	}
	
	private void checkBrackets() {   // counts left and right brackets
		
		int leftCount=0,rightCount=0;
		
		for(int i=0;i<exp.length;i++) {
			if(exp[i]=='(')
				leftCount++;
			else if(exp[i]==')')
				rightCount++;
		}
		
		if(leftCount!=rightCount) {
			System.out.println("Error: Unequal left and right brackets. Please try again.\n");
			System.exit(0);
		}
	}
	
	private void checkDivideByZero() {
		
		double num;
		String s="";
		
		for(int i=0;i<exp.length;i++) {
			if(exp[i]=='/') {
				for(int j=i+1;j<exp.length;j++) {
					s+=exp[j];
				}
				num=Double.parseDouble(s);
				if(num==0) {
					System.out.println("Error: Divison by zero.");
					System.exit(0);
				}
			}
		}
	}
	
	private void checkDecimalPoint() {
		
		for(int i=0;i<exp.length;i++) {
			if(exp[i]=='.') {
				for(int j=i+1;j<exp.length;j++) {
					if(exp[j]=='+' || exp[j]=='-' || exp[j]=='/' || exp[j]=='*' || exp[j]=='X' || exp[j]=='x' || exp[j]=='^' || exp[j]==')' || exp[j]=='(') {
							break;
					}
					else if(exp[j]=='.') {
						System.out.println("Error: Multiple points in a single number.");
						System.exit(0);
					}
				}
			}
		}
	}
	
	private void checkEmptyBrackets() {
		
		for(int i=0;i<exp.length;i++) {
			if(exp[i]=='(' && exp[i+1]==')') {
				System.out.println("Error: Empty brackets.");
				System.exit(0);
			}
		}
	}
	
	private void checkRightOfBracket() {
		
		for(int i=0;i<exp.length;i++) {
			if(exp[i]==')' && i+1!=exp.length) {
				if(exp[i+1]==')')
					continue;
				else if(exp[i+1]!='+' && exp[i+1]!='-' && exp[i+1]!='*' && exp[i+1]!='/'&& exp[i+1]!='^' && exp[i+1]!='x' && exp[i+1]!='X') {
					System.out.println("Error: Operand to right of bracket is missing.");
					System.exit(0);
				}
			}
		}
	}
	
	private void checkPoint() {
		
		for(int i=0;i<exp.length;i++) {
			if(exp[i]=='.' && i==0 && i+1==exp.length) {
				System.out.println("Error: No argument found.");
				System.exit(0);
			}
			else if(exp[i]=='.' && i==0) {
				if(!(exp[i+1]>='0' && exp[i+1]<='9')) {
					System.out.println("Error: No argument found.");
					System.exit(0);
				}
			}
			else if((exp[i]=='.' && i+1==exp.length) || (exp[i]=='.' && (exp[i+1]=='+' || exp[i+1]=='-' || exp[i+1]=='*' || exp[i+1]=='/' || exp[i+1]=='^'))) {
				if(!(exp[i-1]>='0' && exp[i-1]<='9')) {
					System.out.println("Error: No argument found.");
					System.exit(0);
				}
			}
		}
	}
	
	private void checkSqrt() {
		
		int searchingIndex=0,index;
		for(int i=0;i<exp.length;i++) {
			index=expToBeChecked.indexOf("sqrt",searchingIndex);
			if(index==-1)
				break;
			else {
				if(exp[index+5]=='-') {
					System.out.println("Error: Square root of negative number is not possible.");
					System.exit(0);
				}
				else
					searchingIndex=index+5;
			}
		}
	}
	
}






