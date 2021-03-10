
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;



abstract class Component {

    protected Project project;
    protected String dirPath;
    protected String fileName;
    protected String filePath;
    protected String relativePath;
    protected String defaultPath;
    public String componentName;

    protected String ls = System.lineSeparator();

    public Component(){
    }

    public void setPath(String path){
        this.relativePath = path;
        this.filePath= project.getProjectPath() + path;
        this.fileName = filePath.substring(filePath.lastIndexOf("/") + 1);
        this.dirPath =  filePath.substring(0, filePath.lastIndexOf("/") + 1);
    }

    public void setDefaultPath(){
        this.relativePath = defaultPath;
        this.filePath= project.getProjectPath() + defaultPath;
        this.fileName = filePath.substring(filePath.lastIndexOf("/") + 1);
        this.dirPath =  filePath.substring(0, filePath.lastIndexOf("/") + 1);
    }

    /**
     * Create component file and directory (if they do not exist) and check if
     * file is readable
     * @return true if files were created and are readable
     */
    public boolean createFiles(){
        boolean result = true;

        // Create component directory
        File componentDir = new File(dirPath);
        if(!componentDir.exists()){
            if(!componentDir.mkdirs()){
                System.out.println("Unable to create directory");
                result = false;
            }
        }

        // Create component file
        if(result){
            result = createFile(filePath);
        }

        return result;
    }

    public boolean createFile(String filePath){
        boolean result = true;

        File componentFile = new File(filePath);
        if(!componentFile.exists()){
            try {
                componentFile.createNewFile();
            } catch (IOException e) {
                System.out.println("Unable to create component file");
                result = false;
            }
        }
        if(!componentFile.canWrite()){
            System.out.println("Error: component file not writeable");
            result = false;
        }
        return result;
    }

    public boolean copyResourceFile(String resourceName, String filePath){
        boolean result = false;
        File file = new File(filePath);

        InputStream in = getClass().getResourceAsStream(resourceName);
        try {
            Files.copy(in, Paths.get(filePath), StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (IOException e) {
            System.out.println("Unable to bit copy resource file");
            return false;
        }

        /*
        String resourceContent;
        try {
            resourceContent = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.out.println("Unable to locate resource");
            return false;
        }

        if(resourceContent != null){
            if(writeToFile(filePath, resourceContent)){
                result = true;
            }
            else{
                System.out.println("Unable to write resource content to file");
            }
        }
        else{
            System.out.println("Unable to read resource file");
        }
        return result;

         */
    }


    public boolean writeToFile(String path, String content){
        try {
            Files.writeString(Paths.get(path), content);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public String readFromFile(String path){
        try {
            String content = Files.readString(Paths.get(path), StandardCharsets.UTF_8);
            return content;
        } catch (IOException e) {
            return null;
        }
    }

    abstract void initFiles();

    public void setProject(Project project) {
        this.project = project;
    }

    public String getDirPath() {
        return dirPath;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getRelativePath() {
        return relativePath;
    }
}
