package cop5556fa17;

import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import cop5556fa17.Scanner.Kind;
import cop5556fa17.TypeUtils.Type;
import cop5556fa17.AST.ASTNode;
import cop5556fa17.AST.ASTVisitor;
import cop5556fa17.AST.Declaration;
import cop5556fa17.AST.Declaration_Image;
import cop5556fa17.AST.Declaration_SourceSink;
import cop5556fa17.AST.Declaration_Variable;
import cop5556fa17.AST.Expression;
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
import cop5556fa17.AST.Sink_Ident;
import cop5556fa17.AST.Sink_SCREEN;
import cop5556fa17.AST.Source;
import cop5556fa17.AST.Source_CommandLineParam;
import cop5556fa17.AST.Source_Ident;
import cop5556fa17.AST.Source_StringLiteral;
import cop5556fa17.AST.Statement_In;
import cop5556fa17.AST.Statement_Out;
import cop5556fa17.AST.Statement_Assign;
import cop5556fa17.ImageFrame;
import cop5556fa17.ImageSupport;

public class CodeGenVisitor implements ASTVisitor, Opcodes {

	/**
	 * All methods and variable static.
	 */


	/**
	 * @param DEVEL
	 *            used as parameter to genPrint and genPrintTOS
	 * @param GRADE
	 *            used as parameter to genPrint and genPrintTOS
	 * @param sourceFileName
	 *            name of source file, may be null.
	 */
	public CodeGenVisitor(boolean DEVEL, boolean GRADE, String sourceFileName) {
		super();
		this.DEVEL = DEVEL;
		this.GRADE = GRADE;
		this.sourceFileName = sourceFileName;
	}

	ClassWriter cw;
	String className;
	String classDesc;
	String sourceFileName;

	MethodVisitor mv; // visitor of method currently under construction

	/** Indicates whether genPrint and genPrintTOS should generate code. */
	final boolean DEVEL;
	final boolean GRADE;


	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		className = program.name;
		classDesc = "L" + className + ";";
		String sourceFileName = (String) arg;
		cw.visit(52, ACC_PUBLIC + ACC_SUPER, className, null, "java/lang/Object", null);
		cw.visitSource(sourceFileName, null);
		// create main method
		mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);
		// initialize
		mv.visitCode();
		//add label before first instruction
		Label mainStart = new Label();
		mv.visitLabel(mainStart);

		mv.visitInsn(ICONST_0);
		mv.visitVarInsn(ISTORE, 1);
		mv.visitInsn(ICONST_0);
		mv.visitVarInsn(ISTORE, 2);
		mv.visitInsn(ICONST_0);
		mv.visitVarInsn(ISTORE, 3);
		mv.visitInsn(ICONST_0);
		mv.visitVarInsn(ISTORE, 4);
		mv.visitInsn(ICONST_0);
		mv.visitVarInsn(ISTORE, 5);
		mv.visitInsn(ICONST_0);
		mv.visitVarInsn(ISTORE, 6);
		mv.visitInsn(ICONST_0);
		mv.visitVarInsn(ISTORE, 7);
		mv.visitInsn(ICONST_0);
		mv.visitVarInsn(ISTORE, 8);

		ArrayList<ASTNode> decsAndStatements = program.decsAndStatements;
		for (ASTNode node : decsAndStatements) {
//			System.out.println("------------new node--------------");
			node.visit(this, arg);
		}

		//generates code to add string to log
//		CodeGenUtils.genLog(GRADE, mv, "leaving main");

		//adds the required (by the JVM) return statement to main
		mv.visitInsn(RETURN);

		//adds label at end of code
		Label mainEnd = new Label();
		mv.visitLabel(mainEnd);

		//handles parameters and local variables of main. Right now, only args
		mv.visitLocalVariable("args", "[Ljava/lang/String;", null, mainStart, mainEnd, 0);
		mv.visitLocalVariable("x", "I", null, mainStart, mainEnd, 1);
		mv.visitLocalVariable("y", "I", null, mainStart, mainEnd, 2);
		mv.visitLocalVariable("X", "I", null, mainStart, mainEnd, 3);
		mv.visitLocalVariable("Y", "I", null, mainStart, mainEnd, 4);
		mv.visitLocalVariable("r", "I", null, mainStart, mainEnd, 5);
		mv.visitLocalVariable("a", "I", null, mainStart, mainEnd, 6);
		mv.visitLocalVariable("R", "I", null, mainStart, mainEnd, 7);
		mv.visitLocalVariable("A", "I", null, mainStart, mainEnd, 8);
		mv.visitLocalVariable("Z", "I", null, mainStart, mainEnd, 9);
		mv.visitLocalVariable("tmp", "I", null, mainStart, mainEnd, 10);
		mv.visitLocalVariable("tmps", "Ljava/lang/String;", null, mainStart, mainEnd, 11);

		//Sets max stack size and number of local vars.
		//Because we use ClassWriter.COMPUTE_FRAMES as a parameter in the constructor,
		//asm will calculate this itself and the parameters are ignored.
		//If you have trouble with failures in this routine, it may be useful
		//to temporarily set the parameter in the ClassWriter constructor to 0.
		//The generated classfile will not be correct, but you will at least be
		//able to see what is in it.
		mv.visitMaxs(0, 0);

		//terminate construction of main method
		mv.visitEnd();

		//terminate class construction
		cw.visitEnd();

		//generate classfile as byte array and return
		return cw.toByteArray();
	}

	@Override
	public Object visitDeclaration_Variable(Declaration_Variable dec_Var, Object arg) throws Exception {

		cw.visitField(ACC_STATIC, dec_Var.name, dec_Var.getType().toString(), null, null).visitEnd();

		if(dec_Var.e!=null){
			dec_Var.e.visit(this, arg);
			mv.visitFieldInsn(PUTSTATIC, className, dec_Var.name, dec_Var.getType().toString());
		}

		return null;
	}



	public static final String ImageClassName = "java/awt/image/BufferedImage";
	public static final String ImageDesc = "Ljava/awt/image/BufferedImage;";

	@Override
	public Object visitDeclaration_Image(Declaration_Image dec_Img, Object arg) throws Exception {
		cw.visitField(ACC_STATIC, dec_Img.name, dec_Img.getType().toString(), null, null).visitEnd();
		if(dec_Img.xSize!=null && dec_Img.ySize!=null) {
			dec_Img.xSize.visit(this, arg);
			mv.visitVarInsn(ISTORE, 3);
			dec_Img.ySize.visit(this, arg);
			mv.visitVarInsn(ISTORE, 4);
			mv.visitVarInsn(ILOAD, 3);
			mv.visitVarInsn(ILOAD, 4);
			mv.visitMethodInsn(INVOKESTATIC, "cop5556fa17/ImageSupport", "makeImage", "(II)Ljava/awt/image/BufferedImage;", false);
			mv.visitFieldInsn(PUTSTATIC, className, dec_Img.name, dec_Img.getType().toString());
		}
		else if(dec_Img.xSize==null) {
			mv.visitLdcInsn(256);
			mv.visitVarInsn(ISTORE, 3);
			mv.visitLdcInsn(256);
			mv.visitVarInsn(ISTORE, 4);
			mv.visitVarInsn(ILOAD, 3);
			mv.visitVarInsn(ILOAD, 4);
			mv.visitMethodInsn(INVOKESTATIC, "cop5556fa17/ImageSupport", "makeImage", "(II)Ljava/awt/image/BufferedImage;", false);
			mv.visitFieldInsn(PUTSTATIC, className, dec_Img.name, dec_Img.getType().toString());
		}

		if(dec_Img.source!=null){
			dec_Img.source.visit(this, arg);
			if(dec_Img.xSize==null) {
				mv.visitInsn(ACONST_NULL);
				mv.visitInsn(ACONST_NULL);
				mv.visitMethodInsn(INVOKESTATIC, "cop5556fa17/ImageSupport", "readImage", "(Ljava/lang/String;Ljava/lang/Integer;Ljava/lang/Integer;)Ljava/awt/image/BufferedImage;", false);
				mv.visitFieldInsn(PUTSTATIC, className, dec_Img.name, "Ljava/awt/image/BufferedImage;");
			}
			else {
				mv.visitFieldInsn(GETSTATIC, className, dec_Img.name, "Ljava/awt/image/BufferedImage;");
				mv.visitMethodInsn(INVOKESTATIC, "cop5556fa17/ImageSupport", "getX", "(Ljava/awt/image/BufferedImage;)I", false);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);

				mv.visitFieldInsn(GETSTATIC, className, dec_Img.name, "Ljava/awt/image/BufferedImage;");
				mv.visitMethodInsn(INVOKESTATIC, "cop5556fa17/ImageSupport", "getY", "(Ljava/awt/image/BufferedImage;)I", false);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
				mv.visitMethodInsn(INVOKESTATIC, "cop5556fa17/ImageSupport", "readImage", "(Ljava/lang/String;Ljava/lang/Integer;Ljava/lang/Integer;)Ljava/awt/image/BufferedImage;", false);
				mv.visitFieldInsn(PUTSTATIC, className, dec_Img.name, "Ljava/awt/image/BufferedImage;");
			}
		}

		return null;
	}

	@Override
	public Object visitDeclaration_SourceSink(Declaration_SourceSink dec_SrcSink, Object arg)
			throws Exception {
		cw.visitField(ACC_STATIC, dec_SrcSink.name, dec_SrcSink.getType().toString(), null, null).visitEnd();		if(dec_SrcSink.source!=null){
			dec_SrcSink.source.visit(this, arg);
			mv.visitFieldInsn(PUTSTATIC, className, dec_SrcSink.name, dec_SrcSink.getType().toString());
		}
		return null;
	}


	/** For Integers and booleans, the only "sink"is the screen, so generate code to print to console.
	 * For Images, load the Image onto the stack and visit the Sink which will generate the code to handle the image.
	 */
	@Override
	public Object visitStatement_Out(Statement_Out statement_Out, Object arg) throws Exception {

		mv.visitFieldInsn(GETSTATIC, className, statement_Out.name, statement_Out.getDec().getType().toString());
		CodeGenUtils.genLogTOS(GRADE, mv, statement_Out.getDec().getType());
		if(statement_Out.getDec().getType()==Type.INTEGER || statement_Out.getDec().getType()==Type.BOOLEAN){
			CodeGenUtils.genPrintTOS(GRADE, mv, statement_Out.getDec().getType());
			mv.visitInsn(POP);
		}else {
			statement_Out.sink.visit(this, arg);
		}
		return null;
	}

	/**
	 * Visit source to load rhs, which will be a String, onto the stack
	 *
	 *  In HW5, you only need to handle INTEGER and BOOLEAN
	 *  Use java.lang.Integer.parseInt or java.lang.Boolean.parseBoolean
	 *  to convert String to actual type.
	 *
	 */

	public Object visitStatement_In(Statement_In statement_In, Object arg) throws Exception {
		statement_In.source.visit(this, arg);

		if(statement_In.getDec().getType()==Type.INTEGER){
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "parseInt", "(Ljava/lang/String;)I", false);
			mv.visitFieldInsn(PUTSTATIC, className, statement_In.name, "I");
		}
		else if(statement_In.getDec().getType()==Type.BOOLEAN){
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "parseBoolean", "(Ljava/lang/String;)Z", false);
			mv.visitFieldInsn(PUTSTATIC, className, statement_In.name, "Z");
		}
		else {
			Declaration_Image dec = (Declaration_Image) statement_In.getDec();
			if(dec.xSize==null) {
				mv.visitInsn(ACONST_NULL);
				mv.visitInsn(ACONST_NULL);
				mv.visitMethodInsn(INVOKESTATIC, "cop5556fa17/ImageSupport", "readImage", "(Ljava/lang/String;Ljava/lang/Integer;Ljava/lang/Integer;)Ljava/awt/image/BufferedImage;", false);
				mv.visitFieldInsn(PUTSTATIC, className, statement_In.name, "Ljava/awt/image/BufferedImage;");
			}
			else {
				mv.visitFieldInsn(GETSTATIC, className, statement_In.name, "Ljava/awt/image/BufferedImage;");
				mv.visitMethodInsn(INVOKESTATIC, "cop5556fa17/ImageSupport", "getX", "(Ljava/awt/image/BufferedImage;)I", false);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);

				mv.visitFieldInsn(GETSTATIC, className, statement_In.name, "Ljava/awt/image/BufferedImage;");
				mv.visitMethodInsn(INVOKESTATIC, "cop5556fa17/ImageSupport", "getY", "(Ljava/awt/image/BufferedImage;)I", false);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
				mv.visitMethodInsn(INVOKESTATIC, "cop5556fa17/ImageSupport", "readImage", "(Ljava/lang/String;Ljava/lang/Integer;Ljava/lang/Integer;)Ljava/awt/image/BufferedImage;", false);
				mv.visitFieldInsn(PUTSTATIC, className, statement_In.name, "Ljava/awt/image/BufferedImage;");
			}

		}

		return null;
	}


	/**
	 * In HW5, only handle INTEGER and BOOLEAN types.
	 */

	@Override
	public Object visitStatement_Assign(Statement_Assign statement_Assign, Object arg) throws Exception {

		if(ImageDesc.equals(statement_Assign.lhs.getType().toString())){


			mv.visitFieldInsn(GETSTATIC, className, statement_Assign.lhs.name, "Ljava/awt/image/BufferedImage;");
			mv.visitMethodInsn(INVOKESTATIC, "cop5556fa17/ImageSupport", "getX", "(Ljava/awt/image/BufferedImage;)I", false);
			mv.visitVarInsn(ISTORE, 3);

			mv.visitFieldInsn(GETSTATIC, className, statement_Assign.lhs.name, "Ljava/awt/image/BufferedImage;");
			mv.visitMethodInsn(INVOKESTATIC, "cop5556fa17/ImageSupport", "getY", "(Ljava/awt/image/BufferedImage;)I", false);
			mv.visitVarInsn(ISTORE, 4);
			mv.visitInsn(ICONST_0);
			mv.visitVarInsn(ISTORE, 2);
			Label l6 = new Label();
			mv.visitJumpInsn(GOTO, l6);

			Label l7 = new Label();
			mv.visitLabel(l7);
			mv.visitInsn(ICONST_0);
			mv.visitVarInsn(ISTORE, 1);
			Label l9 = new Label();
			mv.visitJumpInsn(GOTO, l9);
			Label l10 = new Label();
			mv.visitLabel(l10);

			statement_Assign.e.visit(this, arg);
			statement_Assign.lhs.visit(this, arg);

			mv.visitIincInsn(1, 1);
			mv.visitLabel(l9);
			mv.visitVarInsn(ILOAD, 1);
			mv.visitVarInsn(ILOAD, 3);
			mv.visitJumpInsn(IF_ICMPLT, l10);

			mv.visitIincInsn(2, 1);
			mv.visitLabel(l6);
			mv.visitVarInsn(ILOAD, 2);
			mv.visitVarInsn(ILOAD, 4);
			mv.visitJumpInsn(IF_ICMPLT, l7);


		}else{
			statement_Assign.e.visit(this, arg);
			statement_Assign.lhs.visit(this, arg);
		}

		return null;
	}

	@Override
	public Object visitIndex(Index index, Object arg) throws Exception {

		if(index.isCartesian()) {
			index.e0.visit(this, arg);
			index.e1.visit(this, arg);
		}else {
			index.e0.visit(this, arg);
			index.e1.visit(this, arg);
			mv.visitMethodInsn(INVOKESTATIC, "cop5556fa17/RuntimeFunctions", "cart_x", "(II)I", false);
			index.e0.visit(this, arg);
			index.e1.visit(this, arg);
			mv.visitMethodInsn(INVOKESTATIC, "cop5556fa17/RuntimeFunctions", "cart_y", "(II)I", false);
		}
		return null;
	}


	/**
	 * In HW5, only handle INTEGER and BOOLEAN types.
	 */

	@Override
	public Object visitLHS(LHS lhs, Object arg) throws Exception {

		if(lhs.getType() == Type.INTEGER || lhs.getType() == Type.BOOLEAN){
			mv.visitFieldInsn(PUTSTATIC, className, lhs.name, lhs.getType().toString());
		}
		else {
			if(lhs.index!=null) {
				try {
					Expression_PredefinedName exp0= (Expression_PredefinedName)lhs.index.e0;
					Expression_PredefinedName exp1= (Expression_PredefinedName)lhs.index.e1;
					if(exp0.kind==Kind.KW_r && exp1.kind==Kind.KW_a) {
						mv.visitFieldInsn(GETSTATIC, className, lhs.name, "Ljava/awt/image/BufferedImage;");
						mv.visitVarInsn(ILOAD, 1);	//x
						mv.visitVarInsn(ILOAD, 2);	//y
						mv.visitMethodInsn(INVOKESTATIC, "cop5556fa17/ImageSupport", "setPixel", "(ILjava/awt/image/BufferedImage;II)V", false);
					}
					else if(exp0.kind==Kind.KW_x && exp1.kind==Kind.KW_y) {
						mv.visitFieldInsn(GETSTATIC, className, lhs.name, "Ljava/awt/image/BufferedImage;");
						mv.visitVarInsn(ILOAD, 1);
						mv.visitVarInsn(ILOAD, 2);
						mv.visitMethodInsn(INVOKESTATIC, "cop5556fa17/ImageSupport", "setPixel", "(ILjava/awt/image/BufferedImage;II)V", false);
					}
				}catch(Exception e) {
					mv.visitFieldInsn(GETSTATIC, className, lhs.name, "Ljava/awt/image/BufferedImage;");
					lhs.index.visit(this, arg);
					mv.visitMethodInsn(INVOKESTATIC, "cop5556fa17/ImageSupport", "setPixel", "(ILjava/awt/image/BufferedImage;II)V", false);
				}
			}
		}


		return null;
	}


	@Override
	public Object visitExpression_Binary(Expression_Binary exp_Binary, Object arg) throws Exception{
		if(exp_Binary.op == Kind.OP_EQ ||exp_Binary.op == Kind.OP_NEQ||exp_Binary.op == Kind.OP_GE || exp_Binary.op == Kind.OP_GT || exp_Binary.op == Kind.OP_LT||exp_Binary.op == Kind.OP_LE ){
			exp_Binary.e0.visit(this,arg);
			exp_Binary.e1.visit(this,arg);
			Label le1 = new Label();
			if(exp_Binary.op == Kind.OP_EQ)
				mv.visitJumpInsn(IF_ICMPNE, le1);
			else if(exp_Binary.op == Kind.OP_NEQ)
				mv.visitJumpInsn(IF_ICMPEQ, le1);
			else if(exp_Binary.op == Kind.OP_GE)
				mv.visitJumpInsn(IF_ICMPLT, le1);
			else if(exp_Binary.op == Kind.OP_GT)
				mv.visitJumpInsn(IF_ICMPLE, le1);
			else if(exp_Binary.op == Kind.OP_LT)
				mv.visitJumpInsn(IF_ICMPGE, le1);
			else if(exp_Binary.op == Kind.OP_LE)
				mv.visitJumpInsn(IF_ICMPGT, le1);

			mv.visitInsn(ICONST_1);
			Label le2 = new Label();
			mv.visitJumpInsn(GOTO, le2);
			mv.visitLabel(le1);
			mv.visitInsn(ICONST_0);
			mv.visitLabel(le2);
		}
		else if(exp_Binary.op == Kind.OP_AND){
			exp_Binary.e0.visit(this,arg);
			exp_Binary.e1.visit(this,arg);
			mv.visitInsn(IAND);
		}
		else if(exp_Binary.op == Kind.OP_OR){
			exp_Binary.e0.visit(this,arg);
			exp_Binary.e1.visit(this,arg);
			mv.visitInsn(IOR);
		}
		else if(exp_Binary.op == Kind.OP_DIV ||exp_Binary.op == Kind.OP_MINUS || exp_Binary.op == Kind.OP_MOD ||exp_Binary.op == Kind.OP_PLUS ||exp_Binary.op == Kind.OP_POWER ||exp_Binary.op == Kind.OP_TIMES){
			exp_Binary.e0.visit(this,arg);
			exp_Binary.e1.visit(this,arg);
			if(exp_Binary.op == Kind.OP_PLUS)
				mv.visitInsn(IADD);
			else if(exp_Binary.op == Kind.OP_MINUS)
				mv.visitInsn(ISUB);
			else if(exp_Binary.op == Kind.OP_TIMES)
				mv.visitInsn(IMUL);
			else if(exp_Binary.op == Kind.OP_DIV)
				mv.visitInsn(IDIV);
			else if(exp_Binary.op == Kind.OP_MOD)
				mv.visitInsn(IREM);
		}

		return null;
	}

	@Override
	public Object visitExpression_Unary(Expression_Unary exp_Unary, Object arg) throws Exception {

		exp_Unary.e.visit(this, arg);
		if(exp_Unary.op==Kind.OP_EXCL)
		{
			if(exp_Unary.getType()==Type.BOOLEAN){
				Label l1 = new Label();
				mv.visitJumpInsn(IFEQ, l1);
				mv.visitInsn(ICONST_0);
				Label l2 = new Label();
				mv.visitJumpInsn(GOTO, l2);
				mv.visitLabel(l1);
				mv.visitInsn(ICONST_1);
				mv.visitLabel(l2);
			}
			else if(exp_Unary.getType() == Type.INTEGER){
				mv.visitLdcInsn(Integer.MAX_VALUE);
				mv.visitInsn(IXOR);
			}
		}
		else if(exp_Unary.op==Kind.OP_MINUS){
			mv.visitInsn(INEG);
		}
		return null;
	}


	@Override
	public Object visitExpression_PixelSelector(Expression_PixelSelector exp_PixelSelector, Object arg)
			throws Exception {

		mv.visitFieldInsn(GETSTATIC, className, exp_PixelSelector.name, "Ljava/awt/image/BufferedImage;");
		exp_PixelSelector.index.visit(this, arg);
		mv.visitMethodInsn(INVOKESTATIC, "cop5556fa17/ImageSupport", "getPixel", "(Ljava/awt/image/BufferedImage;II)I", false);
		return null;
	}

	@Override
	public Object visitExpression_Conditional(Expression_Conditional exp_Conditional, Object arg)
			throws Exception {

		exp_Conditional.condition.visit(this, arg);
		Label if_false = new Label();
		Label if_true = new Label();

		mv.visitJumpInsn(IFEQ, if_false);
		exp_Conditional.trueExpression.visit(this, arg);
		mv.visitJumpInsn(GOTO, if_true);

		mv.visitLabel(if_false);
		exp_Conditional.falseExpression.visit(this, arg);

		mv.visitLabel(if_true);
		return null;
	}

	@Override
	public Object visitExpression_FunctionAppWithExprArg(
			Expression_FunctionAppWithExprArg exp_Arg, Object arg) throws Exception {
		exp_Arg.arg.visit(this, arg);
		if(exp_Arg.function==Kind.KW_abs) {
			mv.visitMethodInsn(INVOKESTATIC, "cop5556fa17/RuntimeFunctions", "abs", "(I)I", false);
		}
		else if(exp_Arg.function==Kind.KW_log) {
			mv.visitMethodInsn(INVOKESTATIC, "cop5556fa17/RuntimeFunctions", "log", "(I)I", false);
		}

		return null;
	}

	@Override
	public Object visitExpression_FunctionAppWithIndexArg(
			Expression_FunctionAppWithIndexArg exp_IdxArg, Object arg) throws Exception {

		if(exp_IdxArg.arg!=null) {
			exp_IdxArg.arg.e0.visit(this, arg);
			exp_IdxArg.arg.e1.visit(this, arg);

			if(exp_IdxArg.function==Kind.KW_cart_x) {
				mv.visitMethodInsn(INVOKESTATIC, "cop5556fa17/RuntimeFunctions", "cart_x", "(II)I", false);
			}
			else if(exp_IdxArg.function==Kind.KW_cart_y) {
				mv.visitMethodInsn(INVOKESTATIC, "cop5556fa17/RuntimeFunctions", "cart_y", "(II)I", false);
			}
			else if(exp_IdxArg.function==Kind.KW_polar_r) {
				mv.visitMethodInsn(INVOKESTATIC, "cop5556fa17/RuntimeFunctions", "polar_r", "(II)I", false);
			}
			else if(exp_IdxArg.function==Kind.KW_polar_a) {
				mv.visitMethodInsn(INVOKESTATIC, "cop5556fa17/RuntimeFunctions", "polar_a", "(II)I", false);
			}
		}
		return null;
	}

	@Override
	public Object visitExpression_PredefinedName(Expression_PredefinedName exp_PredefName, Object arg)
			throws Exception {

		int slot_no = 1;
		if(exp_PredefName.kind==Kind.KW_x) {
			mv.visitVarInsn(ILOAD, 1);
		}
		else if(exp_PredefName.kind==Kind.KW_y){
			mv.visitVarInsn(ILOAD, 2);
		}
		else if(exp_PredefName.kind==Kind.KW_X){
			mv.visitVarInsn(ILOAD, 3);
		}
		else if(exp_PredefName.kind==Kind.KW_Y){
			mv.visitVarInsn(ILOAD, 4);
		}

		else if(exp_PredefName.kind==Kind.KW_r){
			mv.visitVarInsn(ILOAD, 1);
			mv.visitVarInsn(ILOAD, 2);
			mv.visitMethodInsn(INVOKESTATIC, "cop5556fa17/RuntimeFunctions", "polar_r", "(II)I", false);
			mv.visitVarInsn(ISTORE, 5);
			mv.visitVarInsn(ILOAD, 5);
		}

		else if(exp_PredefName.kind==Kind.KW_a){
			mv.visitVarInsn(ILOAD, 1);
			mv.visitVarInsn(ILOAD, 2);
			mv.visitMethodInsn(INVOKESTATIC, "cop5556fa17/RuntimeFunctions", "polar_a", "(II)I", false);
			mv.visitVarInsn(ISTORE, 6);
			mv.visitVarInsn(ILOAD, 6);
		}

		else if(exp_PredefName.kind==Kind.KW_R){
			mv.visitVarInsn(ILOAD, 3);
			mv.visitVarInsn(ILOAD, 4);
			mv.visitMethodInsn(INVOKESTATIC, "cop5556fa17/RuntimeFunctions", "polar_r", "(II)I", false);
			mv.visitVarInsn(ISTORE, 7);
			mv.visitVarInsn(ILOAD, 7);
		}

		else if(exp_PredefName.kind==Kind.KW_A){
			mv.visitInsn(ICONST_0);
			mv.visitVarInsn(ILOAD, 4);
			mv.visitMethodInsn(INVOKESTATIC, "cop5556fa17/RuntimeFunctions", "polar_a", "(II)I", false);
			mv.visitVarInsn(ISTORE, 8);
			mv.visitVarInsn(ILOAD, 8);
		}
		else if(exp_PredefName.kind==Kind.KW_DEF_X){
			mv.visitLdcInsn(new Integer(256));
		}
		else if(exp_PredefName.kind==Kind.KW_DEF_Y){
			mv.visitLdcInsn(new Integer(256));
		}
		else if(exp_PredefName.kind==Kind.KW_Z){
			mv.visitLdcInsn(new Integer(16777215));
		}
		return null;
	}

	@Override
	public Object visitExpression_BooleanLit(Expression_BooleanLit expression_BooleanLit, Object arg) throws Exception {

		if (expression_BooleanLit.value == true) {
			mv.visitInsn(ICONST_1);
		} else {
			mv.visitInsn(ICONST_0);
		}
		return null;
	}

	@Override
	public Object visitExpression_IntLit(Expression_IntLit expression_IntLit, Object arg) throws Exception {
		mv.visitLdcInsn(expression_IntLit.value);
		return null;
	}

	@Override
	public Object visitExpression_Ident(Expression_Ident exp_Ident,
			Object arg) throws Exception {
		mv.visitFieldInsn(GETSTATIC, className, exp_Ident.name, exp_Ident.getType().toString());
		return null;
	}


	@Override
	public Object visitSink_SCREEN(Sink_SCREEN sink_SCREEN, Object arg) throws Exception {
		mv.visitMethodInsn(INVOKESTATIC, "cop5556fa17/ImageFrame", "makeFrame", "(Ljava/awt/image/BufferedImage;)Ljavax/swing/JFrame;", false);
		mv.visitInsn(POP);
		return null;
	}

	@Override
	public Object visitSink_Ident(Sink_Ident sink_Ident, Object arg) throws Exception {

		mv.visitFieldInsn(GETSTATIC, className, sink_Ident.name, sink_Ident.getType().toString());
		mv.visitMethodInsn(INVOKESTATIC, "cop5556fa17/ImageSupport", "write", "(Ljava/awt/image/BufferedImage;Ljava/lang/String;)V", false);
		return null;
	}

	@Override
	public Object visitSource_StringLiteral(Source_StringLiteral src_StrLit, Object arg) throws Exception {
		mv.visitLdcInsn(new String(src_StrLit.fileOrUrl));
		return null;
	}

	@Override
	public Object visitSource_CommandLineParam(Source_CommandLineParam src_CLP, Object arg)
			throws Exception {
		mv.visitVarInsn(ALOAD,0);
		src_CLP.paramNum.visit(this, arg);
		mv.visitInsn(AALOAD);
		return null;
	}
	@Override
	public Object visitSource_Ident(Source_Ident source_Ident, Object arg) throws Exception {
		mv.visitFieldInsn(GETSTATIC, className, source_Ident.name, "Ljava/lang/String;");

		return null;
	}
}
