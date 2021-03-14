import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Readme extends Component {

    private String templateFileName = "readme_template2.md";

    private String par =
            "One to two paragraph statement about your product and what it does. " +
            "A few motivating and useful examples of how your product can be used. " +
            "Spice this up with code blocks and potentially more screenshots.";
    private String defaultText;


    public Readme() {
        super();
        defaultPath = "README.md";
        componentName = "ReadMe";
    }

    @Override
    void initFiles() {
        copyResourceFile(templateFileName, filePath);

        updateName(project.getName());
        updateDescription(project.getDescription());
        updateAuthor(project.getAuthor());
        updateDateCreated(project.getDateCreated());
        updateDateLastSaved(project.getDateLastSaved());
        updateState(project.getState());
        updateVersion(project.getVersion());
        updateComponents(project.getComponents());
    }

    boolean writeReadMe(String text){
        return writeToFile(filePath, text);
    }



    boolean replaceText(String oldText, String newText){
        boolean result;

        String readMeContent = readFromFile(filePath);
        if(readMeContent != null){
            readMeContent = readMeContent.replaceFirst(oldText, newText);
            result = writeReadMe(readMeContent);
        }
        else{
            result = false;
        }
        return result;
    }

    boolean updateName(String newName){
        String regex = "^#\\s.+";
        newName = "# " + newName;
        return replaceText(regex, newName);
    }

    boolean updateDescription(String newDescription){
        String regex = "\\*.+\\*";
        newDescription = "*" + newDescription + "*";
        return replaceText(regex, newDescription);
    }

    boolean updateAuthor(String newDescription){
        String regex = "-\\sAuthor:\\s.+";
        newDescription = "- Author: " + newDescription;
        return replaceText(regex, newDescription);
    }

    boolean updateDateCreated(String newDescription){
        String regex = "-\\sCreated:\\s.+";
        newDescription = "- Created: " + newDescription;
        return replaceText(regex, newDescription);
    }

    boolean updateVersion(String newDescription){
        String regex = "-\\sVersion:\\s.+";
        newDescription = "- Version: " + newDescription;
        return replaceText(regex, newDescription);
    }

    boolean updateState(String newState){
        String regex = "-\\sState:\\s.+";
        newState = "- State: " + newState;
        return replaceText(regex, newState);
    }

    boolean updateDateLastSaved(String newDescription){
        String regex = "-\\sLast saved:\\s.+";
        newDescription = "- Last saved: " + newDescription;
        return replaceText(regex, newDescription);
    }

    boolean updateComponents(List<Component> components){
        String regex = "-\\sComponents:\\s.+";
        String newComponents = "- Components: ";
        for (Component c : components){
            newComponents += c.componentName + " ";
        }
        return replaceText(regex, newComponents);
    }



}
