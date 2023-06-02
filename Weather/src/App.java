
import java.util.*;
import java.net.*;
import java.io.*;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
// import java.lang.Math.*;

// I'll clean this up later
public class App {

    public static void main(String[] args) {

        // take in address from input
        Scanner s = new Scanner(System.in);
        System.out.print("Enter an address: ");
        String address = s.nextLine();
        s.close();

        // I don't want to keep typing it
        // probably don't leave in at the end
        if (address.equals("!")) {
            address = "boston logan airport";
        } else if (address.equals("@")) {
            // NW Florida
            address = "550 alf coleman rd panama city beach";
        }

        // Just trust me, it has to do this
        String encodedAddress = URLEncoder.encode(address, StandardCharsets.UTF_8);
        String urlFormat = "https://geocode.maps.co/search?q=" + encodedAddress;

        // geocode from address
        // 0 is latitude, 1 is longitude
        Double coords[] = { 0.0, 0.0 };
        geocoding(urlFormat, coords);

        // Format the URL, feed into gridpoints function, then feed into getTheWeather
        getTheWeather(gridpoints(formatURLGridpoints(urlFormat, coords)));

    }

    private static String formatURLGridpoints(String urlFormat, Double coords[]) {
        // I just want less stuff in main honestly
        // Same idea as before, just different API we have to access
        String x = URLEncoder.encode(coords[0].toString(), StandardCharsets.UTF_8);
        urlFormat = "https://api.weather.gov/points/" + x + ",";
        x = URLEncoder.encode(coords[1].toString(), StandardCharsets.UTF_8);
        urlFormat = urlFormat + x;

        return urlFormat;
    }

    // It does what you think it does
    // Sets coords to values of lat and lon
    // we love how java treats arrays
    private static void geocoding(String urlFormat, Double[] returner) {
        try {

            // http client for geocoding request
            HttpClient httpClient = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(urlFormat))
                    .build();

            // what's he gonna say
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            // string that contains the full response
            String responseBody = response.body();

            // contain the results of the latitude and longitude
            // java moment
            returner[0] = latlon(0, responseBody);
            returner[1] = latlon(1, responseBody);

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    // Used by geocoding method to pull lat and lon then do a little formatting
    private static Double latlon(int opt, String responseBody) {

        String lookingFor = "empty";
        if (opt == 0) {
            lookingFor = "lat";
        } else {
            lookingFor = "lon";
        }

        Double returner = 0.0;

        int startIndex = responseBody.indexOf(lookingFor);

        // this is the worst possible way to do this
        if (startIndex != -1 && startIndex + lookingFor.length() + 10 <= responseBody.length()) {
            String result = responseBody.substring(startIndex + lookingFor.length(),
                    startIndex + lookingFor.length() + 10);
            result = result.substring(3);

            returner = Double.parseDouble(result);

        }

        returner = Math.round(returner * 1000.0) / 1000.0;
        return returner;
    }

    private static String gridpoints(String urlFormat) {
        try {

            // http client for gridpoint request
            HttpClient httpClient = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(urlFormat))
                    .build();

            // what's he gonna say
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            // int statusCode = response.statusCode();
            // System.out.println(statusCode);

            // string that contains the full response
            String responseBody = response.body();

            String gridX = hitTheGriddy(0, responseBody);
            String gridY = hitTheGriddy(1, responseBody);
            String gridId = hitTheGriddy(2, responseBody);
            // gridId comes back with quotes, removing them
            gridId = gridId.replace("\"", "");

            // https://api.weather.gov/gridpoints/{office}/{grid X},{grid Y}/forecast

            // Just format the string here to return it
            String encodedAddress = URLEncoder.encode(gridId.toString(), StandardCharsets.UTF_8);
            String x = "https://api.weather.gov/gridpoints/" + encodedAddress + "/";
            encodedAddress = URLEncoder.encode(gridX.toString(), StandardCharsets.UTF_8);
            x = x + encodedAddress + ",";
            encodedAddress = URLEncoder.encode(gridY.toString(), StandardCharsets.UTF_8);
            x = x + encodedAddress + "/forecast";

            // Returns the formatted URL for the next request
            return x;

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        // In case you really broke it
        return "you broke it";
    }

    // Search and format for gridpoints
    // I genuinely apologize for this name
    private static String hitTheGriddy(int opt, String responseBody) {

        String lookingFor = "empty";
        if (opt == 0) {
            lookingFor = "gridX";
        } else if (opt == 1) {
            lookingFor = "gridY";
        } else {
            lookingFor = "gridId";
        }

        int startIndex = responseBody.indexOf(lookingFor);

        // this is the worst possible way to do this
        // just finding the specific string and getting 10 chars
        if (startIndex != -1 && startIndex + lookingFor.length() + 10 <= responseBody.length()) {
            String result = responseBody.substring(startIndex + lookingFor.length(),
                    startIndex + lookingFor.length() + 10);
            result = result.substring(3);

            int commaIndex = result.indexOf(",");
            if (commaIndex != -1) {
                // Return result before comma
                return result.substring(0, commaIndex);
            }

        }

        // If something really broke
        return "something broke";
    }

    private static void getTheWeather(String urlFormat) {
        try {

            // http client for weather request
            HttpClient httpClient = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(urlFormat))
                    .build();

            // what's he gonna say
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            // string that contains the full response
            String responseBody = response.body();

            // For now just prints the whole thing, we can pull whatever later
            System.out.println(responseBody);

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

}
