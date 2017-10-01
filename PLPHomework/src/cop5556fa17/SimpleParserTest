package cop5556fa17;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import cop5556fa17.Scanner.LexicalException;
import cop5556fa17.Scanner.Token;
import cop5556fa17.SimpleParser.SyntaxException;
import static cop5556fa17.Scanner.Kind.*;


public class SimpleParserTest {


        @Rule
        public ExpectedException thrown = ExpectedException.none();


        //To make it easy to print objects and turn this output on and off
        static final boolean doPrint = true;
        private void show(Object input) {
            if (doPrint) {
                System.out.println(input.toString());
            }
        }


        @Test
        public void testEmpty() throws LexicalException, SyntaxException {
            String input = "";  //The input is the empty string.  This is not legal
            show(input);            //Display the input
            Scanner scanner = new Scanner(input).scan();  //Create a Scanner and initialize it
            show(scanner);   //Display the Scanner
            SimpleParser parser = new SimpleParser(scanner);  //Create a parser
            thrown.expect(SyntaxException.class);
            try
            {
                parser.parse();  //Parse the program
            }
            catch (SyntaxException e)
            {
                show(e);
                throw e;
            }
        }


        @Test
        public void testDec1() throws LexicalException, SyntaxException {
            String input = "prog int k;";
            show(input);
            Scanner scanner = new Scanner(input).scan();  //Create a Scanner and initialize it
            show(scanner);   //Display the Scanner
            SimpleParser parser = new SimpleParser(scanner);  //
            parser.parse();
        }


        @Test
        public void expression1() throws SyntaxException, LexicalException {
            String input = "2";
            show(input);
            Scanner scanner = new Scanner(input).scan();
            show(scanner);
            SimpleParser parser = new SimpleParser(scanner);
            parser.expression();
        }

        @Test
    	public void testDeclaration() throws LexicalException, SyntaxException {
	    	String input = "prog file  def=\"24\";";
	    	show(input);
	    	Scanner scanner = new Scanner(input).scan();  //Create a Scanner and initialize it
	    	show(scanner);   //Display the Scanner
	    	SimpleParser parser = new SimpleParser(scanner);  //
	    	parser.parse();
    	}

        @Test
        public void testExpression() throws SyntaxException, LexicalException {
	       	 String input = "a=7>4?10!Z";
	       	 show(input);
	       	 Scanner scanner = new Scanner(input).scan();
	       	 show(scanner);
	       	 SimpleParser parser = new SimpleParser(scanner);
	       	 parser.expression();
        }

        @Test
        public void testExpression1() throws SyntaxException, LexicalException {
            String input =  "sin(x)+cos(x)-atan(x)+cart_x(x)+cart_y(y)+polar_a(a)+polar_r(a)";
            show(input);
            Scanner scanner = new Scanner(input).scan();
            show(scanner);
            SimpleParser SimpleParser = new SimpleParser(scanner);
            SimpleParser.expression();
        }

        @Test
        public void testandExpression() throws SyntaxException, LexicalException
        {
            String input = "a>b&c>d";
            show(input);
            Scanner scanner = new Scanner(input).scan();
            show(scanner);
            SimpleParser parser = new SimpleParser(scanner);
            parser.andExpression();
        }
}
