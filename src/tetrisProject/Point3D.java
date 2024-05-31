package tetrisProject;

public class Point3D {
    private int x;
    private int y;
    private int z;

    public Point3D(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    // Getter와 Setter 메소드
    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getZ() {
        return z;
    }

    public void setZ(int z) {
        this.z = z;
    }

    // toString 메소드를 오버라이드하여 좌표 정보를 쉽게 출력할 수 있도록 함
    @Override
    public String toString() {
        return "(" + x + ", " + y + ", " + z + ")";
    }
}
