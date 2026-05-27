package frc.robot;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj2.command.CommandScheduler;

import org.littletonrobotics.junction.LogFileUtil;
import org.littletonrobotics.junction.LoggedRobot;
import org.littletonrobotics.junction.Logger;
import org.littletonrobotics.junction.networktables.NT4Publisher;
import org.littletonrobotics.junction.wpilog.WPILOGReader;
import org.littletonrobotics.junction.wpilog.WPILOGWriter;

/**
 * Robot entry — extends LoggedRobot (not TimedRobot) so AdvantageKit's
 * logger runs around every periodic call.
 *
 * Pattern lifted from Mechanical-Advantage/AdvantageKit's template
 * project. The mode switch decides what data sinks to use:
 *   REAL   → write WPILOG to USB stick, publish to NT4
 *   SIM    → publish to NT4 only (AdvantageScope on localhost picks it up)
 *   REPLAY → read a saved log as the input source, write a *_sim.wpilog
 */
public class Robot extends LoggedRobot {

    private RobotContainer container;

    public Robot() {
        // Metadata baked into every log so we know which commit ran
        Logger.recordMetadata("Project",  "FRCProgramming-Lessons");
        Logger.recordMetadata("ActiveLesson", System.getenv().getOrDefault("FRCPROG_LESSON", "all"));

        switch (Constants.currentMode) {
            case REAL:
                Logger.addDataReceiver(new WPILOGWriter());
                Logger.addDataReceiver(new NT4Publisher());
                break;
            case SIM:
                Logger.addDataReceiver(new WPILOGWriter(Constants.SIM_LOG_DIR));
                Logger.addDataReceiver(new NT4Publisher());
                break;
            case REPLAY:
                setUseTiming(false);    // run as fast as possible
                String path = LogFileUtil.findReplayLog();
                Logger.setReplaySource(new WPILOGReader(path));
                Logger.addDataReceiver(new WPILOGWriter(
                    LogFileUtil.addPathSuffix(path, "_sim")));
                break;
        }
        Logger.start();  // MUST come before any periodic invocation
    }

    @Override
    public void robotInit() {
        container = new RobotContainer();
    }

    @Override
    public void robotPeriodic() {
        CommandScheduler.getInstance().run();
    }

    @Override
    public void disabledInit()      {}
    @Override
    public void disabledPeriodic()  {}
    @Override
    public void autonomousInit()    {}
    @Override
    public void autonomousPeriodic(){}
    @Override
    public void teleopInit()        {}
    @Override
    public void teleopPeriodic()    {}
    @Override
    public void testInit()          { CommandScheduler.getInstance().cancelAll(); }
    @Override
    public void testPeriodic()      {}
    @Override
    public void simulationInit()    {}
    @Override
    public void simulationPeriodic(){
        // Battery sag simulation could live here.
        // Keep it minimal — sim physics belongs in IOSim classes.
    }
}
