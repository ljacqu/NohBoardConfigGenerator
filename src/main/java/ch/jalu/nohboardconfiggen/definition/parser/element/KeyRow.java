package ch.jalu.nohboardconfiggen.definition.parser.element;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class KeyRow {

    private final List<Attribute> attributes = new ArrayList<>();
    private final List<KeyLine> keys = new ArrayList<>();

    public boolean hasKeys() {
        return !keys.isEmpty();
    }

    public void addKey(KeyLine key) {
        keys.add(key);
    }

    public KeyLine getKey(int index) {
        return keys.get(index);
    }
}
