import java.util.List;


public class TodoList extends Component {

    private String templateFileName = "todo_template.md";
    private String defaultText;


    public TodoList() {
        super();
        defaultPath = "TODO.md";
        componentName = "TodoList";
    }

    @Override
    void initFiles() {
        copyResourceFile(templateFileName, filePath);
        updateName(project.getName());
    }

    boolean writeTodo(String text){
        return writeToFile(filePath, text);
    }



    boolean replaceText(String oldText, String newText){
        boolean result;

        String readMeContent = readFromFile(filePath);
        if(readMeContent != null){
            readMeContent = readMeContent.replaceFirst(oldText, newText);
            result = writeTodo(readMeContent);
        }
        else{
            result = false;
        }
        return result;
    }

    boolean updateName(String newName){
        String regex = "^#\\s.+";
        newName = "# TODO list - " + newName;
        return replaceText(regex, newName);
    }


}
