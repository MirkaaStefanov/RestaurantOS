package com.example.RestaurantOS.services.impl;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@AllArgsConstructor
public class ExchangeService {

    private final String API_URL = "https://api.exchangerate-api.com/v4/latest/EUR";
    private static final String URL = "https://www.xe.com/currencyconverter/convert/?Amount=1&From=EUR&To=TRY";
//    private final ChromeDriver driver;

    public double getEurToTryRate() {
        RestTemplate restTemplate = new RestTemplate();
        Map<String, Object> response = restTemplate.getForObject(API_URL, Map.class);
        Map<String, Double> rates = (Map<String, Double>) response.get("rates");
        return rates.get("TRY");
    }

//    public Double scrapeEuroToTryRate() {
//        Double exchangeRate = null;
//        ChromeOptions options = new ChromeOptions();
//        options.addArguments("--headless");
//
//        WebDriver driver = new ChromeDriver(options); // Create a new instance each time
//
//        try {
//            driver.get("https://www.xe.com/currencyconverter/convert/?Amount=1&From=EUR&To=TRY");
//
//            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
//            WebElement rateElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("p.sc-294d8168-1")));
//
//            String mainRate = rateElement.getText().split(" ")[0];
//
//            String decimalPart = "";
//            try {
//                WebElement fadedDigits = rateElement.findElement(By.cssSelector("span.faded-digits"));
//                decimalPart = fadedDigits.getText();
//            } catch (NoSuchElementException e) {
//                System.out.println("No faded digits found.");
//            }
//
//            exchangeRate = Double.parseDouble(mainRate);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            driver.quit(); // Always close the WebDriver
//        }
//
//        return exchangeRate;
//    }

}
