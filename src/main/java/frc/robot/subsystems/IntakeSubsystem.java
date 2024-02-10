/* Copyright (C) 2024 Team 334. All Rights Reserved.*/
package frc.robot.subsystems;

import com.revrobotics.CANSparkLowLevel.MotorType;
import com.revrobotics.CANSparkMax;
import com.revrobotics.RelativeEncoder;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants;
import frc.robot.utils.configs.NeoConfig;

/**
 * @author Peter Gutkovich
 */
public class IntakeSubsystem extends SubsystemBase {
  private final CANSparkMax _feedMotor, _actuatorMotor;
  private final PIDController _actuatorController = new PIDController(0, 0, 0);

  private final RelativeEncoder _actuatorEncoder;

  /** How to feed (in or out). */
  public enum FeedMode {
    INTAKE, OUTTAKE, NONE
  }

  /** The actuator state (out or stowed). */
  public enum ActuatorState {
    STOWED, OUT, NONE
  }

  /** Creates a new IntakeSubsystem. */
  public IntakeSubsystem() {
    _feedMotor = new CANSparkMax(Constants.CAN.INTAKE_FEED, MotorType.kBrushless);
    _actuatorMotor = new CANSparkMax(Constants.CAN.INTAKE_ACTUATOR, MotorType.kBrushless);

    _actuatorEncoder = _actuatorMotor.getEncoder();
    _actuatorEncoder.setPosition(0);

    _actuatorController.setTolerance(0.5);

    NeoConfig.configureNeo(_feedMotor, true);
    NeoConfig.configureNeo(_actuatorMotor, false);
  }


  /**
   * Returns true if the actuator is at the last desired state.
   */
  public boolean atDesiredActuatorState() {
    return _actuatorController.atSetpoint();
  }

  /**
   * Get the actuator's encoder position.
   */
  public double getActuator() {
    return _actuatorEncoder.getPosition();
  }

  // FOR TESTING ONLY
  public void actuate(double speed) {
    _actuatorMotor.set(speed);
  }

  /**
   * Set the actuator's state. MUST BE CALLED REPEATEDLY.
   *
   * @param actuatorState
   *            The state to set the actuator to.
   */
  public void actuate(ActuatorState actuatorState) {
    double out = 0;

    switch (actuatorState) {
      case STOWED :
        out = MathUtil.clamp(
          _actuatorController.calculate(getActuator(), Constants.Encoders.INTAKE_STOWED),
          -Constants.Speeds.INTAKE_ACTUATE_MAX_SPEED,
          Constants.Speeds.INTAKE_ACTUATE_MAX_SPEED
        );
        break;

      case OUT :
        out = MathUtil.clamp(
          _actuatorController.calculate(getActuator(), Constants.Encoders.INTAKE_OUT),
          -Constants.Speeds.INTAKE_ACTUATE_MAX_SPEED,
          Constants.Speeds.INTAKE_ACTUATE_MAX_SPEED
        );
        break;

      case NONE:
        out = 0;
        break;
      
      default:
        break;
    }

    _actuatorMotor.set(out);
  }

  // for testing
  public void feed(double s) {
    _feedMotor.set(s);
  }

  /**
   * Feed in/out of the intake.
   *
   * @param feedMode
   *            How to feed.
   */
  public void feed(FeedMode feedMode) {
    switch (feedMode) {
      case INTAKE :
        _feedMotor.set(Constants.Speeds.INTAKE_FEED_MAX_SPEED);
        break;

      case OUTTAKE :
        _feedMotor.set(-Constants.Speeds.INTAKE_FEED_MAX_SPEED);
        break;

      case NONE :
        _feedMotor.set(0);
        break;

      default :
        break;
    }
  }

  @Override
  public void periodic() {
    // This method will be called once per scheduler run
    SmartDashboard.putNumber("ACTUATOR ENCODER", _actuatorEncoder.getPosition());
    SmartDashboard.putData("ACTUATOR PID", _actuatorController);
  }
}
