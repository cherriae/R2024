// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.commands;

import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.subsystems.SwerveDriveSubsystem;

/**
 * @author Peter Gutkovich
 */
public class ToggleSwerveOrient extends Command {
  private final SwerveDriveSubsystem _swerveDrive;

  /** Creates a new ToggleSwerveOrient. */
  public ToggleSwerveOrient(SwerveDriveSubsystem swerveDrive) {
    // Use addRequirements() here to declare subsystem dependencies.
    _swerveDrive = swerveDrive;

    addRequirements(swerveDrive);
  }

  // Called when the command is initially scheduled.
  @Override
  public void initialize() {
    _swerveDrive.fieldOriented = !_swerveDrive.fieldOriented;
  }

  // Called every time the scheduler runs while the command is scheduled.
  @Override
  public void execute() {}

  // Called once the command ends or is interrupted.
  @Override
  public void end(boolean interrupted) {}

  // Returns true when the command should end.
  @Override
  public boolean isFinished() {
    return true;
  }
}