package ch.jalu.nohboardconfiggen;

import ch.jalu.nohboardconfiggen.config.NohbCoords;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test for {@link ConfigHelper}.
 */
class ConfigHelperTest {

    @Test
    void shouldCalculateTextPosition() {
        // given
        List<NohbCoords> boundaries = List.of(
            new NohbCoords(316, 53),
            new NohbCoords(356, 53),
            new NohbCoords(356, 93),
            new NohbCoords(316, 93));

        // when
        NohbCoords textPosition = ConfigHelper.calculateCenterTextPosition(boundaries);

        // then
        assertThat(textPosition.getX(), equalTo(336));
        assertThat(textPosition.getY(), equalTo(73));
    }

    @Test
    void shouldCalculateTextPosition2() {
        // given
        List<NohbCoords> boundaries = List.of(
            new NohbCoords(8, 6),
            new NohbCoords(51, 6),
            new NohbCoords(51, 46),
            new NohbCoords(8, 46));

        // when
        NohbCoords textPosition = ConfigHelper.calculateCenterTextPosition(boundaries);

        // then
        assertThat(textPosition.getX(), equalTo(29));
        assertThat(textPosition.getY(), equalTo(26));
    }

    @Test
    void shouldThrowIfBoundarySizeNotFour() {
        // given
        List<NohbCoords> boundaries = List.of(
            new NohbCoords(8, 6),
            new NohbCoords(51, 6),
            new NohbCoords(8, 46));

        // when / then
        assertThrows(IllegalArgumentException.class, () -> ConfigHelper.calculateCenterTextPosition(boundaries));
    }
}