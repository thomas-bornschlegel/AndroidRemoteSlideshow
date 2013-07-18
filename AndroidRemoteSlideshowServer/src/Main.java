/**
 * Main class to start the server.
 * 
 * @author Thomas Bornschlegel
 * 
 */
public class Main {

    public static void main(String[] args) {

        /* @formatter:off
         * 
         * TODO:
         * 
         * Add Args parsing:
         * 
         * java Main --port 1235 --imagedir C:\\uni
         * 
         * Started Server on port 1235...
         * Press Enter to display the next image: 1.png
         * Displaying: 1.png
         * Press Enter to display the next image: 2.png
         * ...
         * */
        
        //XXX just for testing
        
        boolean error = false;
        
        if(args.length<4){
            String defaultDir = "/home/thomas/nook/stimuli_new";
            String[] myArgs = {"--port", "5060", "--imagedir", defaultDir};
            args = myArgs;
            System.out.println("No arguments given. Starting with default arguments: java Main --port 5060 --imagedir "+defaultDir);
        } 
        try {
            int port = Integer.valueOf(args[1]);
            String dir = args[3];
            
            Connector connector = new Connector(port, dir);
        } catch (Exception e) {
            error = true;
            System.out.println("Wrong arguments given. Please start with no arguments or arguments of the form: java Main --port 5060 --imagedir C:\\uni_bamberg");
            e.printStackTrace();
        }
        
        if(error==true){
            System.err.println("An error occurred => Exit.");
        }
    }
}