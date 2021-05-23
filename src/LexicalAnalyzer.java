import java.util.ArrayList;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LexicalAnalyzer {
    Helper helper;

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

    public String getValueOfConstant(String string) {
        Pattern pattern = Pattern.compile("[-]?[0-9]+");
        Matcher matcher = pattern.matcher(string);
        if (matcher.matches()) {
            return "INTNUM";
        }
        pattern = Pattern.compile("[-]?[0-9]+\\.[0-9]+");
        matcher = pattern.matcher(string);
        if (matcher.matches()) {
            return "FLOATNUM";
        }
        if (string.equals("true") || string.equals("false")) {
            return "BOOLVAL";
        }
        pattern = Pattern.compile("['].[']");
        matcher = pattern.matcher(string);
        if (matcher.matches()) {
            return "CHARACTER";
        }
        pattern = Pattern.compile("[a-zA-Z]([a-zA-Z0-9])*");
        matcher = pattern.matcher(string);
        if (matcher.matches()) {
            return "ID";
        }

        return null;
    }

    public Token getTokenFromString(String string, int line, int column) throws LexicalException {
        TokenType tokenType = Constants.tokenDict.get(string);
        if (tokenType != null) {
            return new Token(tokenType, string);
        }

        String constantValueType = getValueOfConstant(string);
        if(constantValueType!=null){
            return new Token(TokenType.NUMBER,constantValueType);
        }

        throw new LexicalException(string, String.valueOf(line), String.valueOf(column));
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
        return tokens;
    }

}

