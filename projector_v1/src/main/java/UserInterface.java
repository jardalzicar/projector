import java.util.Scanner;

public class UserInterface {

    public UserInterface() {
    }

    public void showUasge(){
        System.out.println("Usage: $projector [-option]");
        System.out.println("  -v: shows program version");
        System.out.println("  -h: shows program help text");
        System.out.println("  create: creates new project");
        System.out.println("  open: opens existing project");
    }

    public void showVersion(){
        System.out.println("Projector version : 1.0");
        System.out.println("(c) Jaroslav Lzicar, 2020");
    }

    public boolean getYesNoInput(){
        Scanner sc = new Scanner(System.in);
        boolean result = false;

        String input = sc.nextLine();
        while (!(input.equals("Y") || input.equals("N"))){
            System.out.println("Invalid input, type either \"Y\" or \"N\"");
            input = sc.nextLine();
        }
        if (input.equals("Y")){
            result = true;
        }
        return result;
    }
}
