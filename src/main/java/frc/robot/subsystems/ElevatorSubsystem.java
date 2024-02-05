/* Copyright (C) 2024 Team 334. All Rights Reserved.*/
package frc.robot.subsystems;

import com.revrobotics.CANSparkLowLevel.MotorType;
import com.revrobotics.CANSparkMax;
import edu.wpi.first.math.controller.ElevatorFeedforward;
import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants;
import frc.robot.utils.UtilFuncs;
import frc.robot.utils.configs.NeoConfig;

/** @author Peter Gutkovich */
public class ElevatorSubsystem extends SubsystemBase {
  private final CANSparkMax _leftMotor = new CANSparkMax(Constants.CAN.ELEVATOR_LEFT, MotorType.kBrushless);
  private final CANSparkMax _rightMotor = new CANSparkMax(Constants.CAN.ELEVATOR_RIGHT, MotorType.kBrushless);

  private final ElevatorFeedforward _elevatorFeed = new ElevatorFeedforward(0, Constants.FeedForward.ELEVATOR_KG, 0);
  private final ElevatorFeedforward _climbFeed = new ElevatorFeedforward(0, 0, 0); // TODO: Get this value

  private final PIDController _heightController = new PIDController(Constants.PID.ELEVATOR_KP, 0, 0);

  private boolean _usingElevatorFeed = true;

  /** Creates a new ElevatorSubsystem . */
  public ElevatorSubsystem() {
    NeoConfig.configureNeo(_leftMotor, true);
    NeoConfig.configureFollowerNeo(_leftMotor, _rightMotor, true);
  }

  @Override
  public void periodic() {
    // This method will be called once per scheduler run
    // harry chen code maybe fix
    // setMotor(elevatorFeed.calculate(0));
  }

  /** Returns true if the elevator is at the last desired height setpoint. */
  public boolean atDesiredHeight() {
    return _heightController.atSetpoint();
  }

  /** Sets the height of the elevator in meters. MUST be called repeatedly. */
  public void setElevatorHeight(double heightMeters) {
    driveElevator(_heightController.calculate(getElevatorHeight(), heightMeters));
  }

  /** Get the height of the elevator in meters. */
  public double getElevatorHeight() {
    return 0.00;
  }

  /**
   * Drives the elevator at a desired percent output (feedforward is included).
   */
  public void driveElevator(double speed) {
    double out;

    if (_usingElevatorFeed)
      out = _elevatorFeed.calculate(0);
    else {
      out = _climbFeed.calculate(0);
    }

    _leftMotor.set(UtilFuncs.FromVolts(out) + speed);
  }

  /** Stops elevator movement. */
  public void stopElevator() {
    driveElevator(0);
  }

  public void changeElevatorFeed() {
    _usingElevatorFeed = !_usingElevatorFeed;
  }
}
