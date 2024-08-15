import cn.hutool.json.JSONObject;
import org.example.Sofa;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author: 谭健新
 * @github: JackyST0
 * @date: 2024/8/13 11:13
 */
public class Test {

    private final static String uri = "https://baike.baidu.com";

    public static void main(String[] args) throws IOException {
        JSONObject sofa = reptile("亮色沙发");
        System.out.println(sofa);
    }

    private static JSONObject reptile(String entry) throws IOException {
        Sofa sofa = new Sofa();

        // 要抓取的网站
        String url = uri + "/item/" + entry;

        // 使用爬虫获取
        Document document = Jsoup.connect(url).get();

        // 标题
        Elements titleElements = document.select("h1.lemmaTitle_cFJcb");
        sofa.setTitle(titleElements.text());

        // 简介
        Elements descriptionElements = document.select("div.lemmaSummary_Swr9S > div");
        List<String> descriptionList = new ArrayList<>();
        for (Element descriptionElement : descriptionElements) {
            Elements textElements = descriptionElement.select("span.text_Twnd7");
            for (Element textElement : textElements) {
                Elements hrefElements = textElement.select("a");
                if (hrefElements != null && !hrefElements.isEmpty()) {
                    String href = uri + textElement.select("a").attr("href");
                    descriptionList.add(textElement.text() + "(" + href + ")");
                } else {
                    descriptionList.add(textElement.text());
                }
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
                Sofa firstLevelSofa = directoryList.get(firstLevel);
                Elements textElements = dirctoryElement.select("span.text_Twnd7");
                List<String> levelDescriptionList = new ArrayList<>();
                for (Element textElement : textElements) {
                    Elements hrefElements = textElement.select("a");
                    if (hrefElements != null && !hrefElements.isEmpty()) {
                        String href = uri + textElement.select("a").attr("href");
                        levelDescriptionList.add(textElement.text() + "(" + href + ")");
                    } else {
                        levelDescriptionList.add(textElement.text());
                    }
                }
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
            }
        }
        return new JSONObject(sofa);
    }
}
