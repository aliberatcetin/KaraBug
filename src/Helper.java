import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class Helper {


    Scanner scanner;
    String fileName;

    public Helper(String fileName) {
        this.fileName=fileName;
        initFile();
    }

    public void initFile(){
        File file = new File(fileName);
        try {
            scanner=new Scanner(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public String removeWhiteSpaces(String string){
        return string.replaceAll("( )+", " ");
    }

    public String getNextLine(){
        if(scanner.hasNextLine()){
            return removeWhiteSpaces(scanner.nextLine());
        }else{
            return null;
        }
    }


}
