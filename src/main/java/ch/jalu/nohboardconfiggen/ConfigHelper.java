package ch.jalu.nohboardconfiggen;

import ch.jalu.nohboardconfiggen.config.NohbCoords;

import java.util.List;

public class ConfigHelper {

    public static NohbCoords calculateCenterTextPosition(List<NohbCoords> boundaries) {
        if (boundaries == null || boundaries.size() != 4) {
            throw new IllegalArgumentException("Expected four boundaries");
        }

        Integer minX = null;
        Integer minY = null;
        Integer maxX = null;
        Integer maxY = null;
        for (NohbCoords boundary : boundaries) {
            minX = minNullSafe(minX, boundary.getX());
            minY = minNullSafe(minY, boundary.getY());
            maxX = maxNullSafe(maxX, boundary.getX());
            maxY = maxNullSafe(maxY, boundary.getY());
        }

        int textX = minX + (maxX - minX) / 2;
        int textY = minY + (maxY - minY) / 2;
        return new NohbCoords(textX, textY);
    }

    private static Integer minNullSafe(Integer a, Integer b) {
        if (a == null) {
            return b;
        } else if (b == null) {
            return a;
        }
        return Math.min(a, b);
    }

    private static Integer maxNullSafe(Integer a, Integer b) {
        if (a == null) {
            return b;
        } else if (b == null) {
            return a;
        }
        return Math.max(a, b);
    }
}
