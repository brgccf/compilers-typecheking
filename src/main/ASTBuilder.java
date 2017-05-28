package main;
import java.util.List;

import org.antlr.v4.runtime.tree.TerminalNode;

import ast.*;
import main.brgccf_lfp2Parser.ClassDeclarationContext;
import main.brgccf_lfp2Parser.ExpressionContext;
import main.brgccf_lfp2Parser.GoalContext;
import main.brgccf_lfp2Parser.MainClassContext;
import main.brgccf_lfp2Parser.MethodDeclarationContext;
import main.brgccf_lfp2Parser.StatementContext;
import main.brgccf_lfp2Parser.TypeContext;
import main.brgccf_lfp2Parser.VarDeclarationContext;
import main.brgccf_lfp2BaseVisitor;

public class ASTBuilder extends brgccf_lfp2BaseVisitor<Object>{
	public ASTBuilder(){}
	
	public Program visitGoal(GoalContext ctx)
	{
		MainClass main = this.visitMain(ctx.mainClass());
		ClassDeclList classList = this.visitClassDeclList(ctx.classDeclaration());
		return new Program(main, classList);
		
	}
	

	private ClassDeclList visitClassDeclList(List<ClassDeclarationContext> ctx) {
		ClassDeclList classDecList = new ClassDeclList();
		for (ClassDeclarationContext c : ctx){
			if(c.Identifier().size() > 1) classDecList.addElement(this.visiClassDeclExtends(c));
			else {
				classDecList.addElement(this.visitClassDecl(c));
			}
		}
		
		return classDecList;
	}
	
	private ClassDecl visiClassDeclExtends(ClassDeclarationContext ctx) {
		Identifier id1 = this.visitIdentifier(ctx.Identifier(0));
		Identifier id2 = this.visitIdentifier(ctx.Identifier(1));
		VarDeclList varDeclList = this.visitVarDeclList(ctx.varDeclaration());
		MethodDeclList methodDeclList = this.visitMethodDeclList(ctx.methodDeclaration());
		return new ClassDeclExtends(id1, id2, varDeclList, methodDeclList);
	}

	private ClassDecl visitClassDecl(ClassDeclarationContext ctx) {
		Identifier id = this.visitIdentifier(ctx.Identifier(0));
		VarDeclList varDeclList = this.visitVarDeclList(ctx.varDeclaration());
		MethodDeclList methodDeclList = this.visitMethodDeclList(ctx.methodDeclaration());
		return new ClassDeclSimple(id, varDeclList, methodDeclList);
	}

	private MethodDeclList visitMethodDeclList(List<MethodDeclarationContext> ctx) {
		MethodDeclList methodDeclList = new MethodDeclList();
		for (MethodDeclarationContext c : ctx) {
			methodDeclList.addElement(this.visitMethodDecl(c));
		}
		return methodDeclList;
	}
	
	private MethodDecl visitMethodDecl(MethodDeclarationContext ctx) {
		FormalList args = new FormalList();
		List<TypeContext> types = ctx.type();
		List<TerminalNode> tokens = ctx.Identifier();
		Type type = this.visitType(types.get(0));
		Identifier name = new Identifier(tokens.get(0).getText());
		
		for(int i = 1; i< types.size();i++){
			args.addElement(new Formal(this.visitType(types.get(i)),new Identifier(tokens.get(i).getText())));
		}
		VarDeclList vars = this.visitVarDeclList(ctx.varDeclaration());
		for(int i = 0; i < ctx.varDeclaration().size(); i++)
		{
			vars.addElement(this.visitVarDecl(ctx.varDeclaration().get(i)));
		}
		StatementList stmts = this.visitStatementList(ctx.statement());
		for(int i = 0; i < ctx.varDeclaration().size(); i++)
		{
			stmts.addElement(this.visitStatement(ctx.statement().get(i)));
		}
		Exp exp = this.visitExp(ctx.expression());
		return new MethodDecl(type,name,args,vars,stmts,exp);
		
	}
	

	private Exp visitExp(ExpressionContext ctx) {
		TerminalNode op = ctx.OP();
		TerminalNode ids = ctx.Identifier();
		TerminalNode literal = ctx.INTEGER_LITERAL();
		String text = ctx.getText();
		if(op != null){
			Exp exp1 = this.visitExp(ctx.expression().get(0));
			Exp exp2 = this.visitExp(ctx.expression().get(1));
			switch(op.getText()){
			case "&&": return new And(exp1,exp2);
			case "<" : return new LessThan(exp1,exp2);
			case "+" : return new Plus(exp1,exp2);
			case "-" : return new Minus(exp1,exp2);
			case "*" : return new Times(exp1,exp2);
			}
		
		}else if(literal != null){
			return new IntegerLiteral(Integer.parseInt(literal.getText()));
		}
		else if(text.startsWith("!")){
			return new Not(this.visitExp(ctx.expression().get(0)));
		}else if(ctx.expression().size() >= 1 && ids != null){
			return new Call(this.visitExp(ctx.expression().get(0)),
					new Identifier(ids.getText()), this.visitExpList(ctx.expression().subList(1, ctx.expression().size())));
		} else if (ctx.getText().startsWith("true")){
			return new True();
		}else if(ctx.getText().startsWith("false")){
			return new False();
		}else if(ctx.getText().startsWith("this")){
			return new This();
		}else if(ctx.expression().size() == 2){
			return new ArrayLookup(this.visitExp(ctx.expression().get(0)),this.visitExp(ctx.expression().get(1)));
		}else if(ctx.expression().size() == 1 && !text.startsWith("new") && text.contains("length")){
			return new ArrayLength(this.visitExp(ctx.expression().get(0)));
		}else if(ids != null && !text.contains("new")){
			return new IdentifierExp(ids.getText());
		}else if(text.contains("new")){
			if(ctx.expression().size() == 1){
				return new NewArray(this.visitExp(ctx.expression().get(0)));
			}else{
				return new NewObject(new Identifier(ids.getText()));
			}
		}
		return this.visitExp(ctx.expression().get(0));	
	}

	private ExpList visitExpList(List<ExpressionContext> ctx) {
		ExpList expList = new ExpList();
		for (ExpressionContext c : ctx) {
			expList.addElement(this.visitExp(c));
		}
		return expList;
	}

	private StatementList visitStatementList(List<StatementContext> ctx) {
		StatementList statementList = new StatementList();
		for (StatementContext c : ctx) {
			statementList.addElement(this.visitStatement(c));
		}
		return statementList;
	}

	public Statement visitStatement(StatementContext ctx) {
		TerminalNode tokens = ctx.Identifier();
		if(ctx.getChild(0).getText().equals("if")){
			return new If(this.visitExp(ctx.expression(0)),this.visitStatement(ctx.statement(0)),this.visitStatement(ctx.statement(1)));
		}else if(ctx.getChild(0).getText().equals("while")){
			return new While(this.visitExp(ctx.expression(0)),this.visitStatement(ctx.statement(0)));
		}else if(ctx.Identifier() != null && ctx.expression().size() == 2){
			return new ArrayAssign(new Identifier(ctx.Identifier().getText()), 
					this.visitExp(ctx.expression(0)),this.visitExp(ctx.expression(1)));
		}else if(ctx.Identifier() != null && ctx.expression().size() == 1){
			return new Assign(new Identifier(tokens.getText()), this.visitExp(ctx.expression(0)));
		}else if(ctx.getChild(0).getText().equals("System.out.println")){
			return new Print(this.visitExp(ctx.expression(0)));
		}else {
			return new Block(this.visitStatementList(ctx.statement()));
		}
	}
	


	public Type visitType(TypeContext ctx) {
		if(ctx.Identifier() != null){
			return new IdentifierType(ctx.Identifier().getText());
		}
		if(ctx.getText().equals("boolean")){
			return new BooleanType();
		}else if(ctx.getText().equals("int []") || ctx.getText().equals("int[]")){
			return new IntArrayType();
		}else if(ctx.getText().equals("int")){
			return new IntegerType();
		} 
		return null;
	}

	private VarDeclList visitVarDeclList(List<VarDeclarationContext> ctx) {
		VarDeclList varDeclList = new VarDeclList();
		for (VarDeclarationContext c : ctx) {
			varDeclList.addElement(this.visitVarDecl(c));
		}
		return varDeclList;
	}

	private VarDecl visitVarDecl(VarDeclarationContext ctx) {
		return new VarDecl(this.visitType(ctx.type()), this.visitIdentifier(ctx.Identifier()));
	}

	private MainClass visitMain(MainClassContext ctx) {
		return new MainClass(this.visitIdentifier(ctx.Identifier(0)), 
				this.visitIdentifier(ctx.Identifier(1)), this.visitStatement(ctx.statement()));
	}
	
	private Identifier visitIdentifier(TerminalNode ctx) {
		return new Identifier(ctx.toString());
	}
	




	
	
	
	
}
