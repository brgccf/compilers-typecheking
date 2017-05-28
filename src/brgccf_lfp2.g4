grammar brgccf_lfp2;

WS : [ \t\r\n\f]+ -> skip ;

goal
	:	mainClass ( classDeclaration )* EOF
	;
mainClass
	:	'class' Identifier '{' 'public' 'static' 'void' 'main' '(' 'String' 
		'[' ']' Identifier ')' '{' statement '}' '}'
	;
classDeclaration
	:	'class' Identifier ( 'extends' Identifier )? '{' ( varDeclaration )* 
		( methodDeclaration )* '}' 	
	;
varDeclaration 
	: 
		type Identifier ';'
	;
methodDeclaration :'public' type Identifier '(' ( type Identifier ( 
				   ',' type Identifier )* )? ')' '{' ( varDeclaration )* 
				   ( statement )* 'return' expression ';' '}'
				   ;
type
	:	'int' '[' ']'  
	|	'boolean'
	|	'int'
	|	Identifier
	;
statement
	:	'{'(statement)* '}'
	|	'if' '(' expression ')' statement 'else' statement
	|	'while' '(' expression ')' statement
	|	'System.out.println' '(' expression ')'';'
	|	Identifier '=' expression ';'
	|	Identifier '[' expression ']' '=' expression ';'
	;
INTEGER_LITERAL
	:
		[1-9][0-9]*|[0] //INTEGER_LITERAL
	;
expression
	:
		expression OP expression
	|	
		expression '[' expression ']'
	|
		expression '.' 'length'
	|
		expression '.' Identifier '(' ( expression ( ',' expression )* )? ')'
	|
		INTEGER_LITERAL
	|
		'true' 
	|
		'false' 
	|
		Identifier
	|
		'this' 
	|
		'new' 'int' '[' expression ']'
	|
		'new' Identifier '(' ')'
	|
		'!' expression
	|
		'(' expression ')'
	;
OP
	:
		( '&&' | '<' | '+' | '-' | '*' )
	;
Identifier
	:
		([a-zA-Z]|'_')([a-zA-Z]|'_'|[0-9])*
	;
Comment_line
	:
		'//'(~'\n')* -> skip
	;
Comment_ext
	:
		'/*' .*? '*/' -> skip
	;
