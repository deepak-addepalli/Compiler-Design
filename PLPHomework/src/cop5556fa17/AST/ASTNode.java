package cop5556fa17.AST;

import cop5556fa17.Scanner.Token;

public abstract class ASTNode {
	
	final public Token firstToken;
	private Type type_name;
	private Declaration dec;
	private boolean cartesian;
	
	public ASTNode(Token firstToken) {
		super();
		this.firstToken = firstToken;
	}

	public abstract Object visit(ASTVisitor v, Object arg) throws Exception;

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((firstToken == null) ? 0 : firstToken.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ASTNode other = (ASTNode) obj;
		if (firstToken == null) {
			if (other.firstToken != null)
				return false;
		} else if (!firstToken.equals(other.firstToken))
			return false;
		return true;
	}
	
	public Type getType(){
		return type_name;
	}

	public void setType(Type t){
		type_name = t;
	}

	public Declaration getDec(){
		return dec;
	}

	public void setDec(Declaration dec){
		this.dec = dec;
	}

	public boolean isCartesian(){
		return cartesian;
	}

	public void setCartesian(boolean flag){
		cartesian = flag;
	}
	
	
}
