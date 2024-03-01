/* Copyright (C) 2024 Team 334. All Rights Reserved.*/
package frc.robot.commands.shooter;

import java.util.function.DoubleSupplier;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.wpilibj.DriverStation.Alliance;
import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.Constants;
import frc.robot.Constants.FieldConstants;
import frc.robot.subsystems.ElevatorSubsystem;
import frc.robot.subsystems.IntakeSubsystem;
import frc.robot.subsystems.LEDSubsystem;
import frc.robot.subsystems.ShooterSubsystem;
import frc.robot.subsystems.SwerveDriveSubsystem;
import frc.robot.subsystems.VisionSubsystem;
import frc.robot.utils.UtilFuncs;

/**
 * @author Elvis Osmanov
 * @author Peter Gutkovich
 * @author Cherine Soewingjo
 */
public class AutoAim extends Command {
  private final ShooterSubsystem _shooter;
  private final SwerveDriveSubsystem _swerve;
  private final VisionSubsystem _vision;
  private final IntakeSubsystem _intake;
  private final ElevatorSubsystem _elevator;
  private final LEDSubsystem _leds;

  private final DoubleSupplier _xSpeed;
  private final DoubleSupplier _ySpeed;

  private boolean _reachedSwerveHeading;
  private boolean _reachedShooterAngle;

  private boolean _runOnce;

  private PIDController _headingController = new PIDController(Constants.PID.SWERVE_HEADING_KP, 0,
      Constants.PID.SWERVE_HEADING_KD);

  /** Creates a new AutoAim. */
  public AutoAim(ShooterSubsystem shooter, LEDSubsystem leds, SwerveDriveSubsystem swerve,
      DoubleSupplier xSpeed, DoubleSupplier ySpeed, VisionSubsystem vision, IntakeSubsystem intake, ElevatorSubsystem elevator) {
    // Use addRequirements() here to declare subsystem dependencies.
    _leds = leds;
    _shooter = shooter;
    _swerve = swerve;

    _xSpeed = xSpeed;
    _ySpeed = ySpeed;

    _runOnce = false;

    _headingController.setTolerance(2);
    _headingController.enableContinuousInput(-180, 180);

    _vision = vision;
    _intake = intake;
    _elevator = elevator;

    addRequirements(_shooter, _swerve, _leds);
  }

  /** Creates an auton AutoAim that ends when it reaches the first setpoints. */
  public AutoAim(LEDSubsystem leds, ShooterSubsystem shooter, SwerveDriveSubsystem swerve, VisionSubsystem vision, IntakeSubsystem intake, ElevatorSubsystem elevator) {
    this(shooter, leds, swerve, () -> 0, () -> 0, vision, intake, elevator);

    _runOnce = true;
  }

  // Called when the command is initially scheduled.
  @Override
  public void initialize() {
    _reachedSwerveHeading = false;
    _reachedShooterAngle = false;
  }

  // Called every time the scheduler runs while the command is scheduled.
  @Override
  public void execute() {
    double currentSwerveHeading = _swerve.getHeading().getDegrees();
    double desiredSwerveHeading = _swerve.speakerAngles()[0];
    int speakerAprilTag = UtilFuncs.GetAlliance() == Alliance.Red ? 4 : 7;

    double rotationVelocity = MathUtil.clamp(
        _headingController.calculate(currentSwerveHeading, desiredSwerveHeading),
        -Constants.Speeds.SWERVE_DRIVE_MAX_ANGULAR_SPEED * 2,
        Constants.Speeds.SWERVE_DRIVE_MAX_ANGULAR_SPEED * 2);

    _reachedSwerveHeading = _headingController.atSetpoint();
    
    if (_vision.isApriltagVisible(speakerAprilTag)){
      _shooter.setAngle(_vision.tagAngleOffsets(speakerAprilTag)[1]); // Might need an angle offset prob better without limelight
    }
    else{
      _shooter.setAngle(_swerve.speakerAngles()[1]);
    }

    _intake.setAngle(_shooter.getAngle());

    if (_reachedSwerveHeading)
      rotationVelocity = 0; // to prevent oscillation

    if (_reachedSwerveHeading && _reachedShooterAngle) {
      _leds.setColor(Constants.LEDColors.GREEN);
    } else {
      _leds.blink(Constants.LEDColors.YELLOW, Constants.LEDColors.NOTHING, 0.2);
    }

    _swerve.driveChassis(new ChassisSpeeds(
        _xSpeed.getAsDouble() * Constants.Speeds.SWERVE_DRIVE_MAX_SPEED * Constants.Speeds.SWERVE_DRIVE_COEFF,
        _ySpeed.getAsDouble() * Constants.Speeds.SWERVE_DRIVE_MAX_SPEED * Constants.Speeds.SWERVE_DRIVE_COEFF,
        rotationVelocity));
  }

  // Called once the command ends or is interrupted.
  @Override
  public void end(boolean interrupted) {}

  // Returns true when the command should end.
  @Override
  public boolean isFinished() {
    return _runOnce && _reachedSwerveHeading && _reachedShooterAngle;
  }
}
