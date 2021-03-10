import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Controller {

    private UserInterface userInterface;

    public Controller(UserInterface userInterface) {
        this.userInterface = userInterface;
    }

    public static void main(String[] args) {

        UserInterface ui = new UserInterface();
        Controller controller = new Controller(ui);

        if(args.length > 0){
            switch (args[0]){
                case "-h":
                    controller.showUsage();
                    break;
                case "-v":
                    controller.showVersion();
                    break;
                case "create":
                    controller.createNewProject();
                    break;
                case "create-test":
                    controller.createNewProjectTest();
                    break;
                case "info":
                    controller.info();
                    break;
                case "save":
                    controller.save(args);
                    break;
                case "version":
                    controller.newVersion(args);
                    break;
                case "clone":
                    //controller.openProjectUI(args);
                    break;
                case "change":
                    controller.change(args);
                    break;
                default:
                    System.out.println("Unknown argument");
                    controller.showUsage();
            }
        }
        else{
            System.out.println("Too few arguments");
            controller.showUsage();
        }
    }


    public void showUsage(){
        userInterface.showUasge();
    }

    public void showVersion(){
        userInterface.showVersion();
    }

    public void createNewProjectTest(){

        String projectName = "Test project";
        String projectDescription = "This is a test project";
        String author = "Jaroslav Lzicar";

        List<Component> components = new ArrayList<Component>();

        components.add(new ExcelPartList());
        components.add(new Readme());
        components.add(new GitRepository());
        components.add(new Eagle());

        Project project = new Project(this, projectName, projectDescription, author, components);

        System.out.println("Project created successfully!");
        project.printConfig();
    }

    public void createNewProject(){
        Scanner sc = new Scanner(System.in);

        // Get user input
        System.out.println("Enter project name");
        String projectName = sc.nextLine();

        System.out.println("Enter project description");
        String projectDescription = sc.nextLine();

        System.out.println("Enter author name");
        String author = sc.nextLine();

        List<Component> components = new ArrayList<Component>();

        System.out.println("Do you want to create MS Excel part list? Type \"Y\" for yes, \"N\" for no.");
        if(userInterface.getYesNoInput()){
            components.add(new ExcelPartList());
        }

        System.out.println("Do you want to create README document? Type \"Y\" for yes, \"N\" for no.");
        if(userInterface.getYesNoInput()){
            components.add(new Readme());
        }

        System.out.println("Do you want to create Eagle project? Type \"Y\" for yes, \"N\" for no.");
        if(userInterface.getYesNoInput()){
            components.add(new Eagle());
        }

        System.out.println("Do you want to create local GIT repository? Type \"Y\" for yes, \"N\" for no.");
        if(userInterface.getYesNoInput()){
            components.add(new GitRepository());
        }

        // Create project
        Project project = new Project(this, projectName, projectDescription, author, components);

        System.out.println("Project created successfully!");
        project.printConfig();
    }

    public void info(){
        Project project = new Project(this);
        System.out.println("Project opened successfully!");
        project.printConfig();
    }

    public void newVersion(String[] args){
        Project project = new Project(this);
        GitRepository gr = project.getGitRepository();

        if(gr != null){
            if(args.length >= 2){
                if(gr.createVersion(args[1])){
                    System.out.println("New version created successfully!");
                }
                else{
                    errorExit("Failed to create new version");
                }
            }
            else{
                System.out.println("Too few arguments");
                showUsage();
            }
        }
        else{
            errorExit("Project does not have GIT repository");
        }
    }

    public void save(String[] args){
        Project project = new Project(this);
        GitRepository gr = project.getGitRepository();

        if(gr != null){
            if(args.length >= 2){
                if(gr.commit(args[1])){
                    System.out.println("Project saved successfully!");
                }
                else{
                    errorExit("Failed to save project");
                }
            }
            else{
                if(gr.commit("User saved")){
                    System.out.println("Project saved successfully!");
                }
                else{
                    errorExit("Failed to save project");
                }
            }
        }
        else{
            errorExit("Project does not have GIT repository");
        }
    }

    public void change(String[] args){
        if(args.length >= 3){
            String property = args[1];
            String value = args[2];
            Project project = new Project(this);

            switch (property){
                case "name":
                    project.changeName(value);
                    break;
                case "description":
                    project.changeDescription(value);
                    break;
                case "author":
                    project.changeAuthor(value);
                    break;
                default:
                    errorExit("Unknown property: " + property);
            }
        }
        else{
            System.out.println("Too few arguments");
            showUsage();
        }
    }



    // Other
    // ---------------------------------------------------------------------

    public void openProject(){
        Project project = new Project(this);
    }

    public void errorExit(String errorMsg){
        System.out.println("Error: " + errorMsg);
        System.out.println("Exitting...");
        System.exit(1);
    }

    public void exit(){
        System.exit(0);
    }

}
