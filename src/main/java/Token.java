public class Token {
    public TokenType key;
    public String value;
    public int line,column;

    public Token(TokenType key, String value,int line, int column) {
        this.value = value;
        this.key = key;
        this.line = line+1;
        this.column=column+1;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }

    @Override
    public String toString() {
        return "Token{" +
                "key=" + key +
                ", value='" + value + '\'' +

                '}'+'\n';
    }

    public TokenType getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

}
