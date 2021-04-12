import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;

public class Grammar {
    Map<String, ArrayList<String>> cfg;
    GrammarHelper grammarHelper;
    ArrayList<ArrayList<Token>> pool;

    public Grammar(String fileName) {
        cfg = new HashMap<>();
        grammarHelper = new GrammarHelper(fileName);
        pool = new ArrayList<>();
        constructGrammar();
    }

    public void constructGrammar() {
        String currentLine;
        while ((currentLine = grammarHelper.getNextLine()) != null) {
            String tempTerminalVariable = grammarHelper.getNonTerminalVariableFromString(currentLine);
            ArrayList<String> tempNonTerminals = grammarHelper.getDerivationsFromString(currentLine);
            cfg.put(tempTerminalVariable, tempNonTerminals);
        }
        System.out.println();
        unrollNullNonTerminals();
    }

    public boolean doesHaveNullTerminal(ArrayList<String> terminals) {
        return terminals.stream().anyMatch(terminal -> terminal.equals("$"));
    }

    public void traverseAndReplaceNulls(String nonTerminal) {

        for (Map.Entry<String, ArrayList<String>> entry : cfg.entrySet()) {
            int tempNonTerminalSize = entry.getValue().size();
            ArrayList<String> tempNonTerminals = entry.getValue();
            for (int i = 0; i < tempNonTerminalSize; i++) {
                String[] splittedBySpace = tempNonTerminals.get(i).split(" ");
                int occurence = 0;
                for (String currentString : splittedBySpace) {
                    if (currentString.equals(nonTerminal)) {
                        StringBuilder stringBuilder = new StringBuilder();
                        int index = tempNonTerminals.get(i).indexOf(nonTerminal);
                        if (occurence == 1) {
                            index = tempNonTerminals.get(i).lastIndexOf(nonTerminal);
                            tempNonTerminals.add(tempNonTerminals.get(i).replaceAll(nonTerminal, ""));
                        }
                        stringBuilder.append(tempNonTerminals.get(i).substring(0, index)).append(tempNonTerminals.get(i).substring(index + nonTerminal.length()));
                        tempNonTerminals.add(stringBuilder.toString());
                        occurence++;
                        if (occurence == 2) {
                            break;
                        }
                    }
                }
            }
        }
    }

    public void cleanTerminals() {
        for (Map.Entry<String, ArrayList<String>> entry : cfg.entrySet()) {
            int tempTerminalSize = entry.getValue().size();
            ArrayList<String> tempTerminals = entry.getValue();
            for (int i = 0; i < tempTerminalSize; i++) {
                String tempCleaned = tempTerminals.get(i).replaceAll(" ", "");
                if (tempCleaned.length() != 0) {
                    tempTerminals.set(i, tempCleaned);
                } else {
                    tempTerminals.remove(i);
                }
            }
        }
    }

    public void unrollNullNonTerminals() {
        for (Map.Entry<String, ArrayList<String>> entry : cfg.entrySet()) {
            if (doesHaveNullTerminal(entry.getValue())) {
                traverseAndReplaceNulls(entry.getKey());
            }
        }
        cleanTerminals();
    }

    public void printGrammar() {
        for (Map.Entry<String, ArrayList<String>> entry : cfg.entrySet()) {
            System.out.println(entry.getKey() + ":" + entry.getValue().toString());
        }
    }

    public boolean getPossibleTerminals(String tokenSet, ArrayList<Token> tokens, int startIndex, int endIndex) {
        boolean flag = false;
        for (Map.Entry<String, ArrayList<String>> entry : cfg.entrySet()) {
            int tempTerminalSize = entry.getValue().size();
            ArrayList<String> tempTerminals = entry.getValue();
            int counter = 0;
            for (int i = 0; i < tempTerminalSize; i++) {
                if (tempTerminals.get(i).equals(tokenSet)) {
                    if (counter > 0) {
                        ArrayList<Token> temp = new ArrayList<>(tokens);
                        for (int j = startIndex; j < endIndex; j++) {
                            temp.remove(startIndex);
                        }
                        Token token = new Token(TokenType.ID, entry.getKey());
                        temp.add(startIndex, token);
                        pool.add(temp);
                    } else {
                        for (int j = startIndex; j < endIndex; j++) {
                            tokens.remove(startIndex);
                        }
                        Token token = new Token(TokenType.ID, entry.getKey());
                        tokens.add(startIndex, token);
                    }
                    if (entry.getKey().equals("source")) {
                        System.out.println();
                    }
                    flag = true;
                    counter++;
                }
            }
        }
        return flag;
    }

    public void check(ArrayList<Token> tokens) {
        ArrayList<String>[] parseArray = new ArrayList[tokens.size()];
        pool.add(tokens);
        int counter = 0;

        int poolIndex = 0;
        while (pool.size() != 0) {
            boolean isChanged = false;
            int tokenSize = pool.get(poolIndex).size();
            for (int k = 0; k < tokenSize; k++) {
                for (int i = 0; i < tokenSize - k; i++) {
                    StringBuilder stringBuilder = new StringBuilder();
                    for (int j = 0; j <= k; j++) {
                        stringBuilder.append(pool.get(poolIndex).get(i + j).value);
                    }
                    isChanged = getPossibleTerminals(stringBuilder.toString(), pool.get(poolIndex), i, i + k + 1);
                }
            }
            if (isChanged) {
                pool.remove(poolIndex);
            }
            poolIndex = (poolIndex + 1) % pool.size();
        }

    }

}
