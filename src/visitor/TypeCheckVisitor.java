package visitor;

import symboltable.Method;
import symboltable.Class;
import symboltable.SymbolTable;


import ast.And;
import ast.ArrayAssign;
import ast.ArrayLength;
import ast.ArrayLookup;
import ast.Assign;
import ast.Block;
import ast.BooleanType;
import ast.Call;
import ast.ClassDeclExtends;
import ast.ClassDeclSimple;
import ast.False;
import ast.Formal;
import ast.Identifier;
import ast.IdentifierExp;
import ast.IdentifierType;
import ast.If;
import ast.IntArrayType;
import ast.IntegerLiteral;
import ast.IntegerType;
import ast.LessThan;
import ast.MainClass;
import ast.MethodDecl;
import ast.Minus;
import ast.NewArray;
import ast.NewObject;
import ast.Not;
import ast.Plus;
import ast.Print;
import ast.Program;
import ast.This;
import ast.Times;
import ast.True;
import ast.Type;
import ast.VarDecl;
import ast.While;

public class TypeCheckVisitor implements TypeVisitor {

	private SymbolTable symbolTable;
	private Method currMethod;
	private Class currClass;
	public TypeCheckVisitor(SymbolTable st) {
		symbolTable = st;
	}

	// MainClass m;
	// ClassDeclList cl;
	public Type visit(Program n) {
		n.m.accept(this);
		for (int i = 0; i < n.cl.size(); i++) {
			n.cl.elementAt(i).accept(this);
		}
		return null;
	}

	// Identifier i1,i2;
	// Statement s;
	public Type visit(MainClass n) {
		this.currClass = symbolTable.getClass(n.i1.s);
		this.currMethod = symbolTable.getMethod("main", currClass.getId());
		//n.i1.accept(this);
		//n.i2.accept(this);
		n.s.accept(this);
		this.currClass = null;
		return null;
	}

	// Identifier i;
	// VarDeclList vl;
	// MethodDeclList ml;
	public Type visit(ClassDeclSimple n) {
		//n.i.accept(this);
		this.currClass = symbolTable.getClass(n.i.s);
		for (int i = 0; i < n.vl.size(); i++) {
			n.vl.elementAt(i).accept(this);
		}
		for (int i = 0; i < n.ml.size(); i++) {
			n.ml.elementAt(i).accept(this);
		}
		this.currClass = null;
		return null;
	}

	// Identifier i;
	// Identifier j;
	// VarDeclList vl;
	// MethodDeclList ml;
	public Type visit(ClassDeclExtends n) {
		this.currClass = symbolTable.getClass(n.i.s);
		n.i.accept(this);
		n.j.accept(this);
		for (int i = 0; i < n.vl.size(); i++) {
			n.vl.elementAt(i).accept(this);
		}
		for (int i = 0; i < n.ml.size(); i++) {
			n.ml.elementAt(i).accept(this);
		}
		this.currClass = null;
		return null;
	}

	// Type t;
	// Identifier i;
	public Type visit(VarDecl n) {
		n.t.accept(this);
		n.i.accept(this);
		return n.t;
	}

	// Type t;
	// Identifier i;
	// FormalList fl;
	// VarDeclList vl;
	// StatementList sl;
	// Exp e;
	public Type visit(MethodDecl n) {
		this.currMethod = this.symbolTable.getMethod(n.i.s, this.currClass.getId());
		
		n.t.accept(this);
		//n.i.accept(this);
		Type t = symbolTable.getMethodType(n.i.s, this.currClass.getId());
		for (int i = 0; i < n.fl.size(); i++) {
			n.fl.elementAt(i).accept(this);
		}
		for (int i = 0; i < n.vl.size(); i++) {
			n.vl.elementAt(i).accept(this);
		}
		for (int i = 0; i < n.sl.size(); i++) {
			n.sl.elementAt(i).accept(this);
		}
		n.e.accept(this);
		return t;
	}

	// Type t;
	// Identifier i;
	public Type visit(Formal n) {
		n.t.accept(this);
		n.i.accept(this);
		return null;
	}

	public Type visit(IntArrayType n) {
		return new IntArrayType();
	}

	public Type visit(BooleanType n) {
		return new BooleanType();
	}

	public Type visit(IntegerType n) {
		return new IntegerType();
	}

	// String s;
	public Type visit(IdentifierType n) {
		return n;
	}

	// StatementList sl;
	public Type visit(Block n) {
		for (int i = 0; i < n.sl.size(); i++) {
			n.sl.elementAt(i).accept(this);
		}
		return null;
	}

	// Exp e;
	// Statement s1,s2;
	public Type visit(If n) {
		Type t = n.e.accept(this);
		boolean result = this.symbolTable.compareTypes(t, new BooleanType());
		if(!result)
		{
			System.out.println("No boolean expression inside if");
		}
		n.s1.accept(this);
		n.s2.accept(this);
		return null;
	}

	// Exp e;
	// Statement s;
	public Type visit(While n) {
		Type t = n.e.accept(this);
		boolean result = this.symbolTable.compareTypes(t, new BooleanType());
		if(!result)
		{
			System.out.println("No boolean expression inside While");
		}
		n.s.accept(this);
		return null;
	}

	// Exp e;
	public Type visit(Print n) {
		n.e.accept(this);
		return null;
	}

	// Identifier i;
	// Exp e;
	public Type visit(Assign n) {
		Type t1 = n.i.accept(this);
		Type t2 = n.e.accept(this);
		
		if(!symbolTable.compareTypes(symbolTable.getVarType(currMethod, currClass, n.i.s), t2)) {
			System.out.println("Assign Erro! " + n.i.s + " não é do mesmo tipo do valor atribuído");
		}
		
		return null;
	}

	// Identifier i;
	// Exp e1,e2;
	public Type visit(ArrayAssign n) {
		n.i.accept(this);
		n.e1.accept(this);
		n.e2.accept(this);
		return null;
	}

	// Exp e1,e2;
	public Type visit(And n) {
		Type t1 = n.e1.accept(this);
		Type t2 = n.e2.accept(this);
		boolean equal = this.symbolTable.compareTypes(t1, new BooleanType());
		equal = true && this.symbolTable.compareTypes(new BooleanType(), t2);
		if(!equal)
		{
			System.out.println("There was a type error on Expression And " + n.e1.toString() + " " + n.e2.toString());
		}
		return new BooleanType();
	}

	// Exp e1,e2;
	public Type visit(LessThan n) {
		//n.e1.accept(this);
		//n.e2.accept(this);
		Type t1 = n.e1.accept(this);
		Type t2 = n.e2.accept(this);
		boolean equal = this.symbolTable.compareTypes(t1, new IntegerType());
		equal = true && this.symbolTable.compareTypes(new IntegerType(), t2);
		if(!equal)
		{
			System.out.println("There was a Type error in Expression " + n.e1.toString() + " " + n.e2.toString());
		}
		
		return new BooleanType();
	}

	// Exp e1,e2;
	public Type visit(Plus n) {
		Type t1 = n.e1.accept(this);
		Type t2 = n.e2.accept(this);
		boolean equal = this.symbolTable.compareTypes(t1, new IntegerType());
		equal = true && this.symbolTable.compareTypes(new IntegerType(), t2);
		if(!equal)
		{
			System.out.println("There was a type error in expression " + n.e1.toString() + " " + n.e2.toString());
		}
		return new IntegerType();
	}

	// Exp e1,e2;
	public Type visit(Minus n) {
		Type t1 = n.e1.accept(this);
		Type t2 = n.e2.accept(this);
		boolean equal = this.symbolTable.compareTypes(t1, new IntegerType());
		equal = true && this.symbolTable.compareTypes(new IntegerType(), t2);
		if(!equal)
		{
			System.out.println("There was a type error in expression " + n.e1.toString() + " " + n.e2.toString());
		}
		return new IntegerType();
	}

	// Exp e1,e2;
	public Type visit(Times n) {
		Type t1 = n.e1.accept(this);
		Type t2 = n.e2.accept(this);
		if(!(t1 instanceof IntegerType)) {
			System.out.println("Times Erro! Lado esquerdo da expressão não é inteiro");
		}
		if(!(t2 instanceof IntegerType)) {
			System.out.println("Times Erro! Lado direito da expressão não é inteiro");
		}
		return new IntegerType();
	}

	// Exp e1,e2;
	public Type visit(ArrayLookup n) {
		Type t = n.e1.accept(this);
		Type t2 = n.e2.accept(this);
		
		if(!(t instanceof IntArrayType)) {
			System.out.println("Array lookup Erro! Não é um array");
		}
		
		if(!(t2 instanceof IntegerType)) {
			System.out.println("Array lookup Erro! Indice não é um numeral");
		}
		
		return new IntegerType();
	}

	// Exp e;
	public Type visit(ArrayLength n) {
		Type t = n.e.accept(this);
		if((t instanceof IntArrayType) == false){
			System.out.println("O tipo do tamanho do array nao e integer");
		}
		return new IntegerType();
	}

	// Exp e;
	// Identifier i;
	// ExpList el;
	public Type visit(Call n) {		
		Type ttemp = n.e.accept(this);
		Class ctemp;
		Method mtemp = null;
		if(ttemp instanceof IdentifierType){
			ctemp = symbolTable.getClass(((IdentifierType) ttemp).s);
			if(ctemp == null){
				System.out.println("A Classe" + ctemp.getId() + "nao existe");
			}
			else{
				mtemp = ctemp.getMethod(n.i.s);
				for(int i = 0; i < n.el.size(); i++){
					if (!symbolTable.compareTypes(n.el.elementAt(i).accept(this), mtemp.getParamAt(i).type())){
						System.out.println("Parametros invalidos no método " + mtemp.getId() + " na class " + ctemp.getId());
					}
				}
			}
		}
		else{
			System.out.println("Definicao invalida de classe");
		}
		if(mtemp == null) return null;
		return mtemp.type();
	}

	// int i;
	public Type visit(IntegerLiteral n) {
		return new IntegerType();
	}

	public Type visit(True n) {
		return new BooleanType();
	}

	public Type visit(False n) {
		return new BooleanType();
	}

	// String s;
	public Type visit(IdentifierExp n) {
		//n.accept(this);
		return symbolTable.getVarType(this.currMethod, this.currClass, n.s);
	}

	public Type visit(This n) {
		//n.accept(this);
		return currClass.type();
	}

	// Exp e;
	public Type visit(NewArray n) {
		Type t = n.e.accept(this);
		if((t instanceof IntegerType) == false){
			System.out.println("A declaracao do new array nao e Integer!");
		}
		return new IntArrayType();
	}

	// Identifier i;
	public Type visit(NewObject n) {
		//n.i.accept(this);
		Class c = this.symbolTable.getClass(n.i.toString());
		if(c == null){
			System.out.println("O objeto nao existe" + n.i.toString());
			return null;
		}
		return c.type();
	}

	// Exp e;
	public Type visit(Not n) {
		Type t = n.e.accept(this);
		if(!this.symbolTable.compareTypes(t, new BooleanType()))
		{
			System.out.println("No boolean inside negation " + n.e.toString());
		}
		return new BooleanType();
	}

	// String s;
	public Type visit(Identifier n) {
		return new IdentifierType(n.s);
	}
}
