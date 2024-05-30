public class Main {
    public static void main(String[] args) {
        if (args.length > 0 && args[0].equals("interactive")) {
            new InteractiveGUI();
        } else {
            new GUI();
        }
    }
}