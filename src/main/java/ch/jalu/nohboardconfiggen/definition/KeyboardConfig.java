package ch.jalu.nohboardconfiggen.definition;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class KeyboardConfig {

    private int width = 40;
    private int height = 40;
    private int space = 1;

    private List<KeyboardRow> rows = new ArrayList<>();

}
