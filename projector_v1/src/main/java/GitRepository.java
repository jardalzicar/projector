import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.File;
import java.io.IOException;

public class GitRepository extends Component{

    private Git git;

    public GitRepository(){
        super();
        defaultPath = "";
        componentName = "GitRepo";
    }

    @Override
    void initFiles() {
        // Do nothing
    }

    @Override
    public boolean createFiles() {
        return true;
    }

    public boolean createRepo(){
        try {
            // Create repo
            File projectdir = new File(filePath);
            git = Git.init().setDirectory(projectdir).call();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean openRepo(){
        try {
            git = Git.open(new File( filePath + ".git" ));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean createVersion(String newVersion){
        String oldVersion = project.getVersion();
        project.changeVersion(newVersion);
        if(commit("New version: " + newVersion)){
            return true;
        }
        else{
            project.changeVersion(oldVersion);
            return false;
        }
    }

    public boolean commit(String message){
        try {
            git.add().addFilepattern(".").call();
            git.commit().setMessage(message).call();
            this.project.updateDateLastSaved();
            return true;
        } catch (GitAPIException e) {
            return false;
        }
    }


    public void printGitStatus(){
        try {
            Status status = git.status().call();
            System.out.println("Added: " + status.getAdded());
            System.out.println("Changed: " + status.getChanged());
            System.out.println("Conflicting: " + status.getConflicting());
            //System.out.println("ConflictingStageState: " + status.getConflictingStageState());
            System.out.println("IgnoredNotInIndex: " + status.getIgnoredNotInIndex());
            System.out.println("Missing: " + status.getMissing());
            System.out.println("Modified: " + status.getModified());
            System.out.println("Removed: " + status.getRemoved());
            System.out.println("Untracked: " + status.getUntracked());
            System.out.println("UntrackedFolders: " + status.getUntrackedFolders());
        } catch (GitAPIException e) {
            System.out.println("Unable to print git status");
        }
    }


}
