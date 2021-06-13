import java.util.ArrayList;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LexicalAnalyzer {
    Helper helper;

    ArrayList<String> fails = new ArrayList<>();

    public LexicalAnalyzer(String fileName) {
        helper = new Helper(fileName);
    }

    public boolean isStopCharacter(char currentChar) {
        String currentCharStr = String.valueOf(currentChar);
        TokenType tokenType = Constants.tokenDict.get(currentCharStr);
        return Objects.nonNull(tokenType);
    }

    public boolean isComposedConditionOperator(char currentChar, char nextChar) {
        if (currentChar == '!' || currentChar == '<' || currentChar == '>' || currentChar == '=') {
            if (nextChar == '=') {
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    public Token getValueOfConstant(String string, int line, int column) {
        Pattern pattern = Pattern.compile("[-]?[0-9]+");
        Matcher matcher = pattern.matcher(string);
        if (matcher.matches()) {
            return new Token(TokenType.INTNUM, string, line, column);
        }
        pattern = Pattern.compile("[-]?[0-9]+\\.[0-9]+");
        matcher = pattern.matcher(string);
        if (matcher.matches()) {
            return new Token(TokenType.FLOATNUM, string, line, column);
        }
        if (string.equals("true") || string.equals("false")) {
            return new Token(TokenType.BOOLVAL, string, line, column);
        }
        pattern = Pattern.compile("['].[']");
        matcher = pattern.matcher(string);
        if (matcher.matches()) {
            return new Token(TokenType.CHARACTER, string, line, column);
        }
        pattern = Pattern.compile("[\"].[\"]");
        matcher = pattern.matcher(string);
        if (matcher.matches()) {
            return new Token(TokenType.STRING, string, line, column);
        }
        pattern = Pattern.compile("[a-zA-Z]([a-zA-Z0-9])*");
        matcher = pattern.matcher(string);
        if (matcher.matches()) {
            return new Token(TokenType.ID,string,line,column);
        }



        return null;
    }

    public Token getTokenFromString(String string, int line, int column) throws LexicalException {
        TokenType tokenType = Constants.tokenDict.get(string);
        if (tokenType != null) {
            return new Token(tokenType, string, line, column);
        }

        Token constantValueType = getValueOfConstant(string, line, column);
        if (constantValueType != null) {
            return constantValueType;
        }


        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Unexcepted literal: " + string);
        stringBuilder.append(" At Line: " + String.valueOf(line));
        stringBuilder.append("Column: " + String.valueOf(column));
        fails.add(stringBuilder.toString());

        return new Token(TokenType.FAULT, "fault", line, column);
    }

    public ArrayList<Token> getTokensFromLine(String string, int lineNumber) throws LexicalException {
        String splittedString[] = string.split(" ");


        ArrayList<Token> tokens = new ArrayList<>();
        String buffer;
        for (int j = 0; j < splittedString.length; j++) {
            String currentSplittedString = splittedString[j];
            int substringStartIndex = 0;
            for (int i = 0; i < currentSplittedString.length(); i++) {
                char currentChar = currentSplittedString.charAt(i);

                if (isStopCharacter(currentChar)) {

                    buffer = currentSplittedString.substring(substringStartIndex, i);
                    if (buffer.length() != 0) {
                        Token token = getTokenFromString(buffer, lineNumber, i);
                        tokens.add(token);

                    }
                    if (i != currentSplittedString.length() - 1 && isComposedConditionOperator(currentChar, currentSplittedString.charAt(i + 1))) {

                        buffer = currentSplittedString.substring(i, i + 2);
                        Token token = getTokenFromString(buffer, lineNumber, i);
                        tokens.add(token);

                        substringStartIndex = i + 2;
                        i = i + 1;
                        continue;
                    }
                    substringStartIndex = i + 1;
                    tokens.add(getTokenFromString(String.valueOf(currentChar), lineNumber, i));
                }
            }
            buffer = currentSplittedString.substring(substringStartIndex);
            if (buffer.length() != 0) {
                Token token = getTokenFromString(buffer, lineNumber, substringStartIndex);
                tokens.add(token);
            }
        }
        return tokens;
    }

    public ArrayList<Token> generateTokens() throws LexicalException {
        ArrayList<Token> tokens = new ArrayList<>();
        String currentLine = null;
        int lineNumber = 0;
        while ((currentLine = helper.getNextLineWithoutMultipleSpaces()) != null) {
            tokens.addAll(getTokensFromLine(currentLine, lineNumber));
            lineNumber++;
        }

        if (fails.size() > 0) {
            for (String error : fails) {
                System.out.println(error);
            }
            throw new LexicalException("Code fails to compile with several errors");
        }
        return tokens;
    }

}

