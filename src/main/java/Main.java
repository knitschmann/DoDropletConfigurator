import com.myjeeva.digitalocean.DigitalOcean;
import com.myjeeva.digitalocean.exception.DigitalOceanException;
import com.myjeeva.digitalocean.exception.RequestUnsuccessfulException;
import com.myjeeva.digitalocean.impl.DigitalOceanClient;
import com.myjeeva.digitalocean.pojo.*;
import com.myjeeva.digitalocean.pojo.Image;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.List;
import java.util.Scanner;

/**
 * Created by kevin on 31/08/15.
 */
public class Main {
    static final boolean debug = false;

    public static void main(String args[]) throws Exception {
        String authToken = "$TOKEN";
        DigitalOcean apiClient = new DigitalOceanClient(authToken);

        //BasicConfigurator.configure();

        Scanner scanner = new Scanner(System.in);
        displayGUI();
        checkAuthToken(authToken);
        while (scanner.hasNext()) {
            try {
                if (!scanner.hasNextInt()) {
                    System.out.println("Type in the number of your choosing [1 - 4]");
                    scanner.nextLine();
                } else {
                    int input = scanner.nextInt();
                    switch (input) {
                        case 1:
                            System.out.println("Creating new Mumble Server droplet...");
                            createMumbleServer(apiClient);
                            getIpAddress(apiClient);
                            displayGUISpace();
                            displaySimpleGUI();
                            break;
                        case 2:
                            System.out.println("Setting Floating IP...");
//                            getIpAddress(apiClient);
                            setFloatingIp(apiClient);
                            displayGUISpace();
                            displaySimpleGUI();
                            break;
                        case 3:
                            System.out.println("Deleting Mumble Server droplet...");
                            deleteMumbleDroplet(apiClient);
                            displayGUISpace();
                            displaySimpleGUI();
                            break;
                        case 4:
                            System.out.println("by Kevin Nitschmann");
                            scanner.close();
                            System.exit(0);
                    }
                }
            } catch (Exception e) {
                System.out.println("Please enter a number!");
            }
        }
    }

    private static void setFloatingIp(DigitalOcean apiClient) throws DigitalOceanException, RequestUnsuccessfulException {
        Droplet mumbleDroplet = getAvailableDroplets(apiClient);
        if (mumbleDroplet == null) {
            System.out.println("There is no active Mumble Server");
        } else {
            apiClient.assignFloatingIP(mumbleDroplet.getId(),"188.166.195.143");
            System.out.println("Mumble Server set to FLoating IP successful");
        }
    }

    private static void deleteMumbleDroplet(DigitalOcean apiClient) throws DigitalOceanException, RequestUnsuccessfulException {
        Droplet mumbleDroplet = getAvailableDroplets(apiClient);
        if (mumbleDroplet == null) {
            System.out.println("There is no active Mumble Server");
        } else {
            apiClient.deleteDroplet(mumbleDroplet.getId());
            System.out.println("Mumble Server deletion has been successfully initiated");
        }
    }

    private static Droplet getAvailableDroplets(DigitalOcean apiClient) throws DigitalOceanException, RequestUnsuccessfulException {
        Droplets droplets = apiClient.getAvailableDroplets(0,0);
        List<Droplet> dropletList = droplets.getDroplets();
        Droplet mumbleDroplet = null;
        for (Droplet droplet : dropletList) {
            if (droplet.getName().equals("Mumble")) {
                mumbleDroplet = droplet;
            }
        }
        return mumbleDroplet;
    }

    private static void getIpAddress(DigitalOcean apiClient) throws DigitalOceanException, RequestUnsuccessfulException {
        Droplet mumbleDroplet = getAvailableDroplets(apiClient);
        if (mumbleDroplet == null) {
            System.out.println("No active Mumble Server has been found");

        } else {
            String status;
            status = mumbleDroplet.getStatus().toString();
            boolean waitForIP = true;
            while (waitForIP) {

                try {
                    if (!mumbleDroplet.isActive()) {
                        System.out.println("Server still setting up, waiting for IP ...");
                        Thread.sleep(5000);
                        mumbleDroplet = getAvailableDroplets(apiClient);
                        status = mumbleDroplet.getStatus().toString();

                    } else {
                        Networks networks = mumbleDroplet.getNetworks();
                        List<Network> networksIP4 = networks.getVersion4Networks();
                        Network ip4address = networksIP4.get(0);
                        ip4address.getIpAddress();
                        System.out.println(ip4address.getIpAddress());
                        System.out.println("IP address has been copied to clipboard");
                        String myString = ip4address.getIpAddress();
                        StringSelection stringSelection = new StringSelection(myString);
                        Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
                        clpbrd.setContents(stringSelection, null);
                        waitForIP = false;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    private static void createMumbleServer(DigitalOcean apiClient) {
        Images userImages = null;
        try {

            Droplets droplets = apiClient.getAvailableDroplets(0,0);
            List<Droplet> dropletList = droplets.getDroplets();
            Droplet mumbleDroplet = null;
            for (Droplet droplet : dropletList) {
                if (droplet.getName().equals("Mumble")) {
                    System.out.println("Mumble server already started");
                    return;
                }
            }

            userImages = apiClient.getUserImages(0,0);
            List<Image> userImagesList = userImages.getImages();
            Image mumbleImage = null;
            for (Image img : userImagesList) {
                if (img.getId() == 13358881) {
                    mumbleImage = img;
                    break;
                }
            }
            if (mumbleImage != null) {
                Droplet tmpDroplet = createDroplet(apiClient, mumbleImage);
                Droplet droplet = apiClient.createDroplet(tmpDroplet);


            } else {
                System.out.println("Error: Mumble Snapshot ID not found");
            }
        } catch (DigitalOceanException e) {
            e.printStackTrace();
        } catch (RequestUnsuccessfulException e) {
            e.printStackTrace();
        }
    }


    private static Droplet createDroplet(DigitalOcean apiClient, Image img) {
        Droplet newDroplet = new Droplet();
        newDroplet.setName("Mumble");
        newDroplet.setSize("512mb"); // setting size by slug value
        newDroplet.setRegion(new Region("fra1")); // setting region by slug value; sgp1 => Singapore 1 Data center
        newDroplet.setImage(img); // setting by Image Id 1601 => centos-5-8-x64 also available in image slug value
        newDroplet.setEnableBackup(Boolean.FALSE);
        newDroplet.setEnableIpv6(Boolean.FALSE);
        newDroplet.setEnablePrivateNetworking(Boolean.FALSE);

        return newDroplet;
    }


    private static void checkAuthToken(String authToken) {
        if (authToken.equals("$TOKEN")) {
            System.out.println("WARNING: Authenticator Token has not been set");
        }
    }

    private static void displayGUISpace() {
        System.out.println("");
        System.out.println("");
        System.out.println("");
    }

    private static void displayGUI() {
        System.out.println("#################################################################");
        System.out.println("Mumble Server Deployment Script");
        System.out.println("1 - Create Droplet and Start Server from Snapshot");
        System.out.println("2 - set Floating IP");
        System.out.println("3 - destroy Server");
        System.out.println("4 - exit");
        System.out.println("#################################################################");
    }
    private static void displaySimpleGUI(){
        System.out.println("#What's next?#");
        System.out.println("(1 - Create), (2 - Set Floating IP), (3 - Destroy), (4 - Exit))");
    }
}
