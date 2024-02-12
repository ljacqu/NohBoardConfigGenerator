package ch.jalu.nohboardconfiggen.definition;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
public class KeyDefinition {

    private String text;
    private List<KeyBinding> keys = new ArrayList<>();
    private ValueWithUnit customHeight;
    private ValueWithUnit customWidth;
    private ValueWithUnit marginTop;
    private ValueWithUnit marginLeft;
    private boolean stacked;

}
