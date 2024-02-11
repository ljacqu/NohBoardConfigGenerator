package ch.jalu.nohboardconfiggen.definition;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class KeyboardRow {

    private List<KeyDefinition> keys = new ArrayList<>();
    private Integer startX;
    private Integer startY;

}
