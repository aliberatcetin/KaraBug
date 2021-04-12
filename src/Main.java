public class Main {
    public static void main(String[] args) throws LexicalException {
        LexicalAnalyzer lexicalAnalyzer = new LexicalAnalyzer("a.txt");
        Grammar grammar = new Grammar("grammar.txt");
        grammar.printGrammar();
        grammar.check(lexicalAnalyzer.generateTokens());
    }
}