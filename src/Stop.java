/**
 * Created by kikko on 3/11/14.
 */
public class Stop implements Comparable<Stop>{
    public final int floor, nextDirection;

    public Stop(int floor, int nextDirection) {
        this.floor = floor;
        this.nextDirection = nextDirection;
    }
    public Stop(int floor ) {
        this.floor = floor;
        this.nextDirection = Const.NO_NEXT_DIRECTION;
    }
    @Override
    public int compareTo(Stop o) {
        if(this.floor > o.floor)
            return 1;
        else if(this.floor < o.floor)
            return -1;
        else
            return 0;

    }
    @Override
    public boolean equals(Object o) {
        if(!(o instanceof Stop))
            return false;

        Stop s = (Stop)o;
        return this.floor == s.floor;
    }

    public Stop stopWithNegativeFloor() {
        return new Stop(-floor,nextDirection);
    }
}
