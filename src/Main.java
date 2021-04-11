public class Main {
    public static void main(String[] args) throws LexicalException {
        LexicalAnalyzer lexicalAnalyzer = new LexicalAnalyzer("a.txt");
        System.out.println(lexicalAnalyzer.generateTokens());
    }
}