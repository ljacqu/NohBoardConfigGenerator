package ch.jalu.nohboardconfiggen.definition.generator;

import ch.jalu.nohboardconfiggen.definition.KeyBinding;
import ch.jalu.nohboardconfiggen.definition.KeyDefinition;
import ch.jalu.nohboardconfiggen.definition.KeyboardConfig;
import ch.jalu.nohboardconfiggen.definition.KeyboardRow;
import ch.jalu.nohboardconfiggen.definition.generator.attribute.KeyAttributes;
import ch.jalu.nohboardconfiggen.definition.generator.attribute.KeyboardAttributes;
import ch.jalu.nohboardconfiggen.definition.parser.element.Attribute;
import ch.jalu.nohboardconfiggen.definition.parser.element.KeyLine;
import ch.jalu.nohboardconfiggen.definition.parser.element.KeyNameSet;
import ch.jalu.nohboardconfiggen.definition.parser.element.KeyRow;
import ch.jalu.nohboardconfiggen.keycode.KeyboardLayout;
import ch.jalu.nohboardconfiggen.keycode.KeyboardRegion;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class KeyboardModelGenerator {

    public KeyboardConfig generate(List<Attribute> attributes, List<KeyRow> rows) {
        KeyboardConfig config = new KeyboardConfig();
        attributes.forEach(attr -> KeyboardAttributes.processAttribute(config, attr));

        KeyboardLayout keyboardLayout = getKeyboardLayout(attributes);
        List<KeyboardRow> rowModels = rows.stream()
            .map(row -> convertKeyboardRow(row, keyboardLayout))
            .toList();
        config.setRows(rowModels);

        return config;
    }

    private KeyboardRow convertKeyboardRow(KeyRow row, KeyboardLayout keyboardLayout) {
        List<KeyDefinition> keyModels = row.stream()
            .map(keyLine -> mapToKeyModel(keyLine, keyboardLayout))
            .toList();

        KeyboardRow rowModel = new KeyboardRow();
        rowModel.setKeys(keyModels);
        return rowModel;
    }

    private KeyDefinition mapToKeyModel(KeyLine keyLine, KeyboardLayout keyboardLayout) {
        KeyDefinition keyModel = new KeyDefinition();
        keyModel.setText(keyLine.displayText());
        keyModel.setKeys(mapToKeyBindingModels(keyLine.keys(), keyboardLayout));
        keyLine.attributes().forEach(attr -> KeyAttributes.processAttribute(keyModel, attr));
        return keyModel;
    }

    private List<KeyBinding> mapToKeyBindingModels(List<KeyNameSet> keyNameSets, KeyboardLayout keyboardLayout) {
        List<KeyBinding> result = new ArrayList<>(keyNameSets.size());
        for (KeyNameSet keyNameSet : keyNameSets) {
            List<Integer> keyCodes = keyNameSet.keys().stream().map(keyboardLayout::getKeyCodeOrThrow).toList();
            KeyBinding keyBinding = new KeyBinding(keyCodes);
            result.add(keyBinding);
        }
        return result;
    }

    private KeyboardLayout getKeyboardLayout(List<Attribute> attributes) {
        Optional<String> keyboardCode = attributes.stream()
            .filter(attr -> attr.name().equals("keyboard"))
            .map(Attribute::value)
            .findFirst();

        KeyboardRegion region = keyboardCode
            .map(KeyboardRegion::findByCodeOrThrow)
            .orElse(null);
        return KeyboardLayout.create(region);
    }
}
