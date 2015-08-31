import com.myjeeva.digitalocean.DigitalOcean;
import com.myjeeva.digitalocean.common.DropletStatus;
import com.myjeeva.digitalocean.exception.DigitalOceanException;
import com.myjeeva.digitalocean.exception.RequestUnsuccessfulException;
import com.myjeeva.digitalocean.impl.DigitalOceanClient;
import com.myjeeva.digitalocean.pojo.*;
import org.apache.log4j.BasicConfigurator;

import java.util.ArrayList;
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


        BasicConfigurator.configure();

        Scanner scanner = new Scanner(System.in);
        displayGUI();
        checkAuthToken(authToken);
        while(scanner.hasNext()){
            int input = scanner.nextInt();
            switch(input){
                case 1:
                    System.out.println("Creating new Mumble Server droplet...");
                    createMumbleServer(apiClient);
                    getIpAddress(apiClient);
                    displayGUISpace();
                    displayGUI();
                    break;
                case 2:
                    System.out.println("Fetching IP address for mumble Server...");
                    getIpAddress(apiClient);
                    displayGUISpace();
                    displayGUI();
                    break;
                case 3:
                    System.out.println("Deleting Mumble Server droplet...");
                    deleteMumbleDroplet(apiClient);
                    displayGUISpace();
                    displayGUI();
                    break;
                case 4:
                    System.out.println("Bye bye");
                    scanner.close();
                    System.exit(0);
            }
        }

//        createMumbleServer(apiClient);
//        getIpAddress(apiClient);







    }

    private static void checkAuthToken(String authToken) {
        if(authToken.equals("$TOKEN")){
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
        System.out.println("2 - get IP address for active Mumble Server");
        System.out.println("3 - destroy Server");
        System.out.println("4 - exit");
        System.out.println("#################################################################");
    }

    private static void deleteMumbleDroplet(DigitalOcean apiClient) throws DigitalOceanException, RequestUnsuccessfulException {
        Droplets droplets = apiClient.getAvailableDroplets(0);
        List<Droplet> dropletList = droplets.getDroplets();
        Droplet mumbleDroplet = null;
        for (Droplet droplet : dropletList) {
            if (droplet.getName().equals("api-client-test-host")) {
                mumbleDroplet = droplet;
            }
        }
        apiClient.deleteDroplet(mumbleDroplet.getId());
    }

    private static void getIpAddress(DigitalOcean apiClient) throws DigitalOceanException, RequestUnsuccessfulException {
        Droplets droplets = apiClient.getAvailableDroplets(0);
        List<Droplet> dropletList = droplets.getDroplets();
        Droplet mumbleDroplet = null;
        for (Droplet droplet : dropletList) {
            if (droplet.getName().equals("api-client-test-host")) {
                mumbleDroplet = droplet;
            }
        }
        String status;
        DropletStatus dropletStatus = mumbleDroplet.getStatus();
        status = dropletStatus.toString();
        boolean waitForIP = true;
        while (waitForIP) {

            try {
                if (!mumbleDroplet.getStatus().toString().equals("active")) {
                    System.out.println("Server seems to be inactive, trying again in a second ...");
                    Thread.sleep(5000);
                }else{
                    //System.out.println("Server seems to be active now, fetching IP...");
                    Networks networks = mumbleDroplet.getNetworks();
                    List<Network> networksIP4 = networks.getVersion4Networks();
                    Network ip4address = networksIP4.get(0);
                    ip4address.getIpAddress();
                    System.out.println(ip4address.getIpAddress());
                    waitForIP = false;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }

    private static void createMumbleServer(DigitalOcean apiClient) {
        Images userImages = null;
        try {
            userImages = apiClient.getUserImages(0);
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

    public static Droplet createDroplet(DigitalOcean apiClient, Image img) {
        Droplet newDroplet = new Droplet();
        newDroplet.setName("api-client-test-host");
        newDroplet.setSize("512mb"); // setting size by slug value
        newDroplet.setRegion(new Region("fra1")); // setting region by slug value; sgp1 => Singapore 1 Data center
        newDroplet.setImage(img); // setting by Image Id 1601 => centos-5-8-x64 also available in image slug value
        newDroplet.setEnableBackup(Boolean.FALSE);
        newDroplet.setEnableIpv6(Boolean.FALSE);
        newDroplet.setEnablePrivateNetworking(Boolean.FALSE);
        return newDroplet;
    }
}
