import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Config extends Component{

    public Config() {
        super();
        defaultPath = "config.json";
        componentName = "Config";
    }

    @Override
    void initFiles() {
        // Do nothing
    }

    public JSONObject readJsonConfig(){
        JSONParser parser = new JSONParser();
        Object jo;
        try {
            FileReader fr = new FileReader(filePath);
            jo = parser.parse(fr);
            fr.close();
        } catch (IOException | ParseException e) {
            System.out.println("Error reading JSON from file");
            return null;
        }
        return (JSONObject) jo;
    }

    public boolean writeJsonConfig(JSONObject config){
        return writeToFile(filePath, config.toJSONString());
    }


    public void printJson(JSONObject config){
        System.out.println(config.toJSONString());
    }


}
