package ch.jalu.nohboardconfiggen.config;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class NohbElement {

    @SerializedName("__type")
    private String type = "KeyboardKey";

    @SerializedName("Id")
    private Integer id;

    @SerializedName("Boundaries")
    private List<NohbCoords> boundaries;

    @SerializedName("KeyCodes")
    private List<Integer> keyCodes;

    @SerializedName("Text")
    private String text;

    @SerializedName("TextPosition")
    private NohbCoords textPosition;

    @SerializedName("ChangeOnCaps")
    private boolean changeOnCaps = false;

    @SerializedName("ShiftText")
    private String shiftText;

    public void setTexts(String text) {
        this.text = text;
        this.shiftText = text;
    }
}
