package frc.robot;

import edu.wpi.first.wpilibj.RobotBase;

/** Entry point — kept tiny per WPILib convention. */
public final class Main {
    private Main() {}

    public static void main(String... args) {
        RobotBase.startRobot(Robot::new);
    }
}
