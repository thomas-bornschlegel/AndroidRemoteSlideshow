import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Scanner;

/**
 * Used to establish a connection with the Android Client and to exchange data.
 * 
 * @author Thomas Bornschlegel
 * 
 */
public class Connector {

    private int port = 5060;
    private ArrayList<String> filesToDisplay;
    private ServerSocket serverSocket;
    private String dirName;
    private HashMap<Integer, ClientObject> clientsMap = new HashMap<Integer, ClientObject>(2);

    public Connector(int port, String dirName) {
        this.port = port;

        filesToDisplay = getImagesInDirectory(dirName);

        this.dirName = getOnlyDeepestFolder(dirName);
        System.out.println("Starting server on port " + port + "...");
        new ServerThread().start();
        // new ClientThread().start();
    }

    private int waitForClientConnection() throws IOException {
        ClientObject client = new ClientObject(serverSocket.accept());
        receiveIdForClient(client);
        printClientConnected(client.getClientId(), client);
        clientsMap.put(client.getClientId(), client);
        return client.getClientId();
    }

    /**
     * @author Thomas Bornschlegel
     * 
     */
    private class ServerThread extends Thread {
        @Override
        public void run() {
            try {
                Scanner scanner = new Scanner(System.in);

                serverSocket = new ServerSocket(port);
                System.out.println("Server started successfully.");

                System.out.println("Waiting for first client...");
                waitForClientConnection();
                try {
                    Thread.sleep(1000);
                } catch (Exception e1) {
                    System.err.println("Interrupted while trying to pause between connection attempts.");
                    e1.printStackTrace();
                }

                System.out.println("Do you want to connect a second client?");
                System.out.println("Enter \"y\" to do so, or \"n\" otherwise:");
                boolean connectSecondClient = getYesNoFromCommandLine(scanner);

                if (connectSecondClient) {
                    waitForClientConnection();
                }

                // TESTs FOR NEW METHODS BEGIN:

                // int clientOneId = 1;
                // int clientTwoId = 2;
                //
                // System.out.println("Client 1 connected? " + isConnected(getClient(clientOneId)));
                // System.out.println("Client 2 connected? " + isConnected(getClient(clientTwoId)));
                // System.out.println("Both Clients connected? " + areClientsConnectedProperly());
                // System.out
                // .println("Displaying blank screen on client 1: " + displayBlankScreen(getClient(clientOneId)));
                //
                // String directory = "/home/thomas/nook/stimuli_new";
                // switchImageDirectory(directory);
                // System.out.println("Image count of client1: " + countImages(getClient(clientOneId)));
                // ArrayList<String> images = getImagesInDirectory(directory);
                // String folderName = getOnlyDeepestFolder(directory);
                // String imageFileName = images.get(0);
                // boolean imageDisplayed = displayImage(folderName, imageFileName, getClient(clientOneId));
                // System.out.println("Displaying image " + imageFileName + ": " + imageDisplayed);

                // TESTs FOR NEW METHODS END

                // Code to display images in a row (on ENTER press) BEGIN
                displayNextImage(scanner);
                scanner.close();
                // Code to display images in a row END

                // Send exit messages to clients:
                for (ClientObject client : clientsMap.values()) {
                    if (isConnected(client)) {
                        sendMessage(RemoteMessageIds.MESSAGE_EXIT, "true", client);
                    }
                }

                // Close sockets:
                System.out.println("Starting to exit...");
                System.out.println("Please wait a moment.");
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                for (ClientObject client : clientsMap.values()) {
                    if (isConnected(client)) {
                        try {
                            client.getSocket().close();
                            System.out.println("Successfully closed client with id: " + client.getClientId());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                }
                try {
                    serverSocket.close();
                    System.out.println("Successfully closed the server.");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        /**
         * Sends a remote message to all clients. The clients will use the given directory to display the next images.
         * 
         * @param directory
         *            the name of the directory to use. Note that this variable contains only the folder name and not a
         *            whole path!
         */
        private void switchImageDirectory(String directory) {
            dirName = getOnlyDeepestFolder(directory);
            for (ClientObject client : clientsMap.values()) {
                if (isConnected(client)) {
                    try {
                        sendMessage(RemoteMessageIds.MESSAGE_USE_DIRECTORY, dirName, client.getOutputToServer());
                        String answer = client.getInputFromClient().readLine();
                        System.out.println("Client " + client.getClientId() + " answers: " + answer);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            filesToDisplay = getImagesInDirectory(directory);

        }

        /**
         * Displays a blank screen on the given client.
         * 
         * @param client
         *            the client that should display the blank screen
         * @return true if the client is displaying a blank screen, false if something went wrong.
         */
        private boolean displayBlankScreen(ClientObject client) {
            sendMessage(RemoteMessageIds.MESSAGE_SHOW_BLANK_SCREEN, "", client);
            try {
                String response = client.getInputFromClient().readLine();
                return response.startsWith("displaying blank screen");
            } catch (Exception e) {
                System.err.println("Error while trying to obtain image count for client " + client.getClientId());
                e.printStackTrace();
            }
            return false;
        }

        /**
         * @return the number of images in the current directory on the given client, or -1 if the number could not be
         *         obtained.
         */
        private int countImages(ClientObject client) {
            int answer = -1;
            sendMessage(RemoteMessageIds.MESSAGE_COUNT_IMAGES, "", client);
            try {
                String response = client.getInputFromClient().readLine();
                answer = Integer.parseInt(response);
            } catch (Exception e) {
                System.err.println("Error while trying to obtain image count for client " + client.getClientId());
                e.printStackTrace();
            }
            return answer;
        }

        /**
         * Displays the image in the given folder on the given client.
         * 
         * @param dirName
         *            the name of the folder in which the image is located in. Not the path! Example: "uni_bamberg"
         * @param imageFileName
         *            the name of the image file. Example: "1.png"
         * @param client
         *            the client that should display the image
         * 
         * @return true if the client send a positive response after displaying the image
         * */
        private boolean displayImage(String folderName, String imageFileName, ClientObject client) throws IOException {
            sendMessage(RemoteMessageIds.MESSAGE_DISPLAY_IMAGE, dirName + "/" + imageFileName, client);
            String answer = client.getInputFromClient().readLine();

            if (answer.equals("displaying:" + folderName + "/" + imageFileName)) {
                return true;
            } else {
                return false;
            }
        }

        /**
         * Waits for the user to press enter. On each enter-press the next image in the current image is shown an a
         * client. The client with the smallest ID is the first client that displays the first image. The second image
         * is shown on the client with the second smallest ID and so on. After all clients showed an image it starts
         * again with the first client.
         */
        private void displayNextImage(Scanner scanner) throws IOException {

            System.out.println("You can exit at any time by entering \"quit\" and pressing Enter.");

            int minClientId = Integer.MAX_VALUE;
            int maxClientId = Integer.MIN_VALUE;

            for (int id : clientsMap.keySet()) {
                if (id <= minClientId) {
                    minClientId = id;
                }
                if (id >= maxClientId) {
                    maxClientId = id;
                }
            }
            // Start with the smallest client id
            int currentClientId = minClientId;

            int count = 0;
            for (String image : filesToDisplay) {
                ClientObject currentClient = clientsMap.get(currentClientId);

                System.out.println("Press Enter to display the next image: " + image);
                // Wait till the user presses enter:
                String line = scanner.nextLine();

                if (line.equals("quit")) {
                    System.out.println("You chose to quit.");
                    break;
                }

                // Then send the next image to the client:
                long startTime = System.currentTimeMillis();
                boolean displayedImage = displayImage(dirName, image, currentClient);
                long endTime = System.currentTimeMillis() - startTime;

                if (displayedImage) {
                    System.out.println("Displaying image: " + image + " (" + ++count + "/" + filesToDisplay.size()
                            + ")");
                } else {
                    System.out.println("Could NOT display image: " + image + " (" + ++count + "/"
                            + filesToDisplay.size() + ")");
                }
                System.out.println("Consumed time: " + endTime + "ms");

                if (currentClientId < maxClientId) {
                    currentClientId++;
                } else {
                    currentClientId = minClientId;
                }
            }

            boolean noOptionsSelected = true;

            System.out.println("Finished displaying images. Please enter:");
            while (noOptionsSelected) {
                System.out.println("\"r\" to restart with the first image.");
                System.out.println("\"q\" to close the server and terminate the experiment.");
                String option = scanner.nextLine();
                if (option.equals("r")) {
                    System.out.println("Thanks. You choose to restart with the first image.");
                    displayNextImage(scanner);
                    noOptionsSelected = false;
                } else if (option.equals("q")) {
                    System.out.println("Thanks. You choose to quit the server.");
                    noOptionsSelected = false;
                } else {
                    System.out
                            .println("Sorry the input was not correct. Please enter \"q\" or \"r\" without quotation marks.");
                    noOptionsSelected = true;
                }

            }

        }

        /**
         * Sends a message to the given client
         * 
         * @param messageCode
         *            the message code as defined in {@link RemoteMEssageIds}
         * @param messageContent
         *            the content of the message
         * @param client
         *            the client to communicate with
         */
        private void sendMessage(String messageCode, String messageContent, ClientObject client) {
            sendMessage(messageCode, messageContent, client.getOutputToServer());
        }

        private void sendMessage(String messageCode, String messageContent, PrintWriter writer) {
            writer.write(messageCode + ":" + messageContent + "\n");
            writer.flush();
        }

    }

    /**
     * Asks the given client to send its ID to the server. Stores the ID temporarily on the server.
     */
    private void receiveIdForClient(ClientObject client) throws IOException {
        String response = client.getInputFromClient().readLine();
        response = response.replace(RemoteMessageIds.MESSAGE_SEND_CLIENT_ID + ":", "");
        int answer = Integer.parseInt(response);
        client.setClientId(answer);
    }

    /**
     * Shows it the given client is connected and displays its address.
     */
    private void printClientConnected(int clientId, ClientObject client) {
        System.out.println("Client with id " + clientId + " connected.");
        System.out.println("Computer Socket Address: " + client.getSocket().getLocalSocketAddress().toString());
        System.out.println("Client " + clientId + " Socket Address: "
                + client.getSocket().getRemoteSocketAddress().toString());
    }

    /**
     * @return true if all clients are connected properly to this Connector.
     * */
    private boolean areClientsConnectedProperly() {
        for (ClientObject client : clientsMap.values()) {
            if (!isConnected(client)) {
                System.err.println("Client " + client.getClientId() + " is NOT properly connected.");
                return false;
            } else {
                System.out.println("Client " + client.getClientId() + " is properly connected.");
            }
        }
        return true;
    }

    /**
     * @return true if the client is connected properly
     * */
    private boolean isConnected(ClientObject client) {
        if (client == null) {
            return false;
        }
        Socket socket = client.getSocket();
        if (socket == null) {
            return false;
        }
        return !socket.isClosed() && socket.isConnected();
    }

    /**
     * @return true if the user entered "y", false if the user entered "n"
     */
    private boolean getYesNoFromCommandLine(Scanner scanner) {
        boolean noOptionsSelected = true;

        while (noOptionsSelected) {
            String option = scanner.nextLine();
            if (option.equalsIgnoreCase("y")) {
                return true;
            } else if (option.equalsIgnoreCase("n")) {
                return false;
            } else {
                System.out
                        .println("Sorry the input was not correct. Please enter \"y\" or \"n\" without quotation marks.");
                noOptionsSelected = true;
            }

        }

        return false;
    }

    /**
     * @return the top folder of the given directory. For the input "C:\\uni_bamberg" this method returns "uni_bamberg"
     * */
    private String getOnlyDeepestFolder(String dirName) {
        File file = new File(dirName);
        String folder = file.getName();
        file = null;
        return folder;
    }

    /**
     * 
     * @param dirName
     *            for example "C:\\uni_bamberg"
     * @return the names of all images (png or jpg) in the given directory
     * */
    private ArrayList<String> getImagesInDirectory(String dirName) {
        File file = new File(dirName);
        String[] files = file.list();

        ArrayList<String> filesToDisplay = new ArrayList<String>();
        for (String singleFile : files) {
            if (singleFile.endsWith(".png") || singleFile.endsWith(".jpg") || singleFile.endsWith(".PNG")
                    || singleFile.endsWith(".JPG")) {
                filesToDisplay.add(singleFile);
            }
        }

        Collections.sort(filesToDisplay);
        System.out.println("Found " + filesToDisplay.size() + " images to display.");

        return filesToDisplay;
    }

    // // XXX just for testing
    // private class ClientThread extends Thread {
    // @Override
    // public void run() {
    // try {
    // System.out.println("Starting client...");
    // client = new Socket("localhost", port);
    // InputStream is = client.getInputStream();
    // OutputStream out = client.getOutputStream();
    // out.write(4);
    // out.write(9);
    // int result = is.read();
    // System.out.println("Client received result: " + result);
    // } catch (IOException e) {
    // e.printStackTrace();
    // } finally {
    // try {
    // client.close();
    // System.out.println("Successfully closed the client.");
    // } catch (IOException e) {
    // e.printStackTrace();
    // }
    // }
    // }
    // }

}
