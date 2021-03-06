package cop5556fa17;

import java.net.*;

import static cop5556fa17.Scanner.Kind.KW_boolean;
import static cop5556fa17.Scanner.Kind.KW_int;
import cop5556fa17.AST.*;

import java.util.HashMap;

import cop5556fa17.Scanner.Kind;
import cop5556fa17.Scanner.Token;
import cop5556fa17.TypeUtils.Type;
import cop5556fa17.AST.ASTNode;
import cop5556fa17.AST.ASTVisitor;
import cop5556fa17.AST.Declaration_Image;
import cop5556fa17.AST.Declaration_SourceSink;
import cop5556fa17.AST.Declaration_Variable;
import cop5556fa17.AST.Expression_Binary;
import cop5556fa17.AST.Expression_BooleanLit;
import cop5556fa17.AST.Expression_Conditional;
import cop5556fa17.AST.Expression_FunctionAppWithExprArg;
import cop5556fa17.AST.Expression_FunctionAppWithIndexArg;
import cop5556fa17.AST.Expression_Ident;
import cop5556fa17.AST.Expression_IntLit;
import cop5556fa17.AST.Expression_PixelSelector;
import cop5556fa17.AST.Expression_PredefinedName;
import cop5556fa17.AST.Expression_Unary;
import cop5556fa17.AST.Index;
import cop5556fa17.AST.LHS;
import cop5556fa17.AST.Program;
import cop5556fa17.AST.Sink;
import cop5556fa17.AST.Sink_Ident;
import cop5556fa17.AST.Sink_SCREEN;
import cop5556fa17.AST.Source_CommandLineParam;
import cop5556fa17.AST.Source_Ident;
import cop5556fa17.AST.Source_StringLiteral;
import cop5556fa17.AST.Statement_Assign;
import cop5556fa17.AST.Statement_In;
import cop5556fa17.AST.Statement_Out;

public class TypeCheckVisitor implements ASTVisitor {


	@SuppressWarnings("serial")
	public static class SemanticException extends Exception {
		Token t;

		public SemanticException(Token t, String message) {
			super("line " + t.line + " pos " + t.pos_in_line + ": "+  message);
			this.t = t;
		}

	}


	HashMap<String, Declaration> symTab = new HashMap<String, Declaration>();


	/**
	 * The program name is only used for naming the class.  It does not rule out
	 * variables with the same name.  It is returned for convenience.
	 *
	 * @throws Exception
	 */
	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		for (ASTNode node: program.decsAndStatements) {
			node.visit(this, arg);
		}
		return program.name;
	}

	@Override
	public Object visitDeclaration_Variable(
			Declaration_Variable dec_Var, Object arg)
					throws Exception {

		if(symTab.containsKey(dec_Var.name)){
			throw new SemanticException(dec_Var.firstToken, "Error: Variable re-declared");
		}
		Type t= TypeUtils.getType(dec_Var.type);
		if(t == null){
			throw new SemanticException(dec_Var.firstToken, "Error: visitDeclaration_Variable.Type not found in TypeUtils");
		}
		dec_Var.setType(t);
		if(dec_Var.e!=null){
			dec_Var.e.visit(this, arg);
			if(dec_Var.getType() != dec_Var.e.getType()){
				throw new SemanticException(dec_Var.firstToken, "Error: visitDeclaration_Variable: Type mismatch");
			}
		}

		symTab.put(dec_Var.name, dec_Var);
		return null;
	}

	@Override
	public Object visitDeclaration_Image(Declaration_Image dec_Img,
			Object arg) throws Exception {

		if(symTab.containsKey(dec_Img.name)){
			throw new SemanticException(dec_Img.firstToken, "Error: Variable re-declared");
		}
		dec_Img.setType(TypeUtils.Type.IMAGE);

		if(dec_Img.xSize!=null && dec_Img.ySize!=null){
			dec_Img.xSize.visit(this, arg);
			dec_Img.ySize.visit(this, arg);
			if(dec_Img.xSize.getType()!=Type.INTEGER || dec_Img.ySize.getType()!=Type.INTEGER){
				throw new SemanticException(dec_Img.firstToken, "Error: Declaration_Image: xSize or ySize type not integer");
			}
		}
		else if(dec_Img.xSize!=null || dec_Img.ySize!=null){
			throw new SemanticException(dec_Img.firstToken, "Error: Declaration_Image: xSize or ySize null");
		}

		if(dec_Img.source!=null){
			dec_Img.source.visit(this, arg);
		}

		symTab.put(dec_Img.name, dec_Img);
		return null;
	}

	@Override
	public Object visitDeclaration_SourceSink(
			Declaration_SourceSink dec_SrcSink, Object arg)
					throws Exception {

		if(symTab.containsKey(dec_SrcSink.name)){
			throw new SemanticException(dec_SrcSink.firstToken, "Error: Variable re-declared");
		}


		Type t= TypeUtils.getType(dec_SrcSink.type);
		if(t == null){
			throw new SemanticException(dec_SrcSink.firstToken, "Error: Declaration_SourceSink.Type not found in TypeUtils");
		}
		dec_SrcSink.setType(t);

		if(dec_SrcSink.source==null)
			throw new SemanticException(dec_SrcSink.firstToken, "Error: Declaration_SourceSink.source should not be null");

		dec_SrcSink.source.visit(this, arg);
		if(dec_SrcSink.source.getType()!=dec_SrcSink.getType())
			throw new SemanticException(dec_SrcSink.firstToken, "Error: Declaration_SourceSink.Type != Source.Type");

		symTab.put(dec_SrcSink.name, dec_SrcSink);

		return null;
	}

	@Override
	public Object visitStatement_Out(Statement_Out statement_Out, Object arg)
			throws Exception {

		Declaration dec = symTab.get(statement_Out.name);

		if(statement_Out.sink==null)
			throw new SemanticException(statement_Out.firstToken, "Error: visitStatement_Out: Sink not defined");

		statement_Out.sink.visit(this, arg);
		if(	dec!=null && (
				((dec.getType()==Type.INTEGER || dec.getType()==Type.BOOLEAN) && statement_Out.sink.getType()==Type.SCREEN)
				|| (dec.getType()==Type.IMAGE && (statement_Out.sink.getType() ==Type.FILE  || statement_Out.sink.getType() == Type.SCREEN))
				)){
			statement_Out.setDec(dec);
		}
		else {
			throw new SemanticException(statement_Out.firstToken, "Error: Either Variable is not declared or incompatible assignment");
		}
		return null;
	}

	@Override
	public Object visitStatement_In(Statement_In statement_In, Object arg)
			throws Exception {

		Declaration dec = symTab.get(statement_In.name);

		if(statement_In.source!=null){
			statement_In.source.visit(this, arg);
		}
		statement_In.setDec(dec);
		return null;
	}

	@Override
	public Object visitStatement_Assign(Statement_Assign statement_Assign,
			Object arg) throws Exception {

		if(statement_Assign.lhs==null || statement_Assign.e==null){
			throw new SemanticException(statement_Assign.firstToken, "Error: visitStatement_Assign: LHS or exp is null");
		}

		statement_Assign.lhs.visit(this, arg);
		statement_Assign.e.visit(this, arg);
		if(statement_Assign.lhs.getType() == statement_Assign.e.getType()){
			statement_Assign.setCartesian(statement_Assign.lhs.isCartesian());
		}
		return null;
	}

	@Override
	public Object visitIndex(Index index, Object arg) throws Exception {

		if(index.e0==null || index.e1==null)
			throw new SemanticException(index.firstToken, "Error:visitIndex: null expression");
		index.e0.visit(this, arg);
		index.e1.visit(this, arg);
		if(index.e0.getType()==Type.INTEGER && index.e1.getType()==Type.INTEGER){
			try{
				Expression_PredefinedName exp0= (Expression_PredefinedName)index.e0;
				Expression_PredefinedName exp1= (Expression_PredefinedName)index.e1;
				index.setCartesian(!(exp0.kind==Kind.KW_r&& exp1.kind==Kind.KW_a));
			}catch(Exception e){
				index.setCartesian(true);
			}
		}
		else{
			throw new SemanticException(index.firstToken, "Error: Expected index expression type as Integer");
		}

		return null;
	}

	@Override
	public Object visitLHS(LHS lhs, Object arg) throws Exception {

		Declaration dec = symTab.get(lhs.name);
		if(dec==null){
			throw new SemanticException(lhs.firstToken, "Error: visitLHS: no declaration found for the identifier");
		}

		lhs.setDec(dec);
		lhs.setType(lhs.getDec().getType());

		if(lhs.index!=null){
			lhs.index.visit(this, arg);
			lhs.setCartesian(lhs.index.isCartesian());
		}
		else{
			lhs.setCartesian(false);
		}
		return null;
	}

	@Override
	public Object visitSink_SCREEN(Sink_SCREEN sink_SCREEN, Object arg)
			throws Exception {

		sink_SCREEN.setType(Type.SCREEN);
		return null;
	}

	@Override
	public Object visitSink_Ident(Sink_Ident sink_Ident, Object arg)	//sink_Ident
			throws Exception {

		Declaration dec = symTab.get(sink_Ident.name);
		if(dec==null||dec.getType()!=Type.FILE)	{
			throw new SemanticException(sink_Ident.firstToken, "Error: visitSink_Ident: un-expected declaration");
		}
		sink_Ident.setType(dec.getType());
		return null;
	}

	@Override
	public Object visitSource_StringLiteral(
			Source_StringLiteral src_StrLit, Object arg)
					throws Exception {

		try{
			URL url = new URL(src_StrLit.fileOrUrl);
			src_StrLit.setType(Type.URL);
		}catch(Exception e){
			src_StrLit.setType(Type.FILE);
		}
		return null;
	}


	@Override
	public Object visitSource_CommandLineParam(
			Source_CommandLineParam src_CLP, Object arg)
					throws Exception {

		if(src_CLP.paramNum==null){
			throw new SemanticException(src_CLP.firstToken, "Error: visitSource_CommandLineParam: paramNum is null");
		}
		src_CLP.paramNum.visit(this, arg);
		if(src_CLP.paramNum.getType()!=Type.INTEGER){
			throw new SemanticException(src_CLP.firstToken, "Error: Expected integer parameter");
		}
		src_CLP.setType(src_CLP.paramNum.getType());
		//		}
		return null;
	}

	@Override
	public Object visitSource_Ident(Source_Ident src_Ident, Object arg)
			throws Exception {

		Declaration dec= symTab.get(src_Ident.name);
		if(dec!=null && (dec.getType()==Type.FILE||dec.getType()==Type.URL)){
			src_Ident.setType(dec.getType());
		}
		else{
			throw new SemanticException(src_Ident.firstToken, "Error: Expected declaration to be FILE or URL");
		}

		return null;
	}

	@Override
	public Object visitExpression_Binary(Expression_Binary exp_Binary,
			Object arg) throws Exception {

		if(exp_Binary.e0==null || exp_Binary.e1==null){
			throw new SemanticException(exp_Binary.firstToken, "Error: visitExpression_Binary: 1st or 2nd expression is null");
		}
		exp_Binary.e0.visit(this, arg);
		exp_Binary.e1.visit(this, arg);

		if(exp_Binary.e0.getType() != exp_Binary.e1.getType())
			throw new SemanticException(exp_Binary.firstToken, "Error: Expected same type for binary exp");

		if(exp_Binary.op == Kind.OP_EQ ||exp_Binary.op == Kind.OP_NEQ){
			exp_Binary.setType(Type.BOOLEAN);
		}
		else if((exp_Binary.op == Kind.OP_GE || exp_Binary.op == Kind.OP_GT || exp_Binary.op == Kind.OP_LT||exp_Binary.op == Kind.OP_LE ) && exp_Binary.e0.getType() == Type.INTEGER){
			exp_Binary.setType(Type.BOOLEAN);
		}
		else if((exp_Binary.op == Kind.OP_AND || exp_Binary.op == Kind.OP_OR) && (exp_Binary.e0.getType()==Type.BOOLEAN || exp_Binary.e0.getType()==Type.INTEGER)){
			exp_Binary.setType(exp_Binary.e0.getType());
		}
		else if((exp_Binary.op == Kind.OP_DIV ||exp_Binary.op == Kind.OP_MINUS || exp_Binary.op == Kind.OP_MOD ||exp_Binary.op == Kind.OP_PLUS ||exp_Binary.op == Kind.OP_POWER ||exp_Binary.op == Kind.OP_TIMES)&&(exp_Binary.e0.getType()==Type.INTEGER)){
			exp_Binary.setType(exp_Binary.e0.getType());
			if(exp_Binary.op == Kind.OP_DIV || exp_Binary.op == Kind.OP_MOD){
				Expression_IntLit inte;
				try{
					inte = (Expression_IntLit)exp_Binary.e1;
				}
				catch(Exception e){
					return null;
				}
				if(inte.value==0){
					throw new SemanticException(exp_Binary.firstToken, "Error: cannot divide by zero");
				}
			}
		}
		else{
			throw new SemanticException(exp_Binary.firstToken, "Error: invalid binary expression");
		}
		return null;

	}

	@Override
	public Object visitExpression_Unary(Expression_Unary exp_Unary,
			Object arg) throws Exception {
		if(exp_Unary.e==null)
			throw new SemanticException(exp_Unary.firstToken, "Error: visitExpression_Unary: expression null");

		exp_Unary.e.visit(this, arg);
		Type t = exp_Unary.e.getType();
		if(exp_Unary.op==Kind.OP_EXCL &&(t==Type.BOOLEAN || t==Type.INTEGER)){
			exp_Unary.setType(t);
		}
		else if((exp_Unary.op==Kind.OP_PLUS|| exp_Unary.op==Kind.OP_MINUS) && t==Type.INTEGER){
			exp_Unary.setType(Type.INTEGER);
		}
		else{
			throw new SemanticException(exp_Unary.firstToken, "Error: invalid unary expression");
		}
		return null;
	}

	@Override
	public Object visitExpression_PixelSelector(
			Expression_PixelSelector exp_PixelSelector, Object arg)
					throws Exception {

		Declaration dec= symTab.get(exp_PixelSelector.name);

		if(dec==null){
			throw new SemanticException(exp_PixelSelector.firstToken, "Error: variable declaration not found");
		}
		if(dec.getType()==Type.IMAGE){
			exp_PixelSelector.setType(Type.INTEGER);
			if(exp_PixelSelector.index!=null)
				exp_PixelSelector.index.visit(this, arg);
		}
		else if(exp_PixelSelector.index==null)
			exp_PixelSelector.setType(dec.getType());
		else
			throw new SemanticException(exp_PixelSelector.firstToken, "Error: invalid expression for pixel selector");
		return null;
	}

	@Override
	public Object visitExpression_Conditional(
			Expression_Conditional exp_Conditional, Object arg)
					throws Exception {

		if(exp_Conditional.condition==null || exp_Conditional.trueExpression==null|| exp_Conditional.falseExpression==null)
			throw new SemanticException(exp_Conditional.firstToken, "Error: visitExpression_Conditional: expression is null");

		exp_Conditional.condition.visit(this, arg);
		exp_Conditional.trueExpression.visit(this, arg);
		exp_Conditional.falseExpression.visit(this, arg);
		if(exp_Conditional.condition.getType()==Type.BOOLEAN && exp_Conditional.trueExpression.getType()== exp_Conditional.falseExpression.getType()){
			exp_Conditional.setType(exp_Conditional.trueExpression.getType());
		}
		else{
			throw new SemanticException(exp_Conditional.firstToken, "Error: invalid Expression_Conditional");
		}

		return null;
	}


	@Override
	public Object visitExpression_FunctionAppWithExprArg(
			Expression_FunctionAppWithExprArg exp_Arg,
			Object arg) throws Exception {

		exp_Arg.setType(Type.INTEGER);

		if(exp_Arg.arg==null){
			throw new SemanticException(exp_Arg.firstToken, "Error: Expression_FunctionAppWithExprArg: arg expression null");
		}

		exp_Arg.arg.visit(this, arg);
		if(exp_Arg.arg.getType() != Type.INTEGER){
			throw new SemanticException(exp_Arg.firstToken, "Error: Expression_FunctionAppWithExprArg: invalid arg type");
		}
		return null;
	}

	@Override
	public Object visitExpression_FunctionAppWithIndexArg(
			Expression_FunctionAppWithIndexArg exp_IdxArg,
			Object arg) throws Exception {

		exp_IdxArg.setType(Type.INTEGER);
		if(exp_IdxArg.arg!=null){
			exp_IdxArg.arg.visit(this, arg);
		}
		return null;
	}

	@Override
	public Object visitExpression_PredefinedName(
			Expression_PredefinedName exp_PredefName, Object arg)
					throws Exception {

		exp_PredefName.setType(Type.INTEGER);

		return null;
	}

	@Override
	public Object visitExpression_BooleanLit(
			Expression_BooleanLit expression_BooleanLit, Object arg)
					throws Exception {

		expression_BooleanLit.setType(Type.BOOLEAN);
		return null;
	}


	@Override
	public Object visitExpression_IntLit(Expression_IntLit expression_IntLit,
			Object arg) throws Exception {

		expression_IntLit.setType(Type.INTEGER);
		return null;
	}

	@Override
	public Object visitExpression_Ident(Expression_Ident expression_Ident,
			Object arg) throws Exception {

		try{
			expression_Ident.setType(symTab.get(expression_Ident.name).getType());
		}
		catch(Exception e){
			throw new SemanticException(expression_Ident.firstToken, e.getMessage());	//if any instance is null but accessed
		}
		return null;
	}

}
