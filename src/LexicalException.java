public class LexicalException extends Exception{

    public LexicalException(String erroneousString, String line, String column){
        super(erroneousString+" at line: "+line+" column: "+column);
    }
}
