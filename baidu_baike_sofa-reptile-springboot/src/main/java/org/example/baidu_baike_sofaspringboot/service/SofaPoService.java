package org.example.baidu_baike_sofaspringboot.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import org.example.baidu_baike_sofaspringboot.entity.Sofa;
import org.example.baidu_baike_sofaspringboot.entity.SofaPo;
import org.example.baidu_baike_sofaspringboot.mapper.SofaPoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author: 谭健新
 * @github: JackyST0
 * @date: 2024/8/14 11:37
 */
@Service
public class SofaPoService {
    @Autowired
    private SofaPoMapper sofaPoMapper;

    public void format(JSONArray sofa) {
        for (int i = 0; i < sofa.size(); i++) {
            JSONObject item = sofa.getJSONObject(i);
            Sofa bean = BeanUtil.toBean(item, Sofa.class);
            SofaPo sofaPo = new SofaPo();
            BeanUtil.copyProperties(bean, sofaPo);
            if (bean.getChildren() == null || bean.getChildren().isEmpty()) {
                if (bean.getDescription() != null && !bean.getDescription().isEmpty()) {
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("《标题》").append(bean.getTitle()).append("\n\t");
                    List<String> description = bean.getDescription();
                    String join = String.join("\n\t", description);
                    stringBuilder.append(join);
                    sofaPo.setContent(stringBuilder.toString());
                }
            } else {
                StringBuilder stringBuilder = new StringBuilder();
                if (bean.getDescription() != null && !bean.getDescription().isEmpty()) {
                    stringBuilder.append("《标题》").append(bean.getTitle()).append("\n\t");
                    List<String> description = bean.getDescription();
                    String join = String.join("\n\t", description);
                    stringBuilder.append(join).append("\n");
                }
                List<Sofa> children = bean.getChildren();
                for (Sofa child : children) {
                    if (child.getChildren() == null || child.getChildren().isEmpty()) {
                        stringBuilder.append("\n");
                        stringBuilder.append("《一级目录》").append(child.getTitle()).append("\n\t");
                        List<String> description = child.getDescription();
                        String join = String.join("\n\t", description);
                        stringBuilder.append(join).append("\n");
                    } else {
                        stringBuilder.append("\n");
                        stringBuilder.append("《一级目录》").append(child.getTitle());
                        if (child.getDescription() != null && !child.getDescription().isEmpty()) {
                            stringBuilder.append("\n\t");
                            List<String> description = child.getDescription();
                            String join = String.join("\n\t", description);
                            stringBuilder.append(join);

                        }
                        List<Sofa> children1 = child.getChildren();
                        for (Sofa sofa1 : children1) {
                            stringBuilder.append("\n\t");
                            stringBuilder.append("《二级目录》").append(sofa1.getTitle()).append("\n\t\t");
                            List<String> description1 = sofa1.getDescription();
                            String string = String.join("\n\t\t", description1);
                            stringBuilder.append(string);
                        }
                        stringBuilder.append("\n");
                    }
                }
                sofaPo.setContent(stringBuilder.toString());
            }
            System.out.println(sofaPo);
//            sofaPoMapper.insert(sofaPo);
        }
    }
}
