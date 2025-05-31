package com.knemis.skyblock.skyblockcoreproject.utils; // Adjusted package

// No Lombok specified for this one, so a plain class.
public class Coordinate {
    public int x;
    public int y;
    public int z;

    public Coordinate(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    // Optional: Consider adding equals(), hashCode(), and toString() for a data class.
    // For initial integration, these are not required by the snippet.
    // @Override
    // public boolean equals(Object o) {
    //     if (this == o) return true;
    //     if (o == null || getClass() != o.getClass()) return false;
    //     Coordinate that = (Coordinate) o;
    //     return x == that.x && y == that.y && z == that.z;
    // }
    //
    // @Override
    // public int hashCode() {
    //     int result = x;
    //     result = 31 * result + y;
    //     result = 31 * result + z;
    //     return result;
    // }
    //
    // @Override
    // public String toString() {
    //     return "Coordinate{" +
    //            "x=" + x +
    //            ", y=" + y +
    //            ", z=" + z +
    //            '}';
    // }
}
