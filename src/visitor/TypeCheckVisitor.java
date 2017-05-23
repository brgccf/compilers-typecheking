package visitor;

import symboltable.Method;
import symboltable.Class;
import symboltable.SymbolTable;

import com.sun.xml.internal.bind.v2.runtime.unmarshaller.InterningXmlVisitor;

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
		return null;
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
		//n.e.accept(this);
		//Type t = n.e.accept(this);
		boolean result = this.symbolTable.compareTypes(n.s1.accept(this), n.s2.accept(this));
		n.s1.accept(this);
		n.s2.accept(this);
		return null;
	}

	// Exp e;
	// Statement s;
	public Type visit(While n) {
		n.e.accept(this);
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
		//n.i.accept(this);
		//n.e.accept(this);
		Type t1 = n.i.accept(this);
		Type t2 = n.e.accept(this);
		boolean result = this.symbolTable.compareTypes(t1, t2);
		System.out.println("There was a type error!");
		System.exit(0);
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
		n.e1.accept(this);
		n.e2.accept(this);
		return null;
	}

	// Exp e1,e2;
	public Type visit(LessThan n) {
		//n.e1.accept(this);
		//n.e2.accept(this);
		Type t1 = n.e1.accept(this);
		Type t2 = n.e2.accept(this);
		boolean equal = this.symbolTable.compareTypes(t1, t2);
		return null;
	}

	// Exp e1,e2;
	public Type visit(Plus n) {
		Type t1 = n.e1.accept(this);
		Type t2 = n.e2.accept(this);
		boolean equal = this.symbolTable.compareTypes(t1, t2);
		return null;
	}

	// Exp e1,e2;
	public Type visit(Minus n) {
		Type t1 = n.e1.accept(this);
		Type t2 = n.e2.accept(this);
		boolean equal = this.symbolTable.compareTypes(t1, t2);
		return null;
	}

	// Exp e1,e2;
	public Type visit(Times n) {
		Type t1 = n.e1.accept(this);
		Type t2 = n.e2.accept(this);
		boolean equal = this.symbolTable.compareTypes(t1, t2);
		return null;
	}

	// Exp e1,e2;
	public Type visit(ArrayLookup n) {
		n.e1.accept(this);
		n.e2.accept(this);
		return null;
	}

	// Exp e;
	public Type visit(ArrayLength n) {
		n.e.accept(this);
		return null;
	}

	// Exp e;
	// Identifier i;
	// ExpList el;
	public Type visit(Call n) {
		Class ctemp = this.currClass;
		Method mtemp = this.currMethod;
		
		n.e.accept(this);
		//n.i.accept(this);
		Type methodType = symbolTable.getMethodType(n.i.s, ctemp.getId());
		Type expType = null;
		for (int i = 0; i < n.el.size(); i++) {
			//n.el.elementAt(i).accept(this);
			if(n.el.elementAt(i) != null)
			{
				expType = n.el.elementAt(i).accept(this);
			}
		}
		this.currClass = ctemp;
		this.currMethod = mtemp;
		if(methodType == null) return expType;
		else return methodType;
	}

	// int i;
	public Type visit(IntegerLiteral n) {
		n.accept(this);
		return new IntegerType();
	}

	public Type visit(True n) {
		n.accept(this);
		return new BooleanType();
	}

	public Type visit(False n) {
		n.accept(this);
		return new BooleanType();
	}

	// String s;
	public Type visit(IdentifierExp n) {
		n.accept(this);
		return symbolTable.getVarType(this.currMethod, this.currClass, n.s);
	}

	public Type visit(This n) {
		n.accept(this);
		return null;
	}

	// Exp e;
	public Type visit(NewArray n) {
		n.e.accept(this);
		return null;
	}

	// Identifier i;
	public Type visit(NewObject n) {
		n.i.accept(this);
		this.currClass = this.symbolTable.getClass(n.i.s);
		this.currMethod = null;
		return null;
	}

	// Exp e;
	public Type visit(Not n) {
		n.e.accept(this);
		return null;
	}

	// String s;
	public Type visit(Identifier n) {
		return symbolTable.getVarType(this.currMethod, this.currClass, n.s);
	}
}
