package ch.jalu.nohboardconfiggen.definition;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
public class KeyboardConfig {

    private int width = 40;
    private int height = 40;
    private int spacing = 1;

    private List<KeyboardRow> rows = new ArrayList<>();

}
