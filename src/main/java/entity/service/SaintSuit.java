package entity.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author guohaohao
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SaintSuit {

    private Long id;
    private String suitName;
    private Integer pos;
    private String saintName;

}
