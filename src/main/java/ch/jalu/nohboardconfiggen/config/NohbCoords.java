package ch.jalu.nohboardconfiggen.config;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NohbCoords {

    @SerializedName("X")
    private int x;

    @SerializedName("Y")
    private int y;

}
