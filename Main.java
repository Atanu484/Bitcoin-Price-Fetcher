import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicReference;

public class Main {
        public static void main(String[] args){
            exchangeRate();
        }
        public static void exchangeRate(){
            System.out.println("Please write the currency you want to convert from. Example: USD, EUR, GBP, INR etc.");
            Scanner scanner = new Scanner(System.in);
            String inputCurrency = scanner.nextLine();
            if (inputCurrency.length() != 3){
                System.out.println("Please write ISO Code of the currency. Example: USD, EUR, TRY");
                inputCurrency = scanner.nextLine();
            }
            String currencyInUpperCase = inputCurrency.toUpperCase(); // To make sure user input is taken in uppercase to avoid mistakes like uSd
            String currencyInLowerCase = inputCurrency.toLowerCase(); // To use it in the url
            String currencyAPI = "https://api.coindesk.com/v1/bpi/currentprice/" + currencyInLowerCase + ".json";


            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest
                    .newBuilder()
                    .uri(URI.create(currencyAPI))
                    .build();
            
            // Try-Catch Block To Catch Any Errors Or Exceptions And Print Message
            try {
                HttpResponse<String> response = client
                        .send(request, HttpResponse.BodyHandlers.ofString());
                JSONObject jsonObject = new JSONObject(response.body());


                String time = jsonObject.getJSONObject("time")
                        .getString("updated");

                String rate = jsonObject.getJSONObject("bpi")
                        .getJSONObject(currencyInUpperCase)
                        .getString("rate");
                System.out.println("As of " + time + ", the exchange rate is " + rate + " " + currencyInUpperCase + " per 1 BTC.");

                // Formatting the current time and date to be readable by API
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                String currentDate = LocalDateTime.now()
                        .format(formatter);

                // Dialing the time back to 180 days
                String pastDate = LocalDateTime.now()
                        .minusDays(180)
                        .format(formatter);

                // Getting the exchange rate for the past 180 days and storing it in a HashMap
                Map<String, String> exchangeRateChart = new HashMap<>();

                AtomicReference<String> highestValue = new AtomicReference<>("0");
                AtomicReference<String> lowestValue = new AtomicReference<>();
                AtomicReference<String> highestValueDate = new AtomicReference<>();
                AtomicReference<String> lowestValueDate = new AtomicReference<>();

                    //HTTP Request
                    String pastExchangeHistoryAPI = "https://api.coindesk.com/v1/bpi/historical/close.json?start=" + pastDate + "&end=" + currentDate + "&inputCurrency=" + inputCurrency;
                    HttpClient pastExchangeHistoryAPIClient = HttpClient.newHttpClient();
                    HttpRequest pastExchangeHistoryAPIRequest= HttpRequest.newBuilder()
                            .uri(URI.create(pastExchangeHistoryAPI))
                            .build();

                    HttpResponse<String> pastExchangeHistoryAPIResponse = pastExchangeHistoryAPIClient.send(pastExchangeHistoryAPIRequest, HttpResponse.BodyHandlers.ofString());
                    JSONObject jsonObject1 = new JSONObject(pastExchangeHistoryAPIResponse.body());

                    // Using for-loop to iterate and storing data into the variables
                    for (int i = 0; i < jsonObject1.length(); i++) {
                        JSONObject jsonObject2 = jsonObject1.getJSONObject("bpi");
                        jsonObject2.keys().forEachRemaining(key -> {
                            exchangeRateChart.put(key, jsonObject2.get(key).toString());

                            // The lowest value on the exchangeRateChart
                            if (lowestValue.get() == null) {
                                lowestValue.set(exchangeRateChart.get(key));
                                lowestValueDate.set(key);
                            } else if (Double.parseDouble(exchangeRateChart.get(key)) < Double.parseDouble(lowestValue.get())) {
                                lowestValue.set(exchangeRateChart.get(key));
                                lowestValueDate.set(key);
                            }

                            //The highest value in the exchangeRateChart
                            if (Double.parseDouble(exchangeRateChart.get(key)) > Double.parseDouble(highestValue.get())) {
                                highestValue.set(exchangeRateChart.get(key));
                                highestValueDate.set(key);
                            } else if (highestValue.get() == null) {
                                highestValue.set(exchangeRateChart.get(key));
                                highestValueDate.set(key);
                            }
                        });
                    }
                    // Printing the exchange rate history
                System.out.println("The lowest value on past 6 months was " + lowestValue.get() + " " + currencyInUpperCase + " per 1 BTC. on " + lowestValueDate.get());
                System.out.println("The highest value on past 6 months was " + highestValue.get() + " " + currencyInUpperCase + " per 1 BTC. on " + highestValueDate.get());
                scanner.close();
            } catch (Exception e) {
                System.out.println("Oops! The currency " + "'"+ inputCurrency +"'"+ " seems to be not supported." +
                        "\nIf you are not sure about the currency code or want to see the supported currencies by us, please visit: https://api.coindesk.com/v1/bpi/supported-currencies.json"+
                        "\nIf you want to continue, press Y / N to finish:");
                Scanner sc1 = new Scanner(System.in);
                String answer = sc1.nextLine();
                if (answer.equalsIgnoreCase("Y")){
                    exchangeRate();
                } else{
                    System.out.println("Thank you for using our service. Have a nice day!");
                    System.exit(0);
                }
            }
        }
}
