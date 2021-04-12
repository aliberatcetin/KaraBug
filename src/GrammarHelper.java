import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

public class GrammarHelper extends Helper {

    public GrammarHelper(String fileName) {
        super(fileName);
    }

    public String getNonTerminalVariableFromString(String string) {
        return string.substring(0, string.indexOf("->"));
    }

    public ArrayList<String> getDerivationsFromString(String string) {
        String[] splited = string.substring(string.indexOf("->") + 2).split("\\|");
        return new ArrayList<>(Arrays.asList(splited));
    }

}
