package starwars;

import Environment.Environment;
import ai.Choice;
import ai.DecisionSet;
import geometry.Point3D;

public class AT_ST_BASICSURROUND extends AT_ST_BASICAVOID {

    // This is a more sophisticated guidance to avoid comples (concave) obstacles
    // that could freeze the agent with a infinite loop. In order to do that
    // We need to save some values when we find an obstacle, and remember them
    // for future decisions
    // Read more: https://docs.google.com/presentation/d/1JNA4sVv599_VPruT6OI_SDkMby3GtPA5GiY7q8LoQCw/edit#slide=id.g13b7d5a8846_0_361
    protected String whichWall, nextWhichwall; // Wich turn did we take in the first obstacle, left or right?
    protected double distance, nextdistance; // Which was the distance to the goal in that moment
    protected Point3D point, nextPoint; // Which was the GPS position of the agente in that moment?

    // In this ocasoin, we need to refactor proper function Ag()
    // just to remember the values explained above. The remaining is the same
    @Override
    protected Choice Ag(Environment E, DecisionSet A) {
        if (G(E)) {
            return null;
        } else if (A.isEmpty()) {
            return null;
        } else {
            A = Prioritize(E, A);
            whichWall = nextWhichwall;
            distance = nextdistance;
            point = nextPoint;
            return A.BestChoice();
        }
    }

    // We need to refactor again Utility funciont to split it into three cases
    // Two of them as in the previous version, and the new one to implement
    // The left-wall heuristic (always turn to the right in front of an obstacle.
    @Override
    protected double U(Environment E, Choice a) {
        if (whichWall.equals("LEFT")) {
            return goFollowWallLeft(E, a);
        } else if (!E.isFreeFront()) {
            return goAvoid(E, a);
        } else {
            return goAhead(E, a);
        }
    }

    // Refactor goAvoid, so that the first time we get to an obstacle, we remember 
    // its position for further use
    @Override
    public double goAvoid(Environment E, Choice a) {
        if (a.getName().equals("RIGHT")) {
            nextWhichwall = "LEFT";
            nextdistance = E.getDistance();
            nextPoint = E.getGPS();
            return Choice.ANY_VALUE;
        }
        return Choice.MAX_UTILITY;
    }

    // This is a new function, pleas read the other below and, then, continue reading this
    // This is a Utility function to follow a wall until is very end. I keep
    // always stuck to the wall. If the wall turns, I turn with it too.
    // When I get to a better positoin (closer to the goal) that the one I memorized
    // at the begining of the obstacle, then, stop surrounding and go back towards the goal.
    public double goFollowWallLeft(Environment E, Choice a) {
        if (E.isFreeFrontLeft()) {
            return goTurnOnWallLeft(E, a);
        } else if (E.isTargetFrontRight()
                && E.isFreeFrontRight()
                && E.getDistance() < point.planeDistanceTo(E.getTarget())) {
            return goStopWallLeft(E, a);
        } else if (E.isFreeFront()) {
            return goKeepOnWall(E, a);
        } else {
            return goRevolveWallLeft(E, a);
        }

    }

    // If I am still surrounding an obstacle and the front is free, just move to the front
    public double goKeepOnWall(Environment E, Choice a) {
        if (a.getName().equals("MOVE")) {
            return Choice.ANY_VALUE;
        }
        return Choice.MAX_UTILITY;
    }

    // If I am sourronding an obstacle and the obstacle turns, I turn with it
    public double goTurnOnWallLeft(Environment E, Choice a) {
        if (a.getName().equals("LEFT")) {
            return Choice.ANY_VALUE;
        }
        return Choice.MAX_UTILITY;

    }

    // The same as before but the other turn
    public double goRevolveWallLeft(Environment E, Choice a) {
        if (a.getName().equals("RIGHT")) {
            return Choice.ANY_VALUE;
        }
        return Choice.MAX_UTILITY;
    }

    // Very important. I I reacha a better position than the one memorized
    // at the begining of the obstacle, then I stop surronding and go back towards the goal
    public double goStopWallLeft(Environment E, Choice a) {
        if (a.getName().equals("RIGHT")) {
            this.resetAutoNAV();
            return Choice.ANY_VALUE;
        }
        return Choice.MAX_UTILITY;
    }

    @Override
    public Status MyJoinSession() {
        this.resetAutoNAV();
        return super.MyJoinSession();
    }

    @Override
    public String easyPrintPerceptions() {
        return super.easyPrintPerceptions()
                + "\nWall:\n" + whichWall + "\n";
    }

    // Every time a stop surrounding, I delete the previous memorized values 
    public void resetAutoNAV() {
        nextWhichwall = whichWall = "NONE";
        nextdistance = distance = Choice.MAX_UTILITY;
        nextPoint = point = null;
    }

}