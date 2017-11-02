package cop5556fa17;

import static cop5556fa17.Scanner.Kind.EOF;

import java.util.ArrayList;

import cop5556fa17.Scanner.Kind;
import cop5556fa17.Scanner.Token;
import cop5556fa17.AST.*;

public class Parser {

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

	Parser(Scanner scanner) {
		this.scanner = scanner;
		t = scanner.nextToken();
	}

	/**
	 * Main method called by compiler to parser input. Checks for EOF
	 *
	 * @throws SyntaxException
	 */
	public Program parse() throws SyntaxException {
		Program root = program();
		matchEOF();
		return root;
	}

	public void match(Kind k) throws SyntaxException {
		System.out.println("t.kind  " + t.kind + " k  " + k);
		if (t.kind == k) {
			t = scanner.nextToken();
		} else {
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
	Program program() throws SyntaxException {
		// TODO implement this
		ArrayList<ASTNode> arr = new ArrayList<ASTNode> ();
		Token first = t;
		match(Kind.IDENTIFIER);
		while (t.kind == Kind.KW_int || t.kind == Kind.KW_boolean || t.kind == Kind.KW_image
				|| t.kind == Kind.KW_url || t.kind == Kind.KW_file || t.kind == Kind.IDENTIFIER) {

			if (t.kind == Kind.KW_int || t.kind == Kind.KW_boolean || t.kind == Kind.KW_image
					|| t.kind == Kind.KW_url || t.kind == Kind.KW_file) {
				arr.add(declaration());
				match(Kind.SEMI);
			}

			else if (t.kind == Kind.IDENTIFIER) {
				arr.add(statement());
				match(Kind.SEMI);
			}
		}
		return new Program(first,first,arr);
	}

	Expression primary() throws SyntaxException {
		Token first = t;
		if (t.kind == Kind.INTEGER_LITERAL) {
			match(Kind.INTEGER_LITERAL);
			return new Expression_IntLit(first,first.intVal());
		}
		else if (t.kind == Kind.LPAREN) {
			match(Kind.LPAREN);
			Expression e = expression();
			match(Kind.RPAREN);
			return e;
		}
		else if (t.kind == Kind.BOOLEAN_LITERAL) {
			match(Kind.BOOLEAN_LITERAL);
			return new Expression_BooleanLit(first,first.getText().equals("true"));
		}
		else if (t.kind == Kind.KW_sin || t.kind == Kind.KW_cos || t.kind == Kind.KW_atan
				|| t.kind == Kind.KW_abs || t.kind == Kind.KW_cart_x || t.kind == Kind.KW_cart_y
				|| t.kind == Kind.KW_polar_a || t.kind == Kind.KW_polar_r) {

			return functionApplication();
		}
		else {
			throw new SyntaxException(t, "Error at primary");
		}
	}

	Expression_FunctionApp functionApplication() throws SyntaxException {
		Token first = t;
		functionName();
		if (t.kind == Kind.LPAREN) {
			match(Kind.LPAREN);
			Expression e = expression();
			match(Kind.RPAREN);
			return new Expression_FunctionAppWithExprArg(first,first.kind,e);
		}
		else if (t.kind == Kind.LSQUARE) {
			match(Kind.LSQUARE);
			Index idx = selector();
			match(Kind.RSQUARE);
			return new Expression_FunctionAppWithIndexArg(first,first.kind,idx);
		}
		else {
			throw new SyntaxException(t, "Error at functionApplication");
		}
	}

	Index selector() throws SyntaxException {

		Expression e0 = expression();
		match(Kind.COMMA);
		Expression e1 = expression();
		return new Index(e0.firstToken,e0,e1);
	}

	Statement statement() throws SyntaxException {

		Token first = t;
		match(Kind.IDENTIFIER);
		if(t.kind == Kind.OP_ASSIGN || t.kind == Kind.LSQUARE){
			return assignment(first);
		}
		else if (t.kind == Kind.OP_RARROW) {
			return imageOutStatement(first);
		}
		else if (t.kind == Kind.OP_LARROW) {
			return imageInStatement(first);
		}
		else {
			throw new SyntaxException(t, "Statement Error");
		}
	}

	Statement_Assign assignment(Token first) throws SyntaxException {
		LHS l = lhs(first);
		match(Kind.OP_ASSIGN);
		Expression e = expression();
		return new Statement_Assign(first,l,e);
	}

	LHS lhs(Token first) throws SyntaxException {

		if (t.kind == Kind.LSQUARE) {
			match(Kind.LSQUARE);
			Index idx = lhsSelector();
			match(Kind.RSQUARE);
			return new LHS(first,first,idx);
		}
		return new LHS(first,first,null);
	}

	Statement_Out imageOutStatement(Token first) throws SyntaxException {

		match(Kind.OP_RARROW);
		Sink si = sink();
		return new Statement_Out(first,first,si);
	}

	Sink sink() throws SyntaxException {
		Token first = t;
		if (t.kind == Kind.IDENTIFIER){
			match(Kind.IDENTIFIER);
			return new Sink_Ident(first,first);
		}
		else if (t.kind == Kind.KW_SCREEN){
			match(Kind.KW_SCREEN);
			return new Sink_SCREEN(first);
		}
		else
			throw new SyntaxException(t, "Error at sink");
	}

	Statement_In imageInStatement(Token first) throws SyntaxException {

		match(Kind.OP_LARROW);
		Source s = source();
		return new Statement_In(first,first,s);
	}

	Index lhsSelector() throws SyntaxException {

		match(Kind.LSQUARE);
		if (t.kind == Kind.KW_x) {
			Index idx = xySelector();
			match(Kind.RSQUARE);
			return idx;
		}
		else if (t.kind == Kind.KW_r) {
			Index idx = raSelector();
			match(Kind.RSQUARE);
			return idx;
		}
		else
			throw new SyntaxException(t, "Error at Lhs selector");

	}

	Index raSelector() throws SyntaxException {
		Token first = t;
		match(Kind.KW_r);
		Expression e0 = new Expression_PredefinedName(first,first.kind);
		match(Kind.COMMA);
		Token tk = t;
		match(Kind.KW_A);
		Expression e1 = new Expression_PredefinedName(tk,tk.kind);
		return new Index(first,e0,e1);
	}

	Index xySelector() throws SyntaxException {
		Token first = t;
		match(Kind.KW_x);
		Expression e0 = new Expression_PredefinedName(first,first.kind);
		match(Kind.COMMA);
		Token tk = t;
		match(Kind.KW_y);
		Expression e1 = new Expression_PredefinedName(tk,tk.kind);
		return new Index(first,e0,e1);
	}

	Expression unaryExpressionNotPlusMinus() throws SyntaxException {
		Token first = t;
		if (t.kind == Kind.KW_X) {
			match(Kind.KW_X);
			return new Expression_PredefinedName(first,first.kind);
		} else if (t.kind == Kind.KW_x) {
			match(Kind.KW_x);
			return new Expression_PredefinedName(first,first.kind);
		} else if (t.kind == Kind.KW_y) {
			match(Kind.KW_y);
			return new Expression_PredefinedName(first,first.kind);
		} else if (t.kind == Kind.KW_Y) {
			match(Kind.KW_Y);
			return new Expression_PredefinedName(first,first.kind);
		} else if (t.kind == Kind.KW_Z) {
			match(Kind.KW_Z);
			return new Expression_PredefinedName(first,first.kind);
		} else if (t.kind == Kind.KW_R) {
			match(Kind.KW_R);
			return new Expression_PredefinedName(first,first.kind);
		} else if (t.kind == Kind.KW_r) {
			match(Kind.KW_r);
			return new Expression_PredefinedName(first,first.kind);
		} else if (t.kind == Kind.KW_a) {
			match(Kind.KW_a);
			return new Expression_PredefinedName(first,first.kind);
		} else if (t.kind == Kind.KW_A) {
			match(Kind.KW_A);
			return new Expression_PredefinedName(first,first.kind);
		} else if (t.kind == Kind.KW_DEF_X) {
			match(Kind.KW_DEF_X);
			return new Expression_PredefinedName(first,first.kind);
		} else if (t.kind == Kind.KW_DEF_Y) {
			match(Kind.KW_DEF_Y);
			return new Expression_PredefinedName(first,first.kind);
		}
		else if (t.kind == Kind.IDENTIFIER) {
			return identOrPixelSelectorExpression();
		}
		else if (t.kind == Kind.KW_sin || t.kind == Kind.KW_cos || t.kind == Kind.KW_atan || t.kind == Kind.KW_abs
				|| t.kind == Kind.KW_cart_x || t.kind == Kind.KW_cart_y || t.kind == Kind.KW_polar_a
				|| t.kind == Kind.KW_polar_r || t.kind == Kind.BOOLEAN_LITERAL || t.kind == Kind.INTEGER_LITERAL
				|| t.kind == Kind.LPAREN) {

			return primary();
		}

		else if (t.kind == Kind.OP_EXCL) {
			match(Kind.OP_EXCL);
			return new Expression_Unary(first,first,unaryExpression());
		}

		else
			throw new SyntaxException(t, " error at unaryExpressionNotPlusMinus");
	}

	Expression unaryExpression() throws SyntaxException {
		Token first = t;
		if (t.kind == Kind.OP_PLUS) {
			match(Kind.OP_PLUS);
			return new Expression_Unary(first,first,unaryExpression());
		}
		else if (t.kind == Kind.OP_MINUS) {
			match(Kind.OP_MINUS);
			return new Expression_Unary(first,first,unaryExpression());
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
			return unaryExpressionNotPlusMinus();

		}

		else
			throw new SyntaxException(t, "Error at unaryExpression");
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
	Expression expression() throws SyntaxException {

		Expression e0 = orExpression();
		Expression e1 = null;
		Expression e2 = null;
		if (t.kind == Kind.OP_Q) {
			match(Kind.OP_Q);
			e1 = expression();
			match(Kind.OP_COLON);
			e2 = expression();
			return new Expression_Conditional(e0.firstToken,e0,e1,e2);
		}
		return e0;
	}


	Expression multExpression() throws SyntaxException {

		Expression e0 = null;
		Expression e1 = null;
		e0 = unaryExpression();
		while (t.kind == Kind.OP_TIMES || t.kind == Kind.OP_DIV || t.kind == Kind.OP_MOD) {
			Token tk = t;
			if (t.kind == Kind.OP_TIMES) {
				match(Kind.OP_TIMES);
			} else if (t.kind == Kind.OP_DIV) {
				match(Kind.OP_DIV);
			} else if (t.kind == Kind.OP_MOD) {
				match(Kind.OP_MOD);
			}
			e1 = unaryExpression();
			e0 = new Expression_Binary(e0.firstToken,e0,tk,e1);
		}
		return e0;
	}

	Expression addExpression() throws SyntaxException {

		Expression e0 = null;
		Expression e1 = null;
		e0 = multExpression();
		while (t.kind == Kind.OP_PLUS || t.kind == Kind.OP_MINUS) {
			Token tk = t;
			if (t.kind == Kind.OP_PLUS) {
				match(Kind.OP_PLUS);
			} else if (t.kind == Kind.OP_MINUS) {
				match(Kind.OP_MINUS);
			}
			e1 = multExpression();
			e0 = new Expression_Binary(e0.firstToken,e0,tk,e1);
		}
		return e0;
	}

	Expression relExpression() throws SyntaxException {

		Expression e0 = null;
		Expression e1 = null;
		e0 = addExpression();
		while (t.kind == Kind.OP_LT || t.kind == Kind.OP_GT || t.kind == Kind.OP_LE || t.kind == Kind.OP_GE) {
			Token tk = t;
			if (t.kind == Kind.OP_LT) {
				match(Kind.OP_LT);
			}
			else if (t.kind == Kind.OP_GT) {
				match(Kind.OP_GT);
			}
			else if (t.kind == Kind.OP_LE) {
				match(Kind.OP_LE);
			}
			else if (t.kind == Kind.OP_GE) {
				match(Kind.OP_GE);
			}
			e1 = addExpression();
			e0 = new Expression_Binary(e0.firstToken,e0,tk,e1);
		}
		return e0;
	}

	Expression eqExpression() throws SyntaxException {

		Expression e0 = null;
		Expression e1 = null;
		e0 = relExpression();
		while (t.kind == Kind.OP_EQ || t.kind == Kind.OP_NEQ) {
			Token tk = t;
			if (t.kind == Kind.OP_EQ) {
				match(Kind.OP_EQ);
			}
			else if (t.kind == Kind.OP_NEQ) {
				match(Kind.OP_NEQ);
			}
			e1 = relExpression();
			e0 = new Expression_Binary(e0.firstToken,e0,tk,e1);
		}
		return e0;
	}

	Expression andExpression() throws SyntaxException {

		Expression e0 = null;
		Expression e1 = null;
		e0 = eqExpression();
		while (t.kind == Kind.OP_AND) {
			Token tk = t;
			match(Kind.OP_AND);
			e1 = eqExpression();
			e0 = new Expression_Binary(e0.firstToken,e0,tk,e1);
		}
		return e0;
	}

	Expression orExpression() throws SyntaxException {

		Expression e0 = null;
		Expression e1 = null;
		e0 = andExpression();
		while (t.kind == Kind.OP_OR) {
			Token tk = t;
			match(Kind.OP_OR);
			e1 = andExpression();
			e0 = new Expression_Binary(e0.firstToken,e0,tk,e1);
		}
		return e0;
	}

	Expression identOrPixelSelectorExpression() throws SyntaxException {
		Token first = t;
		match(Kind.IDENTIFIER);
		Expression_PixelSelector eps = indentOrPixelNext(first);
		if(eps == null){
			return new Expression_Ident(first,first);
		}
		return eps;
	}

	Expression_PixelSelector indentOrPixelNext(Token first) throws SyntaxException {
		if (t.kind == Kind.LSQUARE) {
			match(Kind.LSQUARE);
			Index idx = selector();
			match(Kind.RSQUARE);
			return new Expression_PixelSelector(first,first,idx);
		}
		return  null;
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

	Declaration declaration() throws SyntaxException {

		if (t.kind == Kind.KW_int || t.kind == Kind.KW_boolean) {
			return variableDeclaration();
		}
		else if (t.kind == Kind.KW_image) {
			return imageDeclaration();
		}
		else if (t.kind == Kind.KW_url || t.kind == Kind.KW_file) {
			return sourceSinkDeclaration();
		} else
			throw new SyntaxException(t, " Error at declartion");
	}

	Declaration_Image imageDeclaration() throws SyntaxException {
		Token first = t;
		match(Kind.KW_image);
		Expression e0 = null;
		Expression e1 = null;
		Source s = null;
		if (t.kind == Kind.LSQUARE) {
			match(Kind.LSQUARE);
			e0 = expression();
			match(Kind.COMMA);
			e1 = expression();
			match(Kind.RSQUARE);
		}
		Token tk = t;
		match(Kind.IDENTIFIER);
		if (t.kind == Kind.OP_LARROW) {
			match(Kind.OP_LARROW);
			s = source();
		}
		return new Declaration_Image(first,e0,e1,tk,s);
	}

	Declaration_SourceSink sourceSinkDeclaration() throws SyntaxException {
		Token first = t;
		sourceSinkType();
		Token tk = t;
		match(Kind.IDENTIFIER);
		match(Kind.OP_ASSIGN);
		Source s = source();
		return new Declaration_SourceSink(first,first,tk,s);
	}

	Declaration_Variable variableDeclaration() throws SyntaxException {
		Token first = t;
		varType();
		Token tk = t;
		match(Kind.IDENTIFIER);
		Expression e = null;
		if (t.kind == Kind.OP_ASSIGN) {
			match(Kind.OP_ASSIGN);
			e = expression();
		}
		return new Declaration_Variable(first,first,tk,e);
	}

	public void varType() throws SyntaxException {

		if (t.kind == Kind.KW_int)
			match(Kind.KW_int);
		else if (t.kind == Kind.KW_boolean)
			match(Kind.KW_boolean);
		else
			throw new SyntaxException(t, "Error at varType");
	}

	Source source() throws SyntaxException {
		Token first = t;

		if (t.kind == Kind.STRING_LITERAL) {
			String s = t.getText();
			match(Kind.STRING_LITERAL);
			return new Source_StringLiteral(first,s);
		}

		else if (t.kind == Kind.OP_AT) {
			match(Kind.OP_AT);
			Expression e = expression();
			return new Source_CommandLineParam(first,e);
		}

		else if (t.kind == Kind.IDENTIFIER) {
			Token tk = t;
			match(Kind.IDENTIFIER);
			return new Source_Ident(first,tk);
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
