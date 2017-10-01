package cop5556fa17;

import static cop5556fa17.Scanner.Kind.EOF;

import cop5556fa17.Scanner.Kind;
import cop5556fa17.Scanner.Token;

public class SimpleParser {

	@SuppressWarnings("serial")
	public class SyntaxException extends Exception {
		Token t;

		public SyntaxException(Token t, String message) {
			super(message);
			this.t = t;
		}

	}

	Scanner scanner;
	Token t;

	SimpleParser(Scanner scanner) {
		this.scanner = scanner;
		t = scanner.nextToken();
	}

	/**
	 * Main method called by compiler to parser input. Checks for EOF
	 *
	 * @throws SyntaxException
	 */
	public void parse() throws SyntaxException {
		program();
		matchEOF();
	}

	public void match(Kind k) throws SyntaxException {

		System.out.println("t.kind : " + t.kind +"  " + "k :" + k);
		if (t.kind == k) {
			t = scanner.nextToken();
		} else {
			System.out.println("flag");
			throw new SyntaxException(t, "Error at match");
		}
	}

	/**
	 * Program ::= IDENTIFIER ( Declaration SEMI | Statement SEMI )*
	 *
	 * Program is start symbol of our grammar.
	 *
	 * @throws SyntaxException
	 */
	public void program() throws SyntaxException {
		// TODO implement this

		match(Kind.IDENTIFIER);
		while (t.kind == Kind.KW_int || t.kind == Kind.KW_boolean || t.kind == Kind.KW_image
				|| t.kind == Kind.KW_url || t.kind == Kind.KW_file || t.kind == Kind.IDENTIFIER) {

			if (t.kind == Kind.KW_int || t.kind == Kind.KW_boolean || t.kind == Kind.KW_image
					|| t.kind == Kind.KW_url || t.kind == Kind.KW_file) {
				declaration();
				match(Kind.SEMI);
			}

			if (t.kind == Kind.IDENTIFIER) {

				statement();
				match(Kind.SEMI);
			}
		}
	}

	public void primary() throws SyntaxException {

		if (t.kind == Kind.INTEGER_LITERAL) {
			match(Kind.INTEGER_LITERAL);
		}
		else if (t.kind == Kind.LPAREN) {
			match(Kind.LPAREN);
			expression();
			match(Kind.RPAREN);
		}
		else if (t.kind == Kind.BOOLEAN_LITERAL) {
			match(Kind.BOOLEAN_LITERAL);
		}
		else if (t.kind == Kind.KW_sin || t.kind == Kind.KW_cos || t.kind == Kind.KW_atan
				|| t.kind == Kind.KW_abs || t.kind == Kind.KW_cart_x || t.kind == Kind.KW_cart_y
				|| t.kind == Kind.KW_polar_a || t.kind == Kind.KW_polar_r) {

			functionApplication();
		}
		else {
			throw new SyntaxException(t, "Error at primary");
		}
	}

	public void functionApplication() throws SyntaxException {

		functionName();
		if (t.kind == Kind.LPAREN) {
			match(Kind.LPAREN);
			expression();
			match(Kind.RPAREN);

		}
		else if (t.kind == Kind.LSQUARE) {
			match(Kind.LSQUARE);
			selector();
			match(Kind.RSQUARE);
		}
		else {
			throw new SyntaxException(t, "Error at functionApplication");
		}
	}

	public void selector() throws SyntaxException {
		expression();
		match(Kind.COMMA);
		expression();
	}

	public void statement() throws SyntaxException {

		match(Kind.IDENTIFIER);
		if (t.kind == Kind.OP_RARROW) {
			imageOutStatement();
		}
		else if (t.kind == Kind.OP_LARROW) {
			imageInStatement();
		}
		else {
			lhs();
			match(Kind.OP_ASSIGN);
			expression();
		}
	}

	public void lhs() throws SyntaxException {

		if (t.kind == Kind.LSQUARE) {
			match(Kind.LSQUARE);
			lhsSelector();
			match(Kind.RSQUARE);
		}
	}

	public void imageOutStatement() throws SyntaxException {

		match(Kind.OP_RARROW);
		sink();
	}

	public void sink() throws SyntaxException {

		if (t.kind == Kind.IDENTIFIER)
			match(Kind.IDENTIFIER);
		else if (t.kind == Kind.KW_SCREEN)
			match(Kind.KW_SCREEN);
		else
			throw new SyntaxException(t, "Error at sink");
	}

	public void imageInStatement() throws SyntaxException {

		match(Kind.OP_LARROW);
		source();
	}

	public void lhsSelector() throws SyntaxException {

		match(Kind.LSQUARE);
		if (t.kind == Kind.KW_x) {
			xySelector();
		}
		else if (t.kind == Kind.KW_r) {
			raSelector();
		}
		else
			throw new SyntaxException(t, "Error at Lhs selector");
		match(Kind.RSQUARE);
	}

	public void raSelector() throws SyntaxException {
		match(Kind.KW_r);
		match(Kind.COMMA);
		match(Kind.KW_A);
	}

	public void xySelector() throws SyntaxException {
		match(Kind.KW_x);
		match(Kind.COMMA);
		match(Kind.KW_y);
	}

	public void unaryExpressionNotPlusMinus() throws SyntaxException {

		if (t.kind == Kind.KW_X) {
			match(Kind.KW_X);
		} else if (t.kind == Kind.KW_x) {
			match(Kind.KW_x);
		} else if (t.kind == Kind.KW_y) {
			match(Kind.KW_y);
		} else if (t.kind == Kind.KW_Y) {
			match(Kind.KW_Y);
		} else if (t.kind == Kind.KW_Z) {
			match(Kind.KW_Z);
		} else if (t.kind == Kind.KW_R) {
			match(Kind.KW_R);
		} else if (t.kind == Kind.KW_r) {
			match(Kind.KW_r);
		} else if (t.kind == Kind.KW_a) {
			match(Kind.KW_a);
		} else if (t.kind == Kind.KW_A) {
			match(Kind.KW_A);
		} else if (t.kind == Kind.KW_DEF_X) {
			match(Kind.KW_DEF_X);
		} else if (t.kind == Kind.KW_DEF_Y) {
			match(Kind.KW_DEF_Y);
		}
		else if (t.kind == Kind.IDENTIFIER) {
			identOrPixelSelectorExpression();
		}
		else if (t.kind == Kind.KW_sin || t.kind == Kind.KW_cos || t.kind == Kind.KW_atan || t.kind == Kind.KW_abs
				|| t.kind == Kind.KW_cart_x || t.kind == Kind.KW_cart_y || t.kind == Kind.KW_polar_a
				|| t.kind == Kind.KW_polar_r || t.kind == Kind.BOOLEAN_LITERAL || t.kind == Kind.INTEGER_LITERAL
				|| t.kind == Kind.LPAREN) {

			primary();
		} else if (t.kind == Kind.OP_EXCL) {
			match(Kind.OP_EXCL);
			unaryExpression();
		} else
			throw new SyntaxException(t, " error at unaryExpressionNotPlusMinus");
	}

	public void unaryExpression() throws SyntaxException {

		if (t.kind == Kind.OP_PLUS) {
			match(Kind.OP_PLUS);
			unaryExpression();
		}
		else if (t.kind == Kind.OP_MINUS) {
			match(Kind.OP_MINUS);
			unaryExpression();
		}
		else if (t.kind == Kind.KW_Z || t.kind == Kind.KW_A || t.kind == Kind.KW_R || t.kind == Kind.KW_DEF_X
				|| t.kind == Kind.KW_DEF_Y || t.kind == Kind.IDENTIFIER || t.kind == Kind.OP_EXCL
				|| t.kind == Kind.KW_x || t.kind == Kind.KW_y || t.kind == Kind.KW_r
				|| t.kind == Kind.KW_a || t.kind == Kind.KW_X || t.kind == Kind.KW_Y ||

				t.kind == Kind.INTEGER_LITERAL || t.kind == Kind.LPAREN || t.kind == Kind.KW_sin
				|| t.kind == Kind.KW_cos || t.kind == Kind.KW_atan || t.kind == Kind.KW_abs || t.kind == Kind.KW_cart_x
				|| t.kind == Kind.KW_cart_y || t.kind == Kind.KW_polar_a || t.kind == Kind.KW_polar_r
				|| t.kind == Kind.BOOLEAN_LITERAL || t.kind == Kind.INTEGER_LITERAL || t.kind == Kind.LPAREN)

		{
			unaryExpressionNotPlusMinus();

		}

		else
			throw new SyntaxException(t, "Error at unaryExpression");
	}

	public void multExpression() throws SyntaxException {

		unaryExpression();
		while (t.kind == Kind.OP_TIMES || t.kind == Kind.OP_DIV || t.kind == Kind.OP_MOD) {

			if (t.kind == Kind.OP_TIMES) {
				match(Kind.OP_TIMES);
			} else if (t.kind == Kind.OP_DIV) {
				match(Kind.OP_DIV);
			} else if (t.kind == Kind.OP_MOD) {
				match(Kind.OP_MOD);
			}
			unaryExpression();
		}
	}

	public void addExpression() throws SyntaxException {

		multExpression();
		while (t.kind == Kind.OP_PLUS || t.kind == Kind.OP_MINUS) {

			if (t.kind == Kind.OP_PLUS) {
				match(Kind.OP_PLUS);
			} else if (t.kind == Kind.OP_MINUS) {
				match(Kind.OP_MINUS);
			}
			multExpression();
		}
	}

	public void relExpression() throws SyntaxException {

		addExpression();
		while (t.kind == Kind.OP_LT || t.kind == Kind.OP_GT || t.kind == Kind.OP_LE || t.kind == Kind.OP_GE) {

			if (t.kind == Kind.OP_LT) {
				match(Kind.OP_LT);
			} else if (t.kind == Kind.OP_GT) {
				match(Kind.OP_GT);
			} else if (t.kind == Kind.OP_LE) {
				match(Kind.OP_LE);
			} else if (t.kind == Kind.OP_GE) {
				match(Kind.OP_GE);
			}
			addExpression();
		}
	}

	public void eqExpression() throws SyntaxException {

		relExpression();

		while (t.kind == Kind.OP_EQ || t.kind == Kind.OP_NEQ) {

			if (t.kind == Kind.OP_EQ) {
				match(Kind.OP_EQ);
			} else if (t.kind == Kind.OP_NEQ) {
				match(Kind.OP_NEQ);
			}
			relExpression();
		}
	}

	public void andExpression() throws SyntaxException {

		eqExpression();
		while (t.kind == Kind.OP_AND) {

			if (t.kind == Kind.OP_AND) {
				match(Kind.OP_AND);
			}
			eqExpression();
		}
	}

	public void orExpression() throws SyntaxException {

		andExpression();
		while (t.kind == Kind.OP_OR) {

			if (t.kind == Kind.OP_OR) {
				match(Kind.OP_OR);
			}
			andExpression();
		}
	}

	public void identOrPixelSelectorExpression() throws SyntaxException {

		match(Kind.IDENTIFIER);
		if (t.kind == Kind.LSQUARE) {
			match(Kind.LSQUARE);
			selector();
			match(Kind.RSQUARE);
		}
	}

	/**
	 * Expression ::= OrExpression OP_Q Expression OP_COLON Expression |
	 * OrExpression
	 *
	 * Our test cases may invoke this routine directly to support incremental
	 * development.
	 *
	 * @throws SyntaxException
	 */
	public void expression() throws SyntaxException {
		orExpression();

		if (t.kind == Kind.OP_Q) {
			match(Kind.OP_Q);
			expression();
			match(Kind.OP_COLON);
			expression();
		}
	}

	public void functionName() throws SyntaxException {
		if (t.kind == Kind.KW_sin)
			match(Kind.KW_sin);
		else if (t.kind == Kind.KW_cos)
			match(Kind.KW_cos);
		else if (t.kind == Kind.KW_atan)
			match(Kind.KW_atan);
		else if (t.kind == Kind.KW_abs)
			match(Kind.KW_abs);
		else if (t.kind == Kind.KW_cart_x)
			match(Kind.KW_cart_x);
		else if (t.kind == Kind.KW_cart_y)
			match(Kind.KW_cart_y);
		else if (t.kind == Kind.KW_polar_a)
			match(Kind.KW_polar_a);
		else if (t.kind == Kind.KW_polar_r)
			match(Kind.KW_polar_r);
		else
			throw new SyntaxException(t, "Error at functionName");
	}

	public void declaration() throws SyntaxException {

		if (t.kind == Kind.KW_int) {
			match(Kind.KW_int);
			variableDeclartion();
		}
		else if (t.kind == Kind.KW_boolean) {
			match(Kind.KW_boolean);
			variableDeclartion();
		}
		else if (t.kind == Kind.KW_image) {
			imageDeclaration();
		}
		else if (t.kind == Kind.KW_url || t.kind == Kind.KW_file) {
			sourceSinkDeclaration();
		} else
			throw new SyntaxException(t, " Error at declartion");
	}

	public void imageDeclaration() throws SyntaxException {
		match(Kind.KW_image);
		if (t.kind == Kind.LSQUARE) {
			match(Kind.LSQUARE);
			expression();
			match(Kind.COMMA);
			expression();
			match(Kind.RSQUARE);
		}
		match(Kind.IDENTIFIER);
		if (t.kind == Kind.OP_LARROW) {
			match(Kind.OP_LARROW);
			source();
		}
	}

	public void sourceSinkDeclaration() throws SyntaxException {
		if(t.kind == Kind.KW_url){
			match(Kind.KW_url);
		}
		else if(t.kind == Kind.KW_file){
			match(Kind.KW_file);
		}
		match(Kind.IDENTIFIER);
		match(Kind.OP_ASSIGN);
		source();
	}

	public void variableDeclartion() throws SyntaxException {
		match(Kind.IDENTIFIER);
		if (t.kind == Kind.OP_ASSIGN) {
			match(Kind.OP_ASSIGN);
			expression();
		}
	}

	public void varType() throws SyntaxException {

		if (t.kind == Kind.KW_int)
			match(Kind.KW_int);
		else if (t.kind == Kind.KW_boolean)
			match(Kind.KW_boolean);
		else
			throw new SyntaxException(t, "Error at varType");
	}

	public void source() throws SyntaxException {

		if (t.kind == Kind.STRING_LITERAL) {
			match(Kind.STRING_LITERAL);
		} else if (t.kind == Kind.OP_AT) {
			match(Kind.OP_AT);
			expression();
		}
		else if (t.kind == Kind.IDENTIFIER) {
			match(Kind.IDENTIFIER);
		}
		else
			throw new SyntaxException(t, "Error at source");
	}

	public void sourceSinkType() throws SyntaxException {

		if (t.kind == Kind.KW_url) {
			match(Kind.KW_url);
		} else if (t.kind == Kind.KW_file) {
			match(Kind.KW_file);
		}
		else
			throw new SyntaxException(t, "Error at sourceSinkType");
	}


	/**
	 * Only for check at end of program. Does not "consume" EOF so no attempt to
	 * get nonexistent next Token.
	 *
	 * @return
	 * @throws SyntaxException
	 */
	private Token matchEOF() throws SyntaxException {
		if (t.kind == EOF) {
			return t;
		}
		String message = "Expected EOL at " + t.line + ":" + t.pos_in_line;
		throw new SyntaxException(t, message);
	}
}
