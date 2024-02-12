package ch.jalu.nohboardconfiggen.definition;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@EqualsAndHashCode
@RequiredArgsConstructor
public class KeyBinding {

    private final List<Integer> codes;

    public KeyBinding(int code) {
        this.codes = List.of(code);
    }
}
