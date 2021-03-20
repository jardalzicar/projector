import org.json.simple.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;


public class Project {

    private Controller controller;
    private Config config;
    private List<Component> components;

    private String projectRoot;
    private String projectDirName;
    private String projectPath;

    private String defaultProjectRoot = "./";

    // Project attributes
    private String name;
    private String description;
    private String version;
    private String author;
    private String dateCreated;
    private String dateLastSaved;
    private String state;

    String defaultVersion = "0.0.1";
    String defaultState = "created";

    //TODO Delete files after unsuccessful open
    // Versions - tagging
    // Change eagle file name
    // To do list
    // GUI


    /**
     * Constructor - create new project
     */
    public Project(Controller controller, String name, String description, String author, List<Component> components) {

        this.components = components;
        this.controller = controller;
        this.name = name;
        this.description = description;
        this.projectRoot = defaultProjectRoot;
        this.author = author;
        this.version = defaultVersion;
        this.state = defaultState;
        this.dateCreated = new SimpleDateFormat("dd.MM.yyyy").format(new Date());
        this.dateLastSaved = dateCreated;

        // Create directory name
        projectDirName = name.toLowerCase().trim().replaceAll("\\s","_");
        projectPath = projectRoot + projectDirName + "/";

        // Create project directory
        File projDir = new File(projectPath);
        if(!projDir.exists()){
            if(!projDir.mkdir()){
                controller.errorExit("Unable to create project directory");
            }
        }

        // Create config file
        config = new Config();
        config.setProject(this);
        config.setDefaultPath();
        if(!config.createFiles()){
            controller.errorExit("Unable to create config file");
        }

        // Init components
        for(Component c : this.components){
            c.setProject(this);
            c.setDefaultPath();
            if (!c.createFiles()) {
                controller.errorExit("Unable to create part component: " + c.componentName);
            }
            c.initFiles();
        }

        // Init git repo
        GitRepository gitRepository = getGitRepository();
        if(gitRepository != null){
            if(!gitRepository.createRepo()){
                controller.errorExit("Unable to create Github repo");
            }
        }

        // Save config
        saveJSONConfig();

        // GIT commit
        if(gitRepository != null){
            if(!gitRepository.commit("Created initial project files")){
                controller.errorExit("Unable to create first commit");
            }
        }
    }


    /**
     * Constructor - open existing project from within project folder
     */
    public Project(Controller controller){

        this.controller = controller;
        projectPath = defaultProjectRoot;

        // Init config file
        config = new Config();
        config.setProject(this);
        config.setDefaultPath();
        if(!config.createFiles()){
            controller.errorExit("Unable to create config file");
        }

        // Parse JSON from config file
        JSONObject jo = config.readJsonConfig();
        if(jo == null){
            controller.errorExit("Error parsing config file");
        }

        // Apply JSON config to project
        applyJSONConfig(jo);
    }


    /**
     * Constructor - open existing project at specified path
     */
    public Project(Controller controller, String path){

        this.controller = controller;
        projectPath = path + "/";

        // Init config file
        config = new Config();
        config.setProject(this);
        config.setDefaultPath();
        if(!config.createFiles()){
            controller.errorExit("Unable to create config file");
        }

        // Parse JSON from config file
        JSONObject jo = config.readJsonConfig();
        if(jo == null){
            controller.errorExit("Error parsing config file");
        }

        // Apply JSON config to project
        applyJSONConfig(jo);
    }


    /**
     * Wraps project config to JSON object
     * @return JSON object with project config
     */
    public JSONObject extractJSONConfig(){

        JSONObject jo = new JSONObject();
        jo.put("name", this.name);
        jo.put("description", this.description);
        jo.put("author", this.author);
        jo.put("version", this.version);
        jo.put("dateCreated", this.dateCreated);
        jo.put("state", this.state);
        jo.put("dateLastSaved", this.dateLastSaved);

        for(Component c : components){
            if(c.relativePath.isEmpty()){
                jo.put(c.componentName, "true");
            }
            else{
                jo.put(c.componentName, c.relativePath);
            }
        }

        return jo;
    }


    public void saveJSONConfig(){
        config.writeJsonConfig(this.extractJSONConfig());
    }


    /**
     * Set project config according to JSON object
     * @return true if operation was successful
     */
    public void applyJSONConfig(JSONObject jo){

        // Get values from JSON object
        String tmpName = (String) jo.get("name");
        String tmpDescription = (String) jo.get("description");
        String tmpAuthor = (String) jo.get("author");
        String tmpVersion = (String) jo.get("version");
        String tmpDateCreated = (String) jo.get("dateCreated");
        String tmpState = (String) jo.get("state");
        String tmpDateLastSaved = (String) jo.get("dateLastSaved");

        // Check values from JSON object
        if(tmpName == null || tmpDescription == null || tmpVersion == null ||
                tmpAuthor == null || tmpDateCreated == null || tmpState == null || tmpDateLastSaved == null){
            controller.errorExit("Some attributes were not found in config file");
        }

        // Assign attribures
        name = tmpName;
        description = tmpDescription;
        author = tmpAuthor;
        version = tmpVersion;
        dateCreated = tmpDateCreated;
        state = tmpState;
        dateLastSaved = tmpDateLastSaved;

        // Components
        components = new ArrayList<Component>();
        List<Component> tmpComponents = new ArrayList<Component>(Arrays.asList(
                new ExcelPartList(), new Readme(), new TodoList(), new GitRepository(), new Eagle()
        ));

        for(Component c: tmpComponents){
            String cJsonString = (String) jo.get(c.componentName);
            if(cJsonString != null){
                if(cJsonString.equals(c.defaultPath) || cJsonString.equals("true")){
                    components.add(c);
                }
                else{
                    controller.errorExit("Config file contains wrong path for " + c.componentName);
                }
            }
        }

        // Init components
        for(Component c : components){
            c.setProject(this);
            c.setDefaultPath();
            if (!c.createFiles()) {
                controller.errorExit("Unable to create component: " + c.componentName);
            }
        }

        // Init git repo
        GitRepository gitRepository = getGitRepository();
        if(gitRepository != null){
            if(!gitRepository.openRepo()){
                controller.errorExit("Unable to create Github repo");
            }
        }
    }


    public void printConfig(){
        //config.printJson(config.readJsonConfig());
        System.out.println("");
        System.out.println("PROJECT " + name);
        System.out.println("===============================");
        System.out.println("Description: " + description);
        System.out.println("Version: " + version);
        System.out.println("Author: " + author);
        System.out.println("Date created: " + dateCreated);
        System.out.println("Date last saved: " + dateLastSaved);
        System.out.println("State: " + state);

        System.out.println("Components: ");
        for(Component c : this.components){
            if(c.relativePath.isEmpty()){
                System.out.println("   " + c.componentName + ": true");
            }
            else{
                System.out.println("   " + c.componentName + ": " + c.relativePath);
            }
        }
        System.out.println("");
    }


    public void changeVersion(String newVersion){
        String oldVersion = version;
        version = newVersion;
        saveJSONConfig();

        Readme readme = getReadme();
        if(readme != null) {
            readme.updateVersion(newVersion);
        }
    }


    public void changeName(String newName){
        String oldName = name;
        name = newName;
        saveJSONConfig();

        Readme readme = getReadme();
        if(readme != null) {
            readme.updateName(newName);
        }

        TodoList todoList = getTodoList();
        if(todoList != null) {
            todoList.updateName(newName);
        }
    }


    public void changeDescription(String newDescription){
        String oldDescription = description;
        description = newDescription;
        saveJSONConfig();

        Readme readme = getReadme();
        if(readme != null) {
            readme.updateDescription(newDescription);
        }
    }


    public void changeAuthor(String newAuthor){
        String oldAuthor = author;
        author = newAuthor;
        saveJSONConfig();

        Readme readme = getReadme();
        if(readme != null) {
            readme.updateAuthor(newAuthor);
        }
    }


    public void changeState(String newState){
        String oldState = state;
        state = newState;
        saveJSONConfig();

        Readme readme = getReadme();
        if(readme != null) {
            readme.updateState(newState);
        }
    }


    public void updateDateLastSaved(){
        String oldDateLastSaved = dateLastSaved;
        dateLastSaved = new SimpleDateFormat("dd.MM.yyyy").format(new Date());
        saveJSONConfig();

        Readme readme = getReadme();
        if(readme != null) {
            readme.updateDateLastSaved(dateLastSaved);
        }
    }


    // Getters and setters
    // ----------------------------------------------------------------------------------

    public Readme getReadme(){
        for (Component c: components) {
            if( c instanceof Readme){
                return (Readme) c;
            }
        }
        return null;
    }

    public TodoList getTodoList(){
        for (Component c: components) {
            if( c instanceof TodoList){
                return (TodoList) c;
            }
        }
        return null;
    }

    public GitRepository getGitRepository(){
        for (Component c: components) {
            if( c instanceof GitRepository){
                return (GitRepository) c;
            }
        }
        return null;
    }

    public String getProjectPath() {
        return projectPath;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getVersion() {
        return version;
    }

    public String getAuthor() {
        return author;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public String getDateLastSaved() {
        return dateLastSaved;
    }

    public String getState() {
        return state;
    }

    public List<Component> getComponents() {
        return components;
    }
}
