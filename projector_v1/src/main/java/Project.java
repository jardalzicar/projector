import org.json.simple.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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

    String defaultVersion = "0.0.1";

    //TODO Delete files after unsuccessful open
    // Versions - tagging
    // Change eagle file name


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
        this.dateCreated = new SimpleDateFormat("dd.MM.yyyy").format(new Date());

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
        // Attributes
        String tmpName = (String) jo.get("name");
        String tmpDescription = (String) jo.get("description");
        String tmpAuthor = (String) jo.get("author");
        String tmpVersion = (String) jo.get("version");
        String tmpDateCreated = (String) jo.get("dateCreated");
        // Components
        String plist = (String) jo.get("ExcelPartList");
        String readMe = (String) jo.get("ReadMe");
        String gitRepo = (String) jo.get("GitRepo");
        String eagle = (String) jo.get("Eagle");

        // Check values from JSON object
        if(tmpName == null || tmpDescription == null || tmpVersion == null ||
                tmpAuthor == null || tmpDateCreated == null){
            controller.errorExit("Some attributes were not found in config file");
        }

        // Assign attribures
        name = tmpName;
        description = tmpDescription;
        author = tmpAuthor;
        version = tmpVersion;
        dateCreated = tmpDateCreated;

        // Create components
        components = new ArrayList<Component>();
        if(!(plist == null)){
            ExcelPartList p = new ExcelPartList();
            if(!p.defaultPath.equals(plist)){
                controller.errorExit("config file contains wrong PartList path");
            }
            else{
                components.add(p);
            }
        }
        if(!(readMe == null)){
            Readme r = new Readme();
            if(!r.defaultPath.equals(readMe)){
                controller.errorExit("config file contains wrong ReadMe path");
            }
            else{
                components.add(r);
            }
        }
        if(!(eagle == null)){
            Eagle e = new Eagle();
            if(!e.defaultPath.equals(eagle)){
                controller.errorExit("config file contains wrong Eagle path");
            }
            else{
                components.add(e);
            }
        }
        if(!(gitRepo == null)){
            GitRepository gr = new GitRepository();
            components.add(gr);
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

    public List<Component> getComponents() {
        return components;
    }
}