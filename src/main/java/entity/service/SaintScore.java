package entity.service;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author guohaohao
 */
@Data
@AllArgsConstructor
public class SaintScore {

    private double score;
    private double levelScore;
    private double ratio;
    private long value;
    private String level;
    private String levelComment;

}
