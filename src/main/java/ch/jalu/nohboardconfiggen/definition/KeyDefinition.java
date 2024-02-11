package ch.jalu.nohboardconfiggen.definition;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.Set;

@Getter
@Setter
@ToString
public class KeyDefinition {

    private String text;
    private Set<KeyCode> keys = EnumSet.noneOf(KeyCode.class);
    private BigDecimal customHeight;
    private BigDecimal customWidth;

}
