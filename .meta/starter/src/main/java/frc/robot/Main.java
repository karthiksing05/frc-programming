// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import edu.wpi.first.wpilibj.RobotBase;

/**
 * The entry point. Do not modify this file.
 *
 * <p>Every Java program starts at a {@code main} method, and a robot program is no exception.
 * {@link RobotBase#startRobot} takes a "give me a new Robot" function and runs it forever: it
 * initialises the hardware abstraction layer, constructs your {@link Robot}, and then drives the
 * periodic loop at 50 Hz until the robot is powered off.
 *
 * <p>You will never edit this file during the curriculum. It is here because a real WPILib project
 * has it, and because knowing where the program actually starts is worth thirty seconds of your
 * attention.
 */
public final class Main {
  private Main() {}

  public static void main(String... args) {
    RobotBase.startRobot(Robot::new);
  }
}
