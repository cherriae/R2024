// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.commands.swerve;

import java.util.function.DoubleSupplier;

import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.Constants;
import frc.robot.subsystems.SwerveDriveSubsystem;
import frc.robot.utils.UtilFuncs;

/**
 * Drive the swerve chassis based on teleop joystick input
 * 
 * @author Peter Gutkovich
 * @author Elvis Osmanov
 */
public class TeleopDrive extends Command {
  private final SwerveDriveSubsystem _swerveDrive;

  private final DoubleSupplier _xSpeed;
  private final DoubleSupplier _ySpeed;

  private final DoubleSupplier _rotationSpeed;

  /** Creates a new TeleopDrive. */
  public TeleopDrive(SwerveDriveSubsystem swerveDrive, DoubleSupplier xSpeed, DoubleSupplier ySpeed, DoubleSupplier rotationSpeed) {
    _swerveDrive = swerveDrive;

    _xSpeed = xSpeed;
    _ySpeed = ySpeed;

    _rotationSpeed = rotationSpeed;

    // Use addRequirements() here to declare subsystem dependencies.
    addRequirements(swerveDrive);
  }

  // Called when the command is initially scheduled.
  @Override
  public void initialize() {
  }

  // Called every time the scheduler runs while the command is scheduled.
  @Override
  public void execute() {
    // apply controller deadband
    double xSpeed = UtilFuncs.ApplyDeadband(_xSpeed.getAsDouble(), 0.1);
    double ySpeed = UtilFuncs.ApplyDeadband(_ySpeed.getAsDouble(), 0.1);
    double rotationSpeed = UtilFuncs.ApplyDeadband(_rotationSpeed.getAsDouble(), 0.1);

    // drive the swerve chassis subsystem
    _swerveDrive.driveChassis(new ChassisSpeeds(
      xSpeed * Constants.Speeds.SWERVE_DRIVE_MAX_SPEED,
      ySpeed * Constants.Speeds.SWERVE_DRIVE_MAX_SPEED,
      rotationSpeed * Constants.Speeds.SWERVE_DRIVE_MAX_ANGULAR_SPEED
    ));
  }

  // Called once the command ends or is interrupted.
  @Override
  public void end(boolean interrupted) {}

  // Returns true when the command should end.
  @Override
  public boolean isFinished() {
    return false;
  }
}