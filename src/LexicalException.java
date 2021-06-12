public class LexicalException extends Exception{

    public LexicalException(String errStr/*String erroneousString, String line, String column*/){
        super(errStr);
        //super(erroneousString+" at line: "+line+" column: "+column);
    }
}
