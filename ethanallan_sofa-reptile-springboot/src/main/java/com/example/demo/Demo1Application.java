package com.example.demo;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootApplication
public class Demo1Application {

    @Autowired
    private RedisTemplate redisTemplate;

    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(Demo1Application.class, args);
        Demo1Application demo1Application = context.getBean(Demo1Application.class);
        demo1Application.runCrawler();
    }

    public void runCrawler() {
        // 设置webdriver.chrome.driver属性为ChromeDriver路径
        System.setProperty("webdriver.chrome.driver", "D:\\chromedriver-win64\\chromedriver.exe");

        ChromeOptions options = new ChromeOptions();
        // 允许所有远程源
        options.addArguments("--remote-allow-origins=*");
        // 排除"enable-automation"开关
        options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});

        // 创建一个新的Chrome浏览器实例
        WebDriver driver = new ChromeDriver(options);

        // 让浏览器访问Google
        driver.get("https://www.ethanallen.com/en_US/search?lang=en_US&q=sofa");

        // 用于收集所有商品的链接
        List<String> allLinks = new ArrayList<>();

        // 爬取相关商品数据
        while(true) {
            // 获取页面上所有的商品链接
            List<WebElement> links = driver.findElements(By.cssSelector("a.name-link"));
            List<String> collects = links.stream()
                    .map(n -> n.getAttribute("href"))
                    .distinct()
                    .collect(Collectors.toList());
            allLinks.addAll(collects);

            try {
                // 使用JavaScript来点击“下一页”按钮
                WebElement nextPageBtn = new WebDriverWait(driver, Duration.ofSeconds(1))
                        .until(ExpectedConditions.elementToBeClickable(By.cssSelector("span.fa.fa-angle-right")));
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", nextPageBtn);

                // 等待页面加载
                Thread.sleep(1000);
            } catch (Exception e) {
                System.out.println("No more pages.");
                break;
            }
        }

        System.out.println(allLinks.size());

        JSONArray array = new JSONArray();

        for (String link : allLinks) {
            driver.get(link);

            JSONObject obj = new JSONObject();

            // 找到具有特定CSS类的元素(NAME)
            WebElement nameElement = driver.findElement(By.cssSelector("h1.product-name.product-name-non-mobile"));
            obj.put("NAME", nameElement.getText());
            System.out.println(nameElement.getText());

            // (product-variations)
            List<WebElement> elements = driver.findElements(By.className("product-variations"));
            if (!elements.isEmpty()) {
                WebElement webElement = elements.get(0);
                Document outerHTML = Jsoup.parse(webElement.getAttribute("outerHTML"));
                Elements select = outerHTML.select("li");
                for (Element element : select) {
                    Document parse = Jsoup.parse(element.html());
                    Elements select1 = parse.select("span");
                    if (!select1.text().isEmpty()) {
                        Document parse1 = Jsoup.parse(select1.first().html());
                        Elements select2 = parse1.select("span");
                        if (!select2.text().isEmpty()) {
                            obj.put(select2.first().text().toUpperCase(), parse1.select("h2").text());
                            System.out.println(select2.first().text());
                            System.out.println(parse1.select("h2").text());
                        }
                    }
                }
            }

            // (sectionTitle)
            List<WebElement> elements8 = driver.findElements(By.className("sectionTitle"));
            if (!elements8.isEmpty()) {
                WebElement webElement = elements8.get(0);
                // 使用executeScript方法执行JavaScript代码，获取<div>元素的直接文本节点
                String divText = (String) ((JavascriptExecutor) driver).executeScript(
                        "return arguments[0].childNodes[0].nodeValue.trim();", webElement);
                WebElement span = webElement.findElement(By.tagName("span"));
                obj.put(divText.toUpperCase(), span.getText());
                System.out.println(divText);
                System.out.println(span.getText());
            }

            // 获取具有特定class的div元素(PRODUCT DETAILS)
            List<WebElement> elements1 = driver.findElements(By.id("product-description"));
            if (!elements1.isEmpty()) {
                List<String> list = new ArrayList<>();
                WebElement divElement1 = elements1.get(0);
                // 获取div元素内的所有p标签元素
                List<WebElement> pElements1 = divElement1.findElements(By.tagName("p"));
                list.add(pElements1.get(0).getText());
                System.out.println(pElements1.get(0).getText());
                // 获取div元素内的所有li标签元素
                List<WebElement> liElements1 = divElement1.findElements(By.tagName("li"));
                for (WebElement liElement : liElements1) {
                    list.add(liElement.getText());
                    System.out.println(liElement.getText());
                }
                JSONArray jsonArray = new JSONArray(list);
                obj.put("PRODUCT DETAILS", jsonArray);
            }

            // 获取具有特定class的div元素(ADDITIONAL INFORMATION)
            List<WebElement> elements2 = driver.findElements(By.id("additional-product-info-biography"));
            if (!elements2.isEmpty()) {
                List<String> list = new ArrayList<>();
                WebElement divElement2 = elements2.get(0);
                // 获取div元素内的所有p标签元素
                List<WebElement> pElements2 = divElement2.findElements(By.tagName("p"));
                for (WebElement pElement : pElements2) {
                    list.add(pElement.getAttribute("innerHTML"));
                    System.out.println(pElement.getAttribute("innerHTML"));
                }
                // 获取div元素内的所有li标签元素
                List<WebElement> liElements2 = divElement2.findElements(By.tagName("li"));
                for (WebElement liElement : liElements2) {
                    list.add(Jsoup.parse(liElement.getAttribute("outerHTML")).select("li").first().ownText());
                    System.out.println(Jsoup.parse(liElement.getAttribute("outerHTML")).select("li").first().ownText());
                }
                JSONArray jsonArray = new JSONArray(list);
                obj.put("ADDITIONAL INFORMATION", jsonArray);
            }

            // (PRODUCT SPECIFICATIONS)
            List<WebElement> elements3 = driver.findElements(By.id("how-To-Measure"));
            if (!elements3.isEmpty()) {
                List<String> list = new ArrayList<>();
                WebElement divElement3 = elements3.get(0);
                List<WebElement> aElements = divElement3.findElements(By.tagName("a"));
                for (WebElement aElement : aElements) {
                    String href = aElement.getAttribute("href");
                    list.add(href);
                    System.out.println(href);
                }
                List<String> collect = list.stream().distinct().collect(Collectors.toList());
                JSONArray jsonArray = new JSONArray(collect);
                obj.put("PRODUCT SPECIFICATIONS", jsonArray);
            }

            // (PRODUCT CARE AND WARRANTY)
            List<WebElement> elements4 = driver.findElements(By.id("warranty-brochure"));
            if (!elements4.isEmpty()) {
                List<String> list = new ArrayList<>();
                WebElement divElement4 = elements4.get(0);
                List<WebElement> aElements = divElement4.findElements(By.tagName("a"));
                for (WebElement aElement : aElements) {
                    String href = aElement.getAttribute("href");
                    list.add(href);
                    System.out.println(href);
                }
                JSONArray jsonArray = new JSONArray(list);
                obj.put("PRODUCT CARE AND WARRANTY", jsonArray);
            }

            // 获取具有特定class的div元素(SHIPPING & RETURNS)
            List<WebElement> elements5 = driver.findElements(By.id("shipping-returns"));
            if (!elements5.isEmpty()) {
                WebElement divElement5 = elements5.get(0);
                Document doc = Jsoup.parse(divElement5.getAttribute("outerHTML"));
                Element content = doc.getElementById("shipping-returns");
                String text = content.text();
                obj.put("SHIPPING & RETURNS", text);
                System.out.println(text);
            }

            array.put(obj);
            redisTemplate.opsForValue().set(nameElement.getText(),obj);

            // 打印出元素的文本
//            System.out.println(obj);
        }

        System.out.println("-------------------------------------------------------------------------------------------------------------------------------------------------------------");
        System.out.println(array);

        // 关闭浏览器
        driver.quit();
    }
}
