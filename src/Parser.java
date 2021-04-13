import java.util.ArrayList;

public class Parser {

    private ArrayList<Token> tokens;

    private Integer tokenIndex = 0;

    public void parse(ArrayList<Token> tokens) {

        this.tokens = tokens;

        boolean isValidProgram = parseSource();

        if (isValidProgram && tokenIndex == tokens.size()) {
            System.out.println("Program compiled successfully");
        }
        else {
            System.out.println("Failed");
        }

    }

    private boolean checkTerminal(String terminal){

        if(tokenIndex == tokens.size()){

            return false;
        }

        String expectedTerminal = tokens.get(tokenIndex).getValue();

        boolean equals = expectedTerminal.equals(terminal);
        if (equals) tokenIndex++;

        return equals;
    }

    private boolean parseSource() {

        return parseFuncs() && parseDecls() && parseSts();
    }

    private boolean nullTerminal() {

        return true;
    }

    private boolean parseFuncs() {

        int fallbackIndex = tokenIndex;

        boolean isValid = funcs1() || nullTerminal();

        if(isValid) return true;

        tokenIndex = fallbackIndex;
        return false;
    }

    private boolean funcs1() {

        int fallbackIndex = tokenIndex;

        boolean isValid = parseFuncSt() && parseFuncs();

        if(isValid) return true;

        tokenIndex = fallbackIndex;
        return false;
    }

    private boolean parseDecls() {

        int fallbackIndex = tokenIndex;

        boolean isValid = decls1() || nullTerminal();

        if(isValid) return true;

        tokenIndex = fallbackIndex;
        return false;

    }

    private boolean decls1() {

        int fallbackIndex = tokenIndex;
        boolean isValid = parseDecl() && parseDecls();

        if(isValid) return true;
        tokenIndex = fallbackIndex;
        return false;
    }

    private boolean parseSts() {

        int fallbackIndex = tokenIndex;

        boolean isValid =  sts1() || nullTerminal();
        if(isValid) return true;
        tokenIndex = fallbackIndex;
        return false;
    }

    private boolean sts1() {

        int fallbackIndex = tokenIndex;

        boolean isValid = parseSt() && parseSts();

        if(isValid) return true;
        tokenIndex = fallbackIndex;
        return false;
    }

    private boolean parseSt() {

        int fallbackIndex = tokenIndex;

        boolean isValid = parseIfSt() || parseWhileSt() || parseAssignmentSt() || parseReturnSt();

        if(isValid) return true;
        tokenIndex = fallbackIndex;
        return false;
    }

    private boolean parseIfSt() {

        int fallbackIndex = tokenIndex;

        boolean isValid = checkTerminal("if") && checkTerminal("(") && parseBooleanExp() && checkTerminal(")") && parseIfBlock();

        if(isValid) return true;
        tokenIndex = fallbackIndex;
        return false;
    }

    private boolean parseIfBlock() {

        int fallbackIndex = tokenIndex;

        boolean isValid = checkTerminal("begin") && parseSts() && parseElseBlock() &&  checkTerminal("end");

        if(isValid) return true;
        tokenIndex = fallbackIndex;
        return false;
    }

    private boolean parseElseBlock() {

        int fallbackIndex = tokenIndex;

        boolean isValid = elseBlock1() ||  nullTerminal();

        if(isValid) return true;
        tokenIndex = fallbackIndex;
        return false;
    }

    private boolean elseBlock1() {

        int fallbackIndex = tokenIndex;

        boolean isValid = checkTerminal("else") && parseSts() && parseElseBlock();

        if(isValid) return true;
        tokenIndex = fallbackIndex;
        return false;
    }

    private boolean parseBlock() {

        int fallbackIndex = tokenIndex;

        boolean isValid = checkTerminal("begin") && parseSts() &&  checkTerminal("end");

        if(isValid) return true;
        tokenIndex = fallbackIndex;
        return false;
    }

    private boolean parseWhileSt() {

        int fallbackIndex = tokenIndex;

        boolean isValid = checkTerminal("while") && checkTerminal("(") &&  parseBooleanExp() && checkTerminal(")") &&  parseBlock();

        if(isValid) return true;
        tokenIndex = fallbackIndex;
        return false;
    }

    private boolean parseAssignmentSt() {
        int fallbackIndex = tokenIndex;

        boolean isValid = assignment1() || assignment2();

        if(isValid) return true;
        tokenIndex = fallbackIndex;
        return false;

    }

    private boolean assignment1(){

        int fallbackIndex = tokenIndex;

        boolean isValid = checkTerminal("ID") && checkTerminal("=") &&  parseArithmeticExp() && checkTerminal(";");

        if(isValid) return true;
        tokenIndex = fallbackIndex;
        return false;
    }

    private boolean assignment2(){

        int fallbackIndex = tokenIndex;

        boolean isValid = checkTerminal("ID") && checkTerminal(";");

        if(isValid) return true;
        tokenIndex = fallbackIndex;
        return false;
    }

    private boolean parseReturnSt() {

        int fallbackIndex = tokenIndex;

        boolean isValid = checkTerminal("return") && parseArithmeticExp() && checkTerminal(";");

        if(isValid) return true;
        tokenIndex = fallbackIndex;
        return false;
    }



    private boolean parseBooleanExp() {

        int fallbackIndex = tokenIndex;

        boolean isValid = parseArithmeticExp() && parseBoolop() && parseArithmeticExp();

        if(isValid) return true;
        tokenIndex = fallbackIndex;
        return false;
    }

    private boolean parseBoolop() {

        int fallbackIndex = tokenIndex;

        boolean isValid = checkTerminal(">") || checkTerminal("<") || checkTerminal("==") || checkTerminal("<=") || checkTerminal(">=") || checkTerminal("!=");

        if(isValid) return true;
        tokenIndex = fallbackIndex;
        return false;
    }

    private boolean parseFuncSt() {

        int fallbackIndex = tokenIndex;

        boolean isValid = checkTerminal("func") && checkTerminal("ID") && checkTerminal("(") && parseParams() && checkTerminal(")") && checkTerminal(":") && parseType() && parseBlock();

        if(isValid) return true;

        tokenIndex = fallbackIndex;
        return false;
    }

    private boolean parseParams() {

        int fallbackIndex = tokenIndex;

        boolean isValid = parseFuncDecl() || params1();

        if(isValid) return true;
        tokenIndex = fallbackIndex;
        return false;
    }

    private boolean params1() {

        int fallbackIndex = tokenIndex;

        boolean isValid = parseFuncDecl() && checkTerminal(",") && parseParams();

        if(isValid) return true;
        tokenIndex = fallbackIndex;
        return false;
    }

    private boolean parseFuncDecl() {

        int fallbackIndex = tokenIndex;

        boolean isValid = funcDecl("int") || funcDecl("float") || funcDecl("bool") || funcDecl("char");

        if(isValid) return true;
        tokenIndex = fallbackIndex;
        return false;
    }

    private boolean parseDecl() {

        int fallbackIndex = tokenIndex;

        boolean isValid = decl("int") || decl("float") || decl("bool") || decl("char");

        if(isValid) return true;
        tokenIndex = fallbackIndex;
        return false;
    }

    private boolean decl(String type) {

        int fallbackIndex = tokenIndex;

        boolean isValid = checkTerminal(type) && parseAssignmentSt();

        if(isValid) return true;
        tokenIndex = fallbackIndex;
        return false;
    }


    private boolean funcDecl(String type) {

        int fallbackIndex = tokenIndex;

        boolean isValid = checkTerminal(type) && checkTerminal("ID");

        if(isValid) return true;
        tokenIndex = fallbackIndex;
        return false;
    }

    private boolean parseType() {

        return checkTerminal("int") || checkTerminal("float") || checkTerminal("void") || checkTerminal("bool") || checkTerminal("char");
    }

    private boolean parseArithmeticExp(){
        int fallbackIndex = tokenIndex;
        boolean isValid = parseArithmetic1() || parseArithmetic2() || parseArithmetic3()  || parseArithmetic4()  || parseArithmetic5() || parseMultExp();
        if(isValid) return true;
        tokenIndex = fallbackIndex;
        return false;
    }
    private boolean parseArithmetic1(){
        int fallbackIndex = tokenIndex;
        boolean isValid = parseMultExp() && checkTerminal("and") && parseArithmeticExp();
        if(isValid) return true;
        tokenIndex = fallbackIndex;
        return false;
    }
    private boolean parseArithmetic2(){
        int fallbackIndex = tokenIndex;
        boolean isValid = parseMultExp() && checkTerminal("or") && parseArithmeticExp();
        if(isValid) return true;
        tokenIndex = fallbackIndex;
        return false;
    }

    private boolean parseArithmetic3(){
        int fallbackIndex = tokenIndex;
        boolean isValid = parseMultExp() && checkTerminal("-") && parseArithmeticExp();
        if(isValid) return true;
        tokenIndex = fallbackIndex;
        return false;
    }
    private boolean parseArithmetic4(){
        int fallbackIndex = tokenIndex;
        boolean isValid = parseMultExp() && checkTerminal("+") && parseArithmeticExp();
        if(isValid) return true;
        tokenIndex = fallbackIndex;
        return false;
    }
    private boolean parseArithmetic5(){
        int fallbackIndex = tokenIndex;
        boolean isValid = parseMultExp() && checkTerminal("%") && parseArithmeticExp();
        if(isValid) return true;
        tokenIndex = fallbackIndex;
        return false;
    }

    private boolean parseFuncCallParams() {

        int fallbackIndex = tokenIndex;

        boolean isValid = funcCallParams1() || nullTerminal();
        if(isValid) return true;
        tokenIndex = fallbackIndex;
        return false;
    }

    private boolean funcCallParams1() {

        int fallbackIndex = tokenIndex;

        boolean isValid = parseSimpleExp() && checkTerminal(",") && parseFuncCallParams();

        if(isValid) return true;
        tokenIndex = fallbackIndex;
        return false;
    }

    private boolean parseMultExp(){
        int fallbackIndex = tokenIndex;
        boolean isValid = parseMultExp1() ||  parseMultExp2() || parseSimpleExp();
        if(isValid) return true;
        tokenIndex = fallbackIndex;
        return false;
    }
    private boolean parseMultExp1(){
        int fallbackIndex = tokenIndex;
        boolean isValid = parseSimpleExp() && checkTerminal("*") && parseMultExp();
        if(isValid) return true;
        tokenIndex = fallbackIndex;
        return false;
    }
    private boolean parseMultExp2(){
        int fallbackIndex = tokenIndex;
        boolean isValid = parseSimpleExp() && checkTerminal("/") && parseMultExp();
        if(isValid) return true;
        tokenIndex = fallbackIndex;
        return false;
    }
    private boolean parseSimpleExp(){
        int fallbackIndex = tokenIndex;
        boolean isValid = checkTerminal("ID") || checkTerminal("INTNUM") || checkTerminal("FLOATNUM") || checkTerminal("BOOLVAL")
                || checkTerminal("CHARACTER") || parseSimpleExp1() || parseSimpleExp2() || parseSimpleExp3();
        if(isValid) return true;
        tokenIndex = fallbackIndex;
        return false;
    }

    private boolean parseSimpleExp1(){
        int fallbackIndex = tokenIndex;
        boolean isValid = checkTerminal("(") && parseArithmeticExp() && checkTerminal(")");
        if(isValid) return true;
        tokenIndex = fallbackIndex;
        return false;
    }

    private boolean parseSimpleExp2(){
        int fallbackIndex = tokenIndex;
        boolean isValid = checkTerminal("ID")  && checkTerminal("(") && parseFuncCallParams() && checkTerminal(")");
        if(isValid) return true;
        tokenIndex = fallbackIndex;
        return false;
    }

    private boolean parseSimpleExp3(){
        int fallbackIndex = tokenIndex;
        boolean isValid = checkTerminal("ID")  && checkTerminal("(") && checkTerminal(")");
        if(isValid) return true;
        tokenIndex = fallbackIndex;
        return false;
    }
}
