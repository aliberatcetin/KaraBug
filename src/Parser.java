import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

class FailedTokens {
    Token token;
    String message;

    public Token getToken() {
        return token;
    }

    public String getMessage() {
        return message;
    }

    public FailedTokens(Token token, String message) {
        this.token = token;
        this.message = message;
    }
}


public class Parser {

    private ArrayList<Token> tokens;

    ArrayList<FailedTokens> failedTokens = new ArrayList<>();


    private Integer tokenIndex = 0;

    public void parse(ArrayList<Token> tokens) {

        this.tokens = tokens;

        boolean isValidProgram = parseSource();

        if(tokenIndex < tokens.size()) {
            failedTokens.add(new FailedTokens(tokens.get(tokenIndex), "Syntax error"));
            semiColonRecovery(tokens.get(tokenIndex));
            if(tokenIndex < tokens.size()) parse(tokens);
            terminate();
        }

        if (isValidProgram && tokenIndex == tokens.size() && failedTokens.size() == 0) {
            System.out.println("Program compiled successfully");
        } else {
            terminate();
        }

    }

    public void terminate() {
        System.out.println("Failed");
        for (FailedTokens failedToken : failedTokens) {
            System.err.println("Exception at line: " + failedToken.getToken().getLine() + " at col: " + failedToken.getToken().getColumn() + " " + failedToken.getMessage());
        }
        System.exit(1);
    }

    ;


    public void endRecovery(Token token) {
        ArrayList<String> stack = new ArrayList<>();

        while (tokenIndex < tokens.size()) {
            if ("begin".equals(tokens.get(tokenIndex).getValue())) {
                stack.add("1");
            } else if ("end".equals(tokens.get(tokenIndex).getValue())) {
                if (stack.size() == 0) {
                    failedTokens.add(new FailedTokens(token, "Begin missing"));
                    tokenIndex++;
                    return;
                } else {
                    stack.remove(stack.size() - 1);
                    if (stack.size() == 0) {
                        tokenIndex++;
                        return;
                    }
                }
            }
            tokenIndex++;
        }
        failedTokens.add(new FailedTokens(token, "end missing"));
        terminate();
    }

    public void semiColonRecovery(Token token) {

        while (tokenIndex < tokens.size()) {
            if (";".equals(tokens.get(tokenIndex).getValue())) {
                tokenIndex++;
                return;
            }
            tokenIndex++;
        }
        failedTokens.add(new FailedTokens(token, "semi colon missing"));
        terminate();
    }


    private boolean checkTerminal(String terminal) {

        if (tokenIndex == tokens.size()) {
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


        boolean isValid = funcs1() || nullTerminal();

        if (isValid) return true;


        return false;
    }

    private boolean funcs1() {


        boolean isValid = parseFuncSt() && parseFuncs();

        if (isValid) return true;

        return false;
    }

    private boolean parseDecls() {


        boolean isValid = decls1() || nullTerminal();

        if (isValid) return true;

        return false;

    }

    private boolean decls1() {

        boolean isValid = parseDecl() && parseDecls();

        if (isValid) return true;
        return false;
    }

    private boolean parseSts() {


        boolean isValid = sts1() || nullTerminal();
        if (isValid) return true;
        return false;
    }

    private boolean sts1() {


        boolean isValid = parseSt() && parseSts();

        if (isValid) return true;
        return false;
    }

    private boolean parseSt() {

        boolean isValid = parseIfSt() || parseWhileSt() || parseAssignmentSt() || parseReturnSt() || parseFuncCallSt();

        if (isValid) return true;
        return false;
    }

    private boolean parseFuncCallSt() {
        int fallbackIndex = tokenIndex;
        boolean isValid = parseFuncCallExp() && checkTerminal(";");
        if (isValid) return true;
        tokenIndex = fallbackIndex;
        return false;
    }

    private boolean parseIfSt() {
        int fallbackIndex = tokenIndex;
        Token token=null;
        if(tokenIndex<tokens.size()){
            token = tokens.get(tokenIndex);
        }
        boolean flag = false;
        boolean existsIf = checkTerminal("if");
        boolean isValid = existsIf && checkTerminal("(") && parseBooleanExp() && checkTerminal(")") && parseIfBlock();

        if (isValid) return true;
        else if (existsIf) {
            failedTokens.add(new FailedTokens(token, "if syntax fail"));
            endRecovery(token);
            flag = true;
        } else {
            tokenIndex = fallbackIndex;
        }

        return flag;
    }

    private boolean parseIfBlock() {

        int fallbackIndex = tokenIndex;

        boolean isValid = checkTerminal("begin") && parseSts() && parseElseBlock() && checkTerminal("end");

        if (isValid) return true;
        tokenIndex = fallbackIndex;
        return false;
    }

    private boolean parseElseBlock() {


        boolean isValid = elseBlock1() || nullTerminal();

        if (isValid) return true;
        return false;
    }

    private boolean elseBlock1() {

        int fallbackIndex = tokenIndex;

        boolean isValid = checkTerminal("else") && parseSts() && parseElseBlock();

        if (isValid) return true;
        tokenIndex = fallbackIndex;
        return false;
    }

    private boolean parseBlock() {

        int fallbackIndex = tokenIndex;

        boolean isValid = checkTerminal("begin") && parseSts() && checkTerminal("end");

        if (isValid) return true;
        tokenIndex = fallbackIndex;
        return false;
    }

    private boolean parseWhileSt() {


        int fallbackIndex = tokenIndex;
        Token token=null;
        if(tokenIndex<tokens.size()){
            token = tokens.get(tokenIndex);
        }

        boolean whileExists = checkTerminal("while");
        boolean isValid =whileExists  && checkTerminal("(") && parseBooleanExp() && checkTerminal(")") && parseBlock();

        if (isValid) return true;
        else if (whileExists) {
            failedTokens.add(new FailedTokens(token, "while syntax fail"));
            endRecovery(token);
        } else {
            tokenIndex = fallbackIndex;
        }
        return false;
    }

    private boolean parseAssignmentSt() {

        boolean isValid = assignment1() || assignment2();

        if (isValid) return true;
        return false;

    }

    private boolean assignment1() {

        int fallbackIndex = tokenIndex;

        boolean isValid = checkTerminal("ID") && checkTerminal("=") && parseArithmeticExp() && checkTerminal(";");

        if (isValid) return true;
        tokenIndex = fallbackIndex;
        return false;
    }

    private boolean assignment2() {

        int fallbackIndex = tokenIndex;

        boolean isValid = checkTerminal("ID") && checkTerminal(";");

        if (isValid) return true;
        tokenIndex = fallbackIndex;
        return false;
    }

    private boolean parseReturnSt() {


        boolean isValid = returnSt1() || returnSt2();

        if (isValid) return true;
        return false;
    }

    private boolean returnSt1() {

        int fallbackIndex = tokenIndex;

        boolean isValid = checkTerminal("return") && parseArithmeticExp() && checkTerminal(";");

        if (isValid) return true;
        tokenIndex = fallbackIndex;
        return false;
    }

    private boolean returnSt2() {

        int fallbackIndex = tokenIndex;

        boolean isValid = checkTerminal("return") && checkTerminal(";");

        if (isValid) return true;
        tokenIndex = fallbackIndex;
        return false;
    }


    private boolean parseBooleanExp() {


        boolean isValid = parseArithmeticExp() && parseBoolop() && parseArithmeticExp();

        if (isValid) return true;
        return false;
    }

    private boolean parseBoolop() {

        int fallbackIndex = tokenIndex;

        boolean isValid = checkTerminal(">") || checkTerminal("<") || checkTerminal("==") || checkTerminal("<=") || checkTerminal(">=") || checkTerminal("!=");

        if (isValid) return true;
        tokenIndex = fallbackIndex;
        return false;
    }

    private boolean parseFuncSt() {

        int fallbackIndex = tokenIndex;
        Token token=null;
        if(tokenIndex<tokens.size()){
            token = tokens.get(tokenIndex);
        }
        boolean flag = false;

        boolean funcExists = checkTerminal("func");
        boolean isValid = funcExists && (funcst2() || funcst1());

        if (isValid) return true;
        else if (funcExists) {
            failedTokens.add(new FailedTokens(token, "function syntax fail"));
            endRecovery(token);
            flag = true;
        } else {
            tokenIndex = fallbackIndex;
        }
        return flag;
    }

    private boolean funcst2() {

        int fallbackIndex = tokenIndex;
        boolean isValid =  checkTerminal("ID") && checkTerminal("(") && checkTerminal(")") && checkTerminal(":") && parseType() && parseBlock();

        if (isValid) return true;
        tokenIndex = fallbackIndex;
        return false;

    }

    private boolean funcst1(){
        int fallbackIndex = tokenIndex;

        boolean isValid = checkTerminal("ID") && checkTerminal("(") && parseParams() && checkTerminal(")") && checkTerminal(":") && parseType() && parseBlock();

        if(isValid) return true;

        tokenIndex = fallbackIndex;
        return false;

    }

    private boolean parseParams() {

        boolean isValid = params1() || parseFuncDecl();

        if (isValid) return true;
        return false;
    }

    private boolean params1() {

        int fallbackIndex = tokenIndex;

        boolean isValid = parseFuncDecl() && checkTerminal(",") && parseParams();

        if (isValid) return true;
        tokenIndex = fallbackIndex;
        return false;
    }

    private boolean parseFuncDecl() {


        boolean isValid = funcDecl("int") || funcDecl("float") || funcDecl("bool") || funcDecl("char");

        if (isValid) return true;
        return false;
    }

    private boolean parseDecl() {


        boolean isValid = decl("int") || decl("float") || decl("bool") || decl("char");

        if (isValid) return true;
        return false;
    }

    private boolean decl(String type) {
        int fallbackIndex = tokenIndex;

        boolean isValid = checkTerminal(type) && parseAssignmentSt();

        if (isValid) return true;
        tokenIndex = fallbackIndex;

        return false;
    }


    private boolean funcDecl(String type) {

        int fallbackIndex = tokenIndex;

        boolean isValid = checkTerminal(type) && checkTerminal("ID");

        if (isValid) return true;
        tokenIndex = fallbackIndex;
        return false;
    }

    private boolean parseType() {
        return checkTerminal("int") || checkTerminal("float") || checkTerminal("void") || checkTerminal("bool") || checkTerminal("char");
    }

    private boolean parseArithmeticExp() {
        boolean isValid = parseArithmetic1() || parseArithmetic2() || parseArithmetic3() || parseArithmetic4() || parseArithmetic5() || parseMultExp();
        if (isValid) return true;
        return false;
    }

    private boolean parseArithmetic1() {
        int fallbackIndex = tokenIndex;
        boolean isValid = parseMultExp() && checkTerminal("and") && parseArithmeticExp();
        if (isValid) return true;
        tokenIndex = fallbackIndex;
        return false;
    }

    private boolean parseArithmetic2() {
        int fallbackIndex = tokenIndex;
        boolean isValid = parseMultExp() && checkTerminal("or") && parseArithmeticExp();
        if (isValid) return true;
        tokenIndex = fallbackIndex;
        return false;
    }

    private boolean parseArithmetic3() {
        int fallbackIndex = tokenIndex;
        boolean isValid = parseMultExp() && checkTerminal("-") && parseArithmeticExp();
        if (isValid) return true;
        tokenIndex = fallbackIndex;
        return false;
    }

    private boolean parseArithmetic4() {
        int fallbackIndex = tokenIndex;
        boolean isValid = parseMultExp() && checkTerminal("+") && parseArithmeticExp();
        if (isValid) return true;
        tokenIndex = fallbackIndex;
        return false;
    }

    private boolean parseArithmetic5() {
        int fallbackIndex = tokenIndex;

        boolean isValid = parseMultExp() && checkTerminal("%") && parseArithmeticExp();
        if (isValid) return true;
        tokenIndex = fallbackIndex;
        return false;
    }

    private boolean parseFuncCallParams() {


        boolean isValid = funcCallParams1() || parseSimpleExp();
        if (isValid) return true;
        return false;
    }

    private boolean funcCallParams1() {

        int fallbackIndex = tokenIndex;

        boolean isValid = parseSimpleExp() && checkTerminal(",") && parseFuncCallParams();

        if (isValid) return true;
        tokenIndex = fallbackIndex;
        return false;
    }

    private boolean parseMultExp() {
        boolean isValid = parseMultExp1() || parseMultExp2() || parseSimpleExp();
        if (isValid) return true;
        return false;
    }

    private boolean parseMultExp1() {
        int fallbackIndex = tokenIndex;
        boolean isValid = parseSimpleExp() && checkTerminal("*") && parseMultExp();
        if (isValid) return true;
        tokenIndex = fallbackIndex;
        return false;
    }

    private boolean parseMultExp2() {
        int fallbackIndex = tokenIndex;
        boolean isValid = parseSimpleExp() && checkTerminal("/") && parseMultExp();
        if (isValid) return true;
        tokenIndex = fallbackIndex;
        return false;
    }

    private boolean parseSimpleExp() {
        int fallbackIndex = tokenIndex;
        boolean isValid = parseFuncCallExp() || checkTerminal("ID") || checkTerminal("INTNUM") || checkTerminal("FLOATNUM") || checkTerminal("BOOLVAL")
                || checkTerminal("CHARACTER") || parseSimpleExp1();
        if (isValid) return true;
        tokenIndex = fallbackIndex;
        return false;
    }

    private boolean parseSimpleExp1() {
        int fallbackIndex = tokenIndex;
        boolean isValid = checkTerminal("(") && parseArithmeticExp() && checkTerminal(")");
        if (isValid) return true;
        tokenIndex = fallbackIndex;
        return false;
    }

    private boolean parseFuncCallExp1() {
        int fallbackIndex = tokenIndex;
        boolean isValid = checkTerminal("ID") && checkTerminal("(") && parseFuncCallParams() && checkTerminal(")");
        if (isValid) return true;
        tokenIndex = fallbackIndex;
        return false;
    }

    private boolean parseFuncCallExp2() {
        int fallbackIndex = tokenIndex;
        boolean isValid = checkTerminal("ID") && checkTerminal("(") && checkTerminal(")");
        if (isValid) return true;
        tokenIndex = fallbackIndex;
        return false;
    }

    private boolean parseFuncCallExp() {

        boolean isValid = parseFuncCallExp1() || parseFuncCallExp2();
        if (isValid) return true;
        return false;
    }
}
