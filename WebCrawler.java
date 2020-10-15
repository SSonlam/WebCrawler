import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpClient.*;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.ArrayList;

/*
So far have implementing parsing but cannot figure out how to remove /


problems
now need to know how to handle request, specifically
301 redirects, and others of course

 */

public class WebCrawler {

    private static HttpURLConnection connection;
    private static String hopURL;
    private static ArrayList<String> visitedURLS = new ArrayList<String>();

    public static void main(String[] args) {
        String startingURL = args[0];
        visitedURLS.add(startingURL);
        hopURL = startingURL;
        int hopNum;
        int numberOfHops = Integer.parseInt(args[1]);
        for (int i = 0; i < numberOfHops; i++) {
            if(!crawlWeb(hopURL)){
                hopNum = i + 1;
                System.out.println("Page has run out of hops. Terminated at page : " + hopURL);
                System.out.println("Hop number: " + hopNum);
                break;
            }
            System.out.println(hopURL);
        }
        System.out.println();
        System.out.println("Here is contents of the last page in hop sequence:");
        System.out.println();

        printLastPage(hopURL);
    }

    public static boolean crawlWeb(String inputURL) {

        HttpClient client = HttpClient.newBuilder()
                .version(Version.HTTP_1_1)
                .followRedirects(Redirect.NORMAL)
                .build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(inputURL))
                .build();
        HttpResponse<String> response =
                null;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            //e.printStackTrace();
        } catch (InterruptedException e) {
            //e.printStackTrace();
        }

        String HTMLPage = response.body();
        Pattern linkPattern = Pattern.compile("href=\"(.*?)\"", Pattern.DOTALL);
        Matcher pageMatcher = linkPattern.matcher(HTMLPage);
        ArrayList<String> links = new ArrayList<String>();
        while (pageMatcher.find()) {
            links.add(pageMatcher.group(1));
        }
        if(links.size() == 0){
            return false;
        }
        String urlWithNoBackSlash = "";
        int httpStatusCode = 0;
        for(int i = 0; i < links.size(); i++){
            if(checkValidURL(links.get(i))){

                httpStatusCode = checkHttpCode((links.get(i)));
                if(httpStatusCode >= 200 && httpStatusCode <= 399){
                    if(!visitedYet(links.get(i))){
                        urlWithNoBackSlash = links.get(i).replaceAll("$/", "");
                        hopURL = urlWithNoBackSlash;
                        visitedURLS.add(hopURL);
                        break;
                    }
                }else if(httpStatusCode >= 400 && httpStatusCode <= 599){
                    System.out.println("Error reaching URL: " + links.get(i) + " Error code: " + httpStatusCode);
                }
            }
        }

        return true;
    }

    public static boolean checkValidURL(String inputURL) {
        try {
            URL validURL = new URL(inputURL);
            return true;
        } catch (MalformedURLException e) {
            //e.printStackTrace();
        }
        return false;
    }

    public static int checkHttpCode(String inputURL) {
        try {
            URL validURL = new URL(inputURL);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(inputURL))
                    .build();
            HttpResponse<String> response =
                    null;
            try {
                response = client.send(request, HttpResponse.BodyHandlers.ofString());
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            int i = response.statusCode();
            return i;


        } catch (MalformedURLException e) {
            //e.printStackTrace();
        }
        return 0;
    }


    public static boolean visitedYet(String inputURL){
        if(visitedURLS != null) {
            String urlWithNoBackSlash = inputURL.replaceAll("/$", "");
            for (int i = 0; i < visitedURLS.size(); i++) {
                if (inputURL.equals(visitedURLS.get(i))) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void printLastPage(String inputURL){
        HttpClient client = HttpClient.newBuilder()
                .version(Version.HTTP_1_1)
                .followRedirects(Redirect.NORMAL)
                .build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(inputURL))
                .build();
        HttpResponse<String> response =
                null;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            //e.printStackTrace();
        } catch (InterruptedException e) {
            //e.printStackTrace();
        }

        String HTMLPage = response.body();
        System.out.println(HTMLPage);
    }
}

