package entity.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author guohaohao
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Property {

    private PropertyEnum property;
    private double value;

    @Override
    public String toString() {
        return property.getName() + "\t\t" + String.format(property.getFormat(), value);
    }

}
