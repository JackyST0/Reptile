package org.example;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.taobao.api.DefaultTaobaoClient;
import com.taobao.api.TaobaoClient;
import com.taobao.api.request.TbkDgMaterialOptionalUpgradeRequest;
import com.taobao.api.response.TbkDgMaterialOptionalUpgradeResponse;

import java.io.*;

/**
 * @author: 谭健新
 * @github: JackyST0
 * @date: 2024/8/15 9:48
 */
public class Main {
    public static void main(String[] args) {
        runCrawler();
    }

    private static void runCrawler() {
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader("E:\\local-projects\\taobao_sofa-reptile-java\\src\\main\\resources\\keyword.txt"));
            String line = reader.readLine();
            while (line != null) {
                System.out.println(line);
                reptile(line);
                // 读取下一行
                line = reader.readLine();
                System.out.println("-------------------------------------");
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void reptile(String entry) {
        try {
            for (int i = 1; i <= 15; i++) {
                TaobaoClient client = new DefaultTaobaoClient("http://gw.api.taobao.com/router/rest", "34697407", "70abe66f019be92549bde431b69df2ac");
                TbkDgMaterialOptionalUpgradeRequest req = new TbkDgMaterialOptionalUpgradeRequest();
//        // false	10	商品筛选-店铺dsr评分。筛选大于等于当前设置的店铺dsr评分的商品0-50000之间
//        req.setStartDsr(10L);
                // false	20	页大小，默认20，1~100
                req.setPageSize(100L);
                // false	1	第几页，默认：１~15
                req.setPageNo((long) i);
                req.setQ(entry);
                req.setAdzoneId(115692600415L);
                TbkDgMaterialOptionalUpgradeResponse rsp = client.execute(req);
                String body = rsp.getBody();
                String path = entry + "-" + i + ".json";
                System.out.println(path);
                FileWriter file = new FileWriter( "E:\\local-projects\\taobao_sofa-reptile-java\\data\\" + path);
                BufferedWriter bufferedWriter = new BufferedWriter(file);
                bufferedWriter.write(body);
                bufferedWriter.close();
                JSONObject jsonObject = new JSONObject(body);
                Object tbkDgMaterialOptionalUpgradeResponse = jsonObject.get("tbk_dg_material_optional_upgrade_response");
                JSONObject jsonObject1 = new JSONObject(tbkDgMaterialOptionalUpgradeResponse);
                Object resultList = jsonObject1.get("result_list");
                JSONObject jsonObject2 = new JSONObject(resultList);
                Object mapData = jsonObject2.get("map_data");
                JSONArray jsonArray = new JSONArray(mapData);
                if (jsonArray.size() < 100) {
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}