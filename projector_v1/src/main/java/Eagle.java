public class Eagle extends Component{

    private String epfDefaultText = "[Eagle]" + ls + "Version=\"09 06 02\"" + ls;
    private String schemeTemplateFileName = "scheme_template.sch";

    private String schemeFileName;
    private String schemeFilePath;


    public Eagle() {
        super();
        defaultPath = "eagle/eagle.epf";
        componentName = "Eagle";
    }

    @Override
    public boolean createFiles() {

        schemeFileName = project.getName().toLowerCase().trim().replaceAll("\\s","_") + ".sch";
        schemeFilePath = dirPath + schemeFileName;

        boolean result = super.createFiles();
        if(result){
            result = createFile(schemeFilePath);
        }
        return result;
    }

    @Override
    void initFiles() {
        // eagle.epf
        writeToFile(filePath, epfDefaultText);
        // project_name.sch
        copyResourceFile(schemeTemplateFileName, schemeFilePath);
    }

}
