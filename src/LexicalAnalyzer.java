import java.util.ArrayList;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LexicalAnalyzer {
    Helper helper;
    public LexicalAnalyzer(String fileName){
        helper=new Helper(fileName);

    }

    public boolean isStopCharacter(char currentChar){
        String currentCharStr = String.valueOf(currentChar);
        TokenType tokenType = Constants.tokenDict.get(currentCharStr);
        return Objects.nonNull(tokenType);
    }

    public Token getTokenFromString(String string, int line, int column) throws LexicalException {
        TokenType tokenType = Constants.tokenDict.get(string);
        if(tokenType!=null){
            return new Token(tokenType,string);
        }

        Pattern pattern = Pattern.compile("[0-9]+");
        Matcher matcher = pattern.matcher(string);
        if (matcher.matches()) {
            return new Token(TokenType.NUMBER,matcher.group(0));
        }

        pattern = Pattern.compile("[a-zA-Z]([a-zA-Z0-9])*");
        matcher = pattern.matcher(string);
        if (matcher.matches()) {
            return new Token(TokenType.ID,matcher.group(0));
        }

        throw new LexicalException(string, String.valueOf(line),String.valueOf(column));
    }

    public ArrayList<Token> getTokensFromLine(String string,int lineNumber) throws LexicalException {
        String splittedString[] = string.split(" ");


        ArrayList<Token> tokens = new ArrayList<>();
        String buffer;
        for (int j = 0; j < splittedString.length; j++) {
            int substringStartIndex=0;
            for(int i=0;i<splittedString[j].length();i++){
                char currentChar = splittedString[j].charAt(i);
                if(isStopCharacter(currentChar)){
                    buffer = splittedString[j].substring(substringStartIndex,i);
                    if(buffer.length()!=0){
                        Token token = getTokenFromString(buffer,lineNumber,i);
                        tokens.add(token);
                    }
                    tokens.add(getTokenFromString(String.valueOf(currentChar),lineNumber,i));
                    substringStartIndex=i+1;

                }
            }
            buffer = splittedString[j].substring(substringStartIndex);
            if(buffer.length()!=0){
                Token token = getTokenFromString(buffer,lineNumber,substringStartIndex);
                tokens.add(token);
            }
        }
        return tokens;
    }

    public ArrayList<Token> generateTokens() throws LexicalException {
        ArrayList<Token> tokens = new ArrayList<>();
        String currentLine=null;
        int lineNumber=0;
        while((currentLine=helper.getNextLine())!=null){
            tokens.addAll(getTokensFromLine(currentLine,lineNumber));
            lineNumber++;
        }
        return tokens;
    }

}

