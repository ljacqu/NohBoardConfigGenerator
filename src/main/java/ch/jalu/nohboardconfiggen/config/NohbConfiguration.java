package ch.jalu.nohboardconfiggen.config;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class NohbConfiguration {

    @SerializedName("Elements")
    private List<NohbElement> elements = new ArrayList<>();

    @SerializedName("Height")
    private int height;
    @SerializedName("Width")
    private int width;

    @SerializedName("Version")
    private int version = 2;

}
