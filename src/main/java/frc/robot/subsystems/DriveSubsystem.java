// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems;

import javax.imageio.ImageTypeSpecifier;

import com.kauailabs.navx.frc.AHRS;
import com.revrobotics.CANSparkMax;
import com.revrobotics.RelativeEncoder;
import com.revrobotics.SparkMaxPIDController;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;
import edu.wpi.first.wpilibj.SerialPort;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj.motorcontrol.MotorController;
import edu.wpi.first.wpilibj.motorcontrol.MotorControllerGroup;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants;
import frc.robot.RobotContainer;

public class DriveSubsystem extends SubsystemBase {
  public final CANSparkMax leftFrontMotor = new CANSparkMax(Constants.leftFrontMotorID, MotorType.kBrushless);
  public final CANSparkMax leftBackMotor = new CANSparkMax(Constants.leftBackMotorID, MotorType.kBrushless);
  public final CANSparkMax rightFrontMotor = new CANSparkMax(Constants.rightFrontMotorID, MotorType.kBrushless);
  public final CANSparkMax rightBackMotor = new CANSparkMax(Constants.rightBackMotorID, MotorType.kBrushless);

  public RelativeEncoder m_leftFrontEncoder = leftFrontMotor.getEncoder();
  public RelativeEncoder leftBackEncoder = leftBackMotor.getEncoder();
  public RelativeEncoder m_rightFrontEncoder = rightFrontMotor.getEncoder();
  public RelativeEncoder rightBackEncoder = rightBackMotor.getEncoder();

  private SparkMaxPIDController leftFrontPIDCon = leftFrontMotor.getPIDController();
  private SparkMaxPIDController leftBackPIDCon = leftBackMotor.getPIDController();
  private SparkMaxPIDController rightFrontPIDCon = rightFrontMotor.getPIDController();
  private SparkMaxPIDController rightBackPIDCon = rightBackMotor.getPIDController();

  int smartMotionSlot = 0;
  int allowedErr;
  int minVel;
  double kP = 4e-4;
  double kI = 0;
  double kD = 0;
  double kIz = 0;
  double kFF = 0.000456;
  double kMaxOutput = 1;
  double kMinOutput = -1;
  double maxRPM = 5700;
  double maxVel = 4000;
  double maxAcc = 1500;
  double setPointDrive = 0;
  // The gyro sensor
  private final AHRS m_gyro = new AHRS(SerialPort.Port.kUSB1);

  public DriveSubsystem() {
    m_gyro.reset();
    leftFrontMotor.restoreFactoryDefaults();
    leftBackMotor.restoreFactoryDefaults();
    rightFrontMotor.restoreFactoryDefaults();
    rightBackMotor.restoreFactoryDefaults();
    initializePID(leftFrontPIDCon, m_leftFrontEncoder);
    initializePID(leftBackPIDCon, leftBackEncoder);
    initializePID(rightFrontPIDCon, m_rightFrontEncoder);
    initializePID(rightBackPIDCon, rightBackEncoder);

  }
  /**
   * Specifically meant to turn robot a certain number of degrees
   * @param y Veritical motion
   * @param rot turn motion
   */

  public void manualDrive(double y, double x, double scaleX, double scaleY){
    leftFrontPIDCon.setReference(setPointLeft(y, x, scaleX, scaleY) , CANSparkMax.ControlType.kSmartVelocity);
    leftBackPIDCon.setReference(setPointLeft(y, x, scaleX, scaleY) , CANSparkMax.ControlType.kSmartVelocity);
    rightFrontPIDCon.setReference(setPointRight(y, x, scaleX, scaleY), CANSparkMax.ControlType.kSmartVelocity);
    rightBackPIDCon.setReference(setPointRight(y, x, scaleX, scaleY), CANSparkMax.ControlType.kSmartVelocity);
  }

  public double setPointLeft(double Jy, double Jx, double scale1, double scale2){
    double yScale = ((Jy) * scale2);
    double xScale = (Jx) * scale1;
    //add if to curve backwards for left
    if (Jy == 0 & Jx ==0){
      return 0;
    }
    return xScale + yScale;
  }

  public double setPointRight(double Jy, double Jx, double scale1, double scale2){
    double xScale = (-(Jx) * scale1);
    double yScale = ((Jy) * scale2);
    //add if to curve backwards for right
    if (Jy == 0 & Jx ==0){
      return 0;
    }
    return  -1*(xScale + yScale);
  }

  public void autoDrive(double displacement, double angle, double scaleLeft, double scaleRight){
    leftFrontPIDCon.setReference(autoSetPointLeft(displacement, angle, scaleLeft), CANSparkMax.ControlType.kSmartMotion);
    leftBackPIDCon.setReference(autoSetPointLeft(displacement, angle, scaleLeft), CANSparkMax.ControlType.kSmartMotion);
    rightFrontPIDCon.setReference(autoSetPointRight(displacement, angle, scaleRight), CANSparkMax.ControlType.kSmartMotion);
    rightBackPIDCon.setReference(autoSetPointRight(displacement, angle, scaleRight), CANSparkMax.ControlType.kSmartMotion);
  }

  /**
   * Sets how far encoders need to move
   * @param setPointMotor The displacement of the robot to the ball
   * @param angle The angle from the field (pointing torwards the rump starting at zero) to the ball
   * @param scale The number you want to muilply the angleError with to increase or decrease setPoint
   * @return if angleError is greater then zero then add to the sestpoint by angleError times scale else setpoint
   */
  public double autoSetPointLeft(double displacement, double angle, double scale){
   /* if (angleError(angle) > 0){
      return (angleError(angle) * scale) + displacement;
    }*/
    return displacement;
  }
  /**
   * Sets how far encoders need to move
   * @param setPoint The displacement of the robot to the ball
   * @param angle The angle from the field (pointing torwards the rump starting at zero) to the ball
   * @param scale The number you want to muilply the angleError with to increase or decrease setPoint
   * @return if angleError is less then zero then add to the sestpoint by angleError times scale else setpoint
   */
  public double autoSetPointRight(double displacement, double angle, double scale){
    /*if (angleError(angle) < 0){
      return (angleError(angle) * scale) + displacement;
    }*/
    return displacement;
  }
  //Neeed to get rid of this soon
  public double angleError(double expectedAngle){
    return Math.IEEEremainder(expectedAngle, 360) - Math.IEEEremainder(m_gyro.getAngle(), 360);
  }

  public void initializePID(SparkMaxPIDController p, RelativeEncoder h){
    p.setP(kP);
    p.setI(kI);
    p.setD(kD);
    p.setIZone(kIz);
    p.setFF(kFF);
    p.setOutputRange(kMinOutput, kMaxOutput);
    p.setSmartMotionMaxVelocity(maxVel, smartMotionSlot);
    p.setSmartMotionMinOutputVelocity(minVel, smartMotionSlot);
    p.setSmartMotionMaxAccel(maxAcc, smartMotionSlot);
    p.setSmartMotionAllowedClosedLoopError(allowedErr, smartMotionSlot);
    h.setPositionConversionFactor(Constants.conversionPosition);
    h.setVelocityConversionFactor(Constants.conversionVelocity);
  }
  @Override
  public void periodic() {
    double processVariable = leftBackEncoder.getVelocity();
    // This method will be called once per scheduler run
    SmartDashboard.putNumber("Postion", leftBackEncoder.getPosition());
    SmartDashboard.putNumber("Velocity", leftBackEncoder.getVelocity());
    SmartDashboard.putNumber("Joystick x", RobotContainer.driverStick.getX());
    SmartDashboard.putNumber("Joystick y", RobotContainer.driverStick.getY());
    SmartDashboard.putNumber("Process Variable", processVariable);
    SmartDashboard.putNumber("Output", leftBackMotor.getAppliedOutput());
  }

  @Override
  public void simulationPeriodic() {
    // This method will be called once per scheduler run during simulation
  }
}
