import java.util.ArrayList;

public class Main {
    public static void main(String[] args) throws LexicalException {

        LexicalAnalyzer lexicalAnalyzer = new LexicalAnalyzer("sampleprogram.kb");
        ArrayList<Token> tokens = lexicalAnalyzer.generateTokens();
        Parser parser = new Parser();
        parser.parse(tokens);

    }
}
