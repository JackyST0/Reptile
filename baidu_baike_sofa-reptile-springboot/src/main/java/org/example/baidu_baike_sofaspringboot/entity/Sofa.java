package org.example.baidu_baike_sofaspringboot.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author: 谭健新
 * @github: JackyST0
 * @date: 2024/8/12 16:44
 */
@Data
@NoArgsConstructor
public class Sofa {
    private String url;
    private Boolean isHave;
    private String title;
    private List<String> description;
    private List<Sofa> children;
}
