package ch.jalu.nohboardconfiggen.definition;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.EnumSet;
import java.util.Set;

@Getter
@Setter
@ToString
public class KeyDefinition {

    private String text;
    private Set<KeyCode> keys = EnumSet.noneOf(KeyCode.class);
    private ValueWithUnit customHeight;
    private ValueWithUnit customWidth;
    private ValueWithUnit marginTop;
    private ValueWithUnit marginLeft;
    private boolean stacked;

}
