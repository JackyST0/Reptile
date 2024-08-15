package org.example.baidu_baike_sofaspringboot.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author: 谭健新
 * @github: JackyST0
 * @date: 2024/8/14 11:35
 */
@Data
@NoArgsConstructor
@TableName("sfc_minesweeper.baidu_baike_sofa")
public class SofaPo implements Serializable {
    private Long id;
    private String title;
    private String content;
    private String url;
    private Integer isHave;
}
