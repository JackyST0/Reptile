package org.example.baidu_baike_sofaspringboot;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import org.example.baidu_baike_sofaspringboot.entity.Sofa;
import org.example.baidu_baike_sofaspringboot.entity.SofaPo;
import org.example.baidu_baike_sofaspringboot.service.SofaPoService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@SpringBootApplication
public class BaiduBaikeSofaSpringbootApplication {

    @Autowired
    private SofaPoService sofaPoService;

    private final static String uri = "https://baike.baidu.com";

    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(BaiduBaikeSofaSpringbootApplication.class, args);
        BaiduBaikeSofaSpringbootApplication demo1Application = context.getBean(BaiduBaikeSofaSpringbootApplication.class);
        demo1Application.runCrawler();
    }

    private void runCrawler() {
        JSONArray jsonArray = new JSONArray();
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader("E:\\local-projects\\baidu_baike_sofa-reptile-springboot\\src\\main\\resources\\title.txt"));
            String line = reader.readLine();
            while (line != null) {
                System.out.println(line);
                JSONObject sofa = reptile(line);
                jsonArray.add(sofa);
                // 读取下一行
                line = reader.readLine();
            }
            reader.close();
            System.out.println("----------------------------------------------------------------------------------------------------------------------");
            System.out.println(jsonArray);
            System.out.println(jsonArray.size());
            sofaPoService.format(jsonArray);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static JSONObject reptile(String entry) throws IOException {
        Sofa sofa = new Sofa();

        // 网站
        String url = uri + "/item/" + entry;

        // 使用爬虫获取
        Document document = Jsoup.connect(url)
                .get();

        // 标题
        Elements titleElements = document.select(".J-lemma-title");
        if (!titleElements.isEmpty()) {
            sofa.setUrl(url);
            sofa.setIsHave(true);
            sofa.setTitle(titleElements.text());
        } else {
            sofa.setUrl("https://baike.baidu.com/search?word=" + entry);
            sofa.setIsHave(false);
            return new JSONObject(sofa);
        }


        // 简介
        Elements descriptionElements = document.select(".J-summary > div");
        List<String> descriptionList = new ArrayList<>();
        for (Element descriptionElement : descriptionElements) {
            StringBuilder descriptionStringBuilder = new StringBuilder();
            Elements textElements = descriptionElement.children();
            for (Element textElement : textElements) {
                Elements hrefElements = textElement.select("a");
                if (hrefElements != null && !hrefElements.isEmpty()) {
                    String href = uri + textElement.select("a").attr("href");
                    descriptionStringBuilder.append(textElement.text()).append("(").append(href).append(")");
                } else {
                    descriptionStringBuilder.append(textElement.text());
                }
            }
            if (descriptionStringBuilder.length() > 0) {
                descriptionList.add(descriptionStringBuilder.toString());
            }
        }
        sofa.setDescription(descriptionList);

        // 目录
        int firstLevel = 0;     // 一级目录
        int secondLevel = 0;    // 二级目录
        List<Sofa> directoryList = new ArrayList<>();
        sofa.setChildren(directoryList);
        Elements directoryElements = document.select("div.J-lemma-content > div");
        for (Element dirctoryElement : directoryElements) {
            String dataTag = dirctoryElement.attr("data-tag");
            // 标题
            if (Objects.equals(dataTag, "header")) {
                String dataIndex = dirctoryElement.attr("data-index");
                int level = dataIndex.length() - dataIndex.replace("-", "").length() + 1;
                // 一级标题
                if (level == 1) {
                    Sofa firstLevelSofa = new Sofa();
                    Elements h2Elements = dirctoryElement.select("h2");
                    firstLevelSofa.setTitle(h2Elements.text());
                    directoryList.add(firstLevelSofa);
                    firstLevel = Integer.parseInt(dataIndex) - 1;
                    secondLevel = 0;
                } else {
                    // 二级标题
                    Sofa firstLevelSofa = directoryList.get(firstLevel);
                    Elements h3Elements = dirctoryElement.select("h3");
                    Sofa secondLevelSofa = new Sofa();
                    secondLevelSofa.setTitle(h3Elements.text());
                    List<Sofa> secondLevelSofaList = new ArrayList<>();
                    secondLevelSofaList.add(secondLevelSofa);
                    // 判断一级目录是否有孩子，无则设置二级目录，有则添加入二级目录
                    if (firstLevelSofa.getChildren() == null || firstLevelSofa.getChildren().isEmpty()) {
                        firstLevelSofa.setChildren(secondLevelSofaList);
                    } else {
                        List<Sofa> firstLevelSofaChildren = firstLevelSofa.getChildren();
                        firstLevelSofaChildren.add(secondLevelSofa);
                        secondLevel = firstLevelSofaChildren.size() - 1;
                    }
                }
            } else {
                // 段落
                StringBuilder descriptionStringBuilder = new StringBuilder();
                Elements textElements = dirctoryElement.children();
                List<String> levelDescriptionList = new ArrayList<>();
                for (Element textElement : textElements) {
                    Elements hrefElements = textElement.select("a");
                    if (hrefElements != null && !hrefElements.isEmpty()) {
                        String href = uri + textElement.select("a").attr("href");
                        descriptionStringBuilder.append(textElement.text()).append("(").append(href).append(")");
                    } else {
                        descriptionStringBuilder.append(textElement.text());
                    }
                }
                if (descriptionStringBuilder.length() > 0) {
                    levelDescriptionList.add(descriptionStringBuilder.toString());
                }
                if (directoryList != null && directoryList.size() > 0) {
                    Sofa firstLevelSofa = directoryList.get(firstLevel);
                    // 判断一级目录是否有孩子，无则设置描述，有则往二级目录添加描述
                    if (firstLevelSofa.getChildren() == null || firstLevelSofa.getChildren().isEmpty()) {
                        if (firstLevelSofa.getDescription() != null && !firstLevelSofa.getDescription().isEmpty()) {
                            List<String> firstLevelSofaDescription = firstLevelSofa.getDescription();
                            firstLevelSofaDescription.addAll(levelDescriptionList);
                        } else {
                            firstLevelSofa.setDescription(levelDescriptionList);
                        }
                    } else {
                        Sofa secondLevelSofa = firstLevelSofa.getChildren().get(secondLevel);
                        if (secondLevelSofa.getDescription() != null && !secondLevelSofa.getDescription().isEmpty()) {
                            List<String> secondLevelSofaDescription = secondLevelSofa.getDescription();
                            secondLevelSofaDescription.addAll(levelDescriptionList);
                        } else {
                            secondLevelSofa.setDescription(levelDescriptionList);
                        }
                    }
                } else {
                    sofa.getDescription().addAll(levelDescriptionList);
                }
            }
        }
        return new JSONObject(sofa);
    }
}
