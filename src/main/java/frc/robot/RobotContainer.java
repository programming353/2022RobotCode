// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import edu.wpi.first.wpilibj.GenericHID;
import edu.wpi.first.wpilibj.Joystick;
import frc.robot.Constants.Buttons;
import frc.robot.Constants.DriveConstants;
import frc.robot.commands.AutoNav;
import frc.robot.commands.ClimberCommand;
import frc.robot.commands.DumpBallCommand;
import frc.robot.commands.TurnToAngleCommand;
import frc.robot.commands.SetDistanceCommand;
import frc.robot.subsystems.BallTransitSubsystem;
import frc.robot.subsystems.ClimberSubsystem;
import frc.robot.subsystems.DriveSubsystem;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.RunCommand;
import edu.wpi.first.wpilibj2.command.StartEndCommand;
import edu.wpi.first.wpilibj2.command.button.JoystickButton;

/**
 * This class is where the bulk of the robot should be declared. Since
 * Command-based is a
 * "declarative" paradigm, very little robot logic should actually be handled in
 * the {@link Robot}
 * periodic methods (other than the scheduler calls). Instead, the structure of
 * the robot (including
 * subsystems, commands, and button mappings) should be declared here.
 */
public class RobotContainer {
  // The robot's subsystems and commands are defined here...
  private final DriveSubsystem driveSubsystem = new DriveSubsystem();
  private final ClimberSubsystem climberSubsystem = new ClimberSubsystem();
  private final BallTransitSubsystem ballTransitSubsystem = new BallTransitSubsystem();
  private final AutoNav m_autoCommand = new AutoNav(driveSubsystem);

  public static final Joystick driverStick = new Joystick(0);
  public static final Joystick operatorStick = new Joystick(1);

  public RobotContainer() {
    // Configure the button bindings
    driveSubsystem.setDefaultCommand(new RunCommand(
        () -> driveSubsystem.manualDrive(-1 * (driverStick.getY()), driverStick.getX(),
            DriveConstants.scaleTurn, DriveConstants.scaleY),
        driveSubsystem));
    configureButtonBindings();
  }

  /*
   * The buttons will be shecduled before the default Command and
   * if the buttons that are activated have the same subsystem requirements as the
   * default
   * command,
   * the defalut command will not run
   */
  /*
   * Also, be careful about brownouts (if to many things are running at once it
   * will cause it), especailly if your running PID on motors and neumatics, so
   * make sure it
   * gets disabled after use execept drive
   */
  private void configureButtonBindings() {

    new JoystickButton(driverStick, Buttons.inverseControl).whileHeld(new RunCommand(
        () -> driveSubsystem.manualDrive(driverStick.getY(), -driverStick.getX(),
            DriveConstants.scaleTurnSlow, DriveConstants.scaleYSlow),
        driveSubsystem));

    JoystickButton driveBoostButton = new JoystickButton(driverStick, Buttons.driveBoostToggle);
    driveBoostButton.whileHeld(new RunCommand(
        () -> driveSubsystem.manualDrive(-1 * (driverStick.getY()), driverStick.getX(),
            DriveConstants.scaleTurnBoost, DriveConstants.scaleYBoost),
        driveSubsystem));

    new JoystickButton(driverStick, Buttons.driveSlowToggle).whileHeld(new RunCommand(
        () -> driveSubsystem.manualDrive(-1 * (driverStick.getY()), driverStick.getX(),
            DriveConstants.scaleTurnSlow, DriveConstants.scaleYSlow),
        driveSubsystem));

    // Turn to Angle Buttons
    new JoystickButton(driverStick, Buttons.turn45Toggle).whileHeld(new TurnToAngleCommand(driveSubsystem, 45));
    new JoystickButton(driverStick, Buttons.turn90Toggle).whileHeld(new TurnToAngleCommand(driveSubsystem, 90));
    new JoystickButton(driverStick, Buttons.turn180Toggle).whileHeld(new TurnToAngleCommand(driveSubsystem, 180));
    new JoystickButton(driverStick, Buttons.turnWithJoyStick)
        .whileHeld(new TurnToAngleCommand(driveSubsystem, driverStick.getDirectionDegrees()));

    new JoystickButton(operatorStick, Buttons.climberButton)
        .whileHeld(new ClimberCommand(climberSubsystem));
    new JoystickButton(operatorStick, Buttons.dumpBallToggle).whenPressed(new DumpBallCommand(ballTransitSubsystem));
  }

  /**
   * Use this to pass the autonomous command to the main {@link Robot} class.
   *
   * @return the command to run in autonomous
   */
  public Command getAutonomousCommand() {
    // An ExampleCommand will run in autonomous
    return m_autoCommand;
  }
}
