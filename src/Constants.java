import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import static java.util.Map.entry;

enum TokenType {
    ID,
    ASSIGN_OPERATOR,
    NUMBER,
    OPERATOR,
    PARENTHESES,
    KEYWORD,
    COMMENT,
    DELIMITER,
    LINE_END
}

public class Constants {
    public static ArrayList<String> stopCharacters= new ArrayList<>(Arrays.asList(" ","=","<"));
    public static Map<String, TokenType> tokenDict = Map.ofEntries(
            entry("if", TokenType.KEYWORD),
            entry("else", TokenType.KEYWORD),
            entry("func", TokenType.KEYWORD),
            entry("int", TokenType.KEYWORD),
            entry("float", TokenType.KEYWORD),
            entry("bool", TokenType.KEYWORD),
            entry("char", TokenType.KEYWORD),
            entry("while", TokenType.KEYWORD),
            entry("begin", TokenType.KEYWORD),
            entry("end", TokenType.KEYWORD),
            entry("void", TokenType.KEYWORD),
            entry("return", TokenType.KEYWORD),
            entry("#", TokenType.COMMENT),
            entry("=", TokenType.ASSIGN_OPERATOR),
            entry("and", TokenType.OPERATOR),
            entry("or", TokenType.OPERATOR),
            entry("-", TokenType.OPERATOR),
            entry("+", TokenType.OPERATOR),
            entry("*", TokenType.OPERATOR),
            entry("/", TokenType.OPERATOR),
            entry("<", TokenType.OPERATOR),
            entry(">", TokenType.OPERATOR),
            entry("<=", TokenType.OPERATOR),
            entry("==", TokenType.OPERATOR),
            entry(">=", TokenType.OPERATOR),
            entry("!=", TokenType.OPERATOR),
            entry("(", TokenType.PARENTHESES),
            entry(")", TokenType.OPERATOR),
            entry(" ",TokenType.DELIMITER),
            entry(",",TokenType.DELIMITER),
            entry(";",TokenType.LINE_END)
    );

}
