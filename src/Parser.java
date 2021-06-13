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

    Node root = new Node(0, "source", null);


    private Integer tokenIndex = 0;

    public void parse(ArrayList<Token> tokens) {

        this.tokens = tokens;


        boolean isValidProgram = parseSource();

        if (tokenIndex < tokens.size()) {
            failedTokens.add(new FailedTokens(tokens.get(tokenIndex), "Syntax error"));
            semiColonRecovery(tokens.get(tokenIndex));
            if (tokenIndex < tokens.size()) parse(tokens);
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


    public Node addNewNonTerminalToParent(Node parent, String nonterminalName) {
        Node newNode = new Node(parent.getChilds().size() + 1, nonterminalName, null);
        parent.addChild(newNode);
        return newNode;
    }


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


    private boolean checkTerminal(Node parent, String terminal) {


        if (tokenIndex == tokens.size()) {
            return false;
        }

        String expectedTerminal = tokens.get(tokenIndex).getValue();

        boolean equals = expectedTerminal.equals(terminal);
        if (equals) {
            Node node = new Node(parent.getChilds().size() + 1, null, tokens.get(tokenIndex));
            parent.addChild(node);
            tokenIndex++;
        }

        return equals;
    }

    private boolean parseSource() {

        return parseFuncs(root) && parseDecls(root) && parseSts(root);
    }

    private boolean nullTerminal() {

        return true;
    }

    private boolean parseFuncs(Node parent) {

        Node node = new Node(parent.getChilds().size() + 1, "funcs", null);
        parent.addChild(node);

        boolean isValid = funcs1(node) || nullTerminal();


        if (isValid) return true;

        parent.removeChild(node);

        return false;
    }

    private boolean funcs1(Node parent) {


        boolean isValid = parseFuncSt(parent) && parseFuncs(parent);

        if (isValid) return true;


        return false;
    }

    private boolean parseDecls(Node parent) {

        Node node = new Node(parent.getChilds().size() + 1, "decls", null);
        parent.addChild(node);

        boolean isValid = decls1(node) || nullTerminal();

        if (isValid) return true;


        parent.removeChild(node);

        return false;

    }

    private boolean decls1(Node parent) {

        boolean isValid = parseDecl(parent) && parseDecls(parent);

        if (isValid) return true;
        return false;
    }

    private boolean parseSts(Node parent) {

        Node node = new Node(parent.getChilds().size() + 1, "sts", null);
        parent.addChild(node);

        boolean isValid = sts1(node) || nullTerminal();
        if (isValid) return true;
        parent.removeChild(node);
        return false;
    }

    private boolean sts1(Node parent) {

        boolean isValid = parseSt(parent) && parseSts(parent);

        if (isValid) return true;
        return false;
    }

    private boolean parseSt(Node parent) {

        Node node = addNewNonTerminalToParent(parent, "st");
        parent.addChild(node);

        boolean isValid = parseIfSt(node) || parseWhileSt(node) || parseAssignmentSt(node) || parseReturnSt(node) || parseFuncCallSt(node);

        if (isValid) return true;
        parent.removeChild(node);
        return false;
    }

    private boolean parseFuncCallSt(Node parent) {
        int fallbackIndex = tokenIndex;
        Node node = addNewNonTerminalToParent(parent,"funccalst");

        boolean isValid = parseFuncCallExp(node) && checkTerminal(node,";");
        if (isValid) return true;
        tokenIndex = fallbackIndex;
        parent.removeChild(node);
        return false;
    }

    private boolean parseIfSt(Node parent) {

        int fallbackIndex = tokenIndex;
        Token token = null;
        if (tokenIndex < tokens.size()) {
            token = tokens.get(tokenIndex);
        }

        Node node = new Node(parent.getChilds().size() + 1, "if", null);
        parent.addChild(node);

        boolean flag = false;
        boolean existsIf = checkTerminal(node, "if");
        boolean isValid = existsIf && checkTerminal(node, "(") && parseBooleanExp(node) && checkTerminal(node, ")") && parseIfBlock(node);

        if (isValid) return true;
        else if (existsIf) {
            failedTokens.add(new FailedTokens(token, "if syntax fail"));
            endRecovery(token);
            flag = true;
        } else {
            parent.removeChild(node);
            tokenIndex = fallbackIndex;
        }

        return flag;
    }

    private boolean parseIfBlock(Node parent) {

        int fallbackIndex = tokenIndex;

        Node node = addNewNonTerminalToParent(parent,"ifblock");

        boolean isValid = checkTerminal(node,"begin") && parseSts(node) && parseElseBlock(node) && checkTerminal(node,"end");

        if (isValid) return true;
        parent.removeChild(node);

        tokenIndex = fallbackIndex;
        return false;
    }

    private boolean parseElseBlock(Node parent) {

        Node node = addNewNonTerminalToParent(parent,"elseblock");

        boolean isValid = elseBlock1(node) || nullTerminal();

        if (isValid) return true;
        parent.removeChild(node);
        return false;
    }

    private boolean elseBlock1(Node parent) {

        int fallbackIndex = tokenIndex;

        boolean isValid = checkTerminal(parent,"else") && parseSts(parent) && parseElseBlock(parent);

        if (isValid) return true;
        tokenIndex = fallbackIndex;
        return false;
    }

    private boolean parseBlock(Node parent) {

        int fallbackIndex = tokenIndex;

        Node node = new Node(parent.getChilds().size() + 1, "block", null);
        parent.addChild(node);

        boolean isValid = checkTerminal(node, "begin") && parseSts(node) && checkTerminal(node, "end");

        if (isValid) return true;
        parent.removeChild(node);
        tokenIndex = fallbackIndex;
        return false;
    }

    private boolean parseWhileSt(Node parent) {


        int fallbackIndex = tokenIndex;
        Token token = null;
        if (tokenIndex < tokens.size()) {
            token = tokens.get(tokenIndex);
        }

        Node node = addNewNonTerminalToParent(parent, "while");

        boolean whileExists = checkTerminal(node, "while");
        boolean isValid = whileExists && checkTerminal(node, "(") && parseBooleanExp(node) && checkTerminal(node, ")") && parseBlock(node);

        if (isValid) return true;
        else if (whileExists) {
            failedTokens.add(new FailedTokens(token, "while syntax fail"));
            endRecovery(token);
        } else {
            parent.removeChild(node);
            tokenIndex = fallbackIndex;
        }
        return false;
    }

    private boolean parseAssignmentSt(Node parent) {

        Node node = addNewNonTerminalToParent(parent, "assigmentst");

        boolean isValid = assignment1(node) || assignment2(node);

        if (isValid) return true;
        parent.removeChild(node);
        return false;

    }

    private boolean assignment1(Node parent) {

        int fallbackIndex = tokenIndex;

        boolean isValid = checkTerminal(parent, "ID") && checkTerminal(parent, "=") && parseArithmeticExp(parent) && checkTerminal(parent, ";");

        if (isValid) return true;
        tokenIndex = fallbackIndex;
        return false;
    }

    private boolean assignment2(Node parent) {

        int fallbackIndex = tokenIndex;

        boolean isValid = checkTerminal(parent, "ID") && checkTerminal(parent, ";");

        if (isValid) return true;
        tokenIndex = fallbackIndex;
        return false;
    }

    private boolean parseReturnSt(Node parent) {

        Node node = addNewNonTerminalToParent(parent, "returnst");

        boolean isValid = returnSt1(node) || returnSt2(node);

        if (isValid) return true;
        parent.removeChild(node);
        return false;
    }

    private boolean returnSt1(Node parent) {

        int fallbackIndex = tokenIndex;

        boolean isValid = checkTerminal(parent, "return") && parseArithmeticExp(parent) && checkTerminal(parent, ";");

        if (isValid) return true;
        tokenIndex = fallbackIndex;
        return false;
    }

    private boolean returnSt2(Node parent) {

        int fallbackIndex = tokenIndex;

        boolean isValid = checkTerminal(parent, "return") && checkTerminal(parent, ";");

        if (isValid) return true;
        tokenIndex = fallbackIndex;
        return false;
    }


    private boolean parseBooleanExp(Node parent) {

        Node node = addNewNonTerminalToParent(parent, "booleanexp");

        boolean isValid = parseArithmeticExp(node) && parseBoolop(node) && parseArithmeticExp(node);

        if (isValid) return true;

        parent.removeChild(node);

        return false;
    }

    private boolean parseBoolop(Node parent) {

        int fallbackIndex = tokenIndex;
        Node node = addNewNonTerminalToParent(parent, "boolop");

        boolean isValid = checkTerminal(node, ">") || checkTerminal(node, "<") || checkTerminal(node, "==") || checkTerminal(node, "<=") || checkTerminal(node, ">=") || checkTerminal(node, "!=");

        if (isValid) return true;
        tokenIndex = fallbackIndex;
        parent.removeChild(node);
        return false;
    }

    private boolean parseFuncSt(Node parent) {


        Node node = new Node(parent.getChilds().size() + 1, "funcst", null);
        root.addChild(node);

        int fallbackIndex = tokenIndex;
        Token token = null;
        if (tokenIndex < tokens.size()) {
            token = tokens.get(tokenIndex);
        }
        boolean flag = false;

        boolean funcExists = checkTerminal(node, "func");
        boolean isValid = funcExists && (funcst2(node) || funcst1(node));

        if (isValid) return true;
        else if (funcExists) {
            failedTokens.add(new FailedTokens(token, "function syntax fail"));
            endRecovery(token);
            flag = true;
        } else {
            tokenIndex = fallbackIndex;
            parent.removeChild(node);
        }
        return flag;
    }

    private boolean funcst2(Node parent) {

        int fallbackIndex = tokenIndex;
        boolean isValid = checkTerminal(parent, "ID") && checkTerminal(parent, "(") && checkTerminal(parent, ")") && checkTerminal(parent, ":") && parseType(parent) && parseBlock(parent);

        if (isValid) return true;
        tokenIndex = fallbackIndex;
        return false;

    }

    private boolean funcst1(Node parent) {
        int fallbackIndex = tokenIndex;

        boolean isValid = checkTerminal(parent, "ID") && checkTerminal(parent, "(") && parseParams(parent) && checkTerminal(parent, ")") && checkTerminal(parent, ":") && parseType(parent) && parseBlock(parent);

        if (isValid) return true;

        tokenIndex = fallbackIndex;
        return false;

    }

    private boolean parseParams(Node parent) {

        Node node = new Node(parent.getChilds().size() + 1, "params", null);
        parent.addChild(node);

        boolean isValid = params1(node) || parseFuncDecl(node);

        if (isValid) return true;
        parent.removeChild(node);
        return false;
    }

    private boolean params1(Node parent) {

        int fallbackIndex = tokenIndex;

        boolean isValid = parseFuncDecl(parent) && checkTerminal(parent, ",") && parseParams(parent);

        if (isValid) return true;
        tokenIndex = fallbackIndex;
        return false;
    }

    private boolean parseFuncDecl(Node parent) {

        Node node = addNewNonTerminalToParent(parent, "funcdecl");

        boolean isValid = funcDecl(node, "int") || funcDecl(node, "float") || funcDecl(node, "bool") || funcDecl(node, "char");

        if (isValid) return true;
        parent.removeChild(node);
        return false;
    }

    private boolean parseDecl(Node parent) {

        Node node = addNewNonTerminalToParent(parent, "decl");

        boolean isValid = decl(node, "int") || decl(node, "float") || decl(node, "bool") || decl(node, "char");

        if (isValid) return true;
        parent.removeChild(node);
        return false;
    }

    private boolean decl(Node parent, String type) {
        int fallbackIndex = tokenIndex;

        boolean isValid = checkTerminal(parent, type) && parseAssignmentSt(parent);

        if (isValid) return true;
        tokenIndex = fallbackIndex;

        return false;
    }


    private boolean funcDecl(Node parent, String type) {

        int fallbackIndex = tokenIndex;

        Node node = new Node(parent.getChilds().size() + 1, "funcdecl", null);
        parent.addChild(node);
        boolean isValid = checkTerminal(node, type) && checkTerminal(node, "ID");

        if (isValid) return true;
        parent.removeChild(node);
        tokenIndex = fallbackIndex;
        return false;
    }

    private boolean parseType(Node parent) {

        Node node = new Node(parent.getChilds().size() + 1, "type", null);
        parent.addChild(node);

        boolean isValid = checkTerminal(node, "int") || checkTerminal(node, "float") || checkTerminal(node, "void") || checkTerminal(node, "bool") || checkTerminal(node, "char");

        if (!isValid) {
            node.removeChild(node);
        }
        return isValid;
    }

    private boolean parseArithmeticExp(Node parent) {

        Node node = addNewNonTerminalToParent(parent, "arithmeticexp");

        boolean isValid = parseArithmetic1(node) || parseArithmetic2(node) || parseArithmetic3(node) || parseArithmetic4(node) || parseArithmetic5(node) || parseMultExp(node);
        if (isValid) return true;
        parent.removeChild(node);
        return false;
    }

    private boolean parseArithmetic1(Node parent) {

        int fallbackIndex = tokenIndex;
        boolean isValid = parseMultExp(parent) && checkTerminal(parent, "and") && parseArithmeticExp(parent);
        if (isValid) return true;
        tokenIndex = fallbackIndex;
        return false;
    }

    private boolean parseArithmetic2(Node parent) {
        int fallbackIndex = tokenIndex;
        boolean isValid = parseMultExp(parent) && checkTerminal(parent,"or") && parseArithmeticExp(parent);
        if (isValid) return true;
        tokenIndex = fallbackIndex;
        return false;
    }

    private boolean parseArithmetic3(Node parent) {
        int fallbackIndex = tokenIndex;
        boolean isValid = parseMultExp(parent) && checkTerminal(parent,"-") && parseArithmeticExp(parent);
        if (isValid) return true;
        tokenIndex = fallbackIndex;
        return false;
    }

    private boolean parseArithmetic4(Node parent) {
        int fallbackIndex = tokenIndex;
        boolean isValid = parseMultExp(parent) && checkTerminal(parent,"+") && parseArithmeticExp(parent);
        if (isValid) return true;
        tokenIndex = fallbackIndex;
        return false;
    }

    private boolean parseArithmetic5(Node parent) {
        int fallbackIndex = tokenIndex;

        boolean isValid = parseMultExp(parent) && checkTerminal(parent,"%") && parseArithmeticExp(parent);
        if (isValid) return true;
        tokenIndex = fallbackIndex;
        return false;
    }

    private boolean parseFuncCallParams(Node parent) {

        Node node = addNewNonTerminalToParent(parent, "funccallparams");

        boolean isValid = funcCallParams1(node) || parseSimpleExp(node);
        if (isValid) return true;
        parent.removeChild(node);
        return false;
    }

    private boolean funcCallParams1(Node parent) {

        int fallbackIndex = tokenIndex;

        boolean isValid = parseSimpleExp(parent) && checkTerminal(parent, ",") && parseFuncCallParams(parent);

        if (isValid) return true;
        tokenIndex = fallbackIndex;
        return false;
    }

    private boolean parseMultExp(Node parent) {
        Node node = addNewNonTerminalToParent(parent, "multexp");
        boolean isValid = parseMultExp1(node) || parseMultExp2(node) || parseSimpleExp(node);
        if (isValid) return true;
        parent.removeChild(node);
        return false;
    }

    private boolean parseMultExp1(Node parent) {
        int fallbackIndex = tokenIndex;

        boolean isValid = parseSimpleExp(parent) && checkTerminal(parent, "*") && parseMultExp(parent);
        if (isValid) return true;
        tokenIndex = fallbackIndex;
        return false;
    }

    private boolean parseMultExp2(Node parent) {

        int fallbackIndex = tokenIndex;
        boolean isValid = parseSimpleExp(parent) && checkTerminal(parent, "/") && parseMultExp(parent);
        if (isValid) return true;
        tokenIndex = fallbackIndex;
        return false;
    }

    private boolean parseSimpleExp(Node parent) {
        int fallbackIndex = tokenIndex;
        Node node = addNewNonTerminalToParent(parent, "simpleexp");

        boolean isValid = parseFuncCallExp(node) || checkTerminal(node, "ID") || checkTerminal(node, "INTNUM") || checkTerminal(node, "FLOATNUM") || checkTerminal(node, "BOOLVAL")
                || checkTerminal(node, "CHARACTER") || parseSimpleExp1(node);
        if (isValid) return true;
        parent.removeChild(node);
        tokenIndex = fallbackIndex;
        return false;
    }

    private boolean parseSimpleExp1(Node parent) {
        int fallbackIndex = tokenIndex;
        boolean isValid = checkTerminal(parent, "(") && parseArithmeticExp(parent) && checkTerminal(parent, ")");
        if (isValid) return true;
        tokenIndex = fallbackIndex;
        return false;
    }

    private boolean parseFuncCallExp1(Node parent) {
        int fallbackIndex = tokenIndex;
        boolean isValid = checkTerminal(parent, "ID") && checkTerminal(parent, "(") && parseFuncCallParams(parent) && checkTerminal(parent, ")");
        if (isValid) return true;
        tokenIndex = fallbackIndex;
        return false;
    }

    private boolean parseFuncCallExp2(Node parent) {
        int fallbackIndex = tokenIndex;
        boolean isValid = checkTerminal(parent, "ID") && checkTerminal(parent, "(") && checkTerminal(parent, ")");
        if (isValid) return true;
        tokenIndex = fallbackIndex;
        return false;
    }

    private boolean parseFuncCallExp(Node parent) {
        Node node = addNewNonTerminalToParent(parent, "funccalexp");
        boolean isValid = parseFuncCallExp1(node) || parseFuncCallExp2(node);
        if (isValid) return true;
        parent.removeChild(node);
        return false;
    }
}
