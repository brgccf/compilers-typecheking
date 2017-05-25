package main;
import ast.*;
import visitor.BuildSymbolTableVisitor;
import visitor.PrettyPrintVisitor;
import visitor.TypeCheckVisitor;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import java.io.*;
public class Main {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		InputStream stream = new FileInputStream("src/teste.txt");
		ANTLRInputStream input = new ANTLRInputStream(stream);
		brgccf_lfp2Lexer lexer = new brgccf_lfp2Lexer(input);
		CommonTokenStream token = new CommonTokenStream(lexer);
		brgccf_lfp2Parser parser = new brgccf_lfp2Parser(token);
		ParseTree tree = parser.goal();
		System.out.println(tree.toStringTree(parser));
		ASTBuilder visitor = new ASTBuilder();
		Program program = (Program) visitor.visit(tree);
		PrettyPrintVisitor pt = new PrettyPrintVisitor();
		//program.accept(pt);
		BuildSymbolTableVisitor stVis = new BuildSymbolTableVisitor();
		program.accept(stVis);
		program.accept(new TypeCheckVisitor(stVis.getSymbolTable()));
	}

}
