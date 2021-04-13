public class Token {
    public TokenType key;
    public String value;

    public Token(TokenType key, String value) {
        this.value = value;
        this.key = key;
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
