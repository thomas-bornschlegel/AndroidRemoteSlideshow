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
         * java NookConnector --port 1235 --imagedir C:\\uni
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
            String[] myArgs = {"--port", "5060", "--imagedir", "/home/thomas/nook/stimuli_new"};
            args = myArgs;
            System.out.println("No arguments given. Starting with default arguments: java NookConnector --port 5060 --imagedir C:\\uni_bamberg");
        } 
        try {
            int port = Integer.valueOf(args[1]);
            String dir = args[3];
            
            Connector connector = new Connector(port, dir);
        } catch (Exception e) {
            error = true;
            System.out.println("Wrong arguments given. Please start with no arguments or the default arguments: java NookConnector --port 5060 --imagedir C:\\uni_bamberg");
            e.printStackTrace();
        }
        
        if(error==true){
            System.err.println("An error occurred => Exit.");
        }
    }
}