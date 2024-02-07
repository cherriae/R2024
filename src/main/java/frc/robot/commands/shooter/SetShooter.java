/* Copyright (C) 2024 Team 334. All Rights Reserved.*/
package frc.robot.commands.shooter;

import java.util.function.DoubleSupplier;

import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.subsystems.ShooterSubsystem;

/**
 * @author Elvis Osmanov
 * @author Harry Chen
 * @author Peter Gutkovich
 */
public class SetShooter extends Command {
  /** Creates a new AngleShooter. */
  private ShooterSubsystem _shooter;

  private DoubleSupplier _angle;

  public SetShooter(ShooterSubsystem shooter, DoubleSupplier angle) {
    // Use addRequirements() here to declare subsystem dependencies.
    _shooter = shooter;
    _angle = angle;
    addRequirements(_shooter);
  }

  // Called when the command is initially scheduled.
  @Override
  public void initialize() {
  }

  // Called every time the scheduler runs while the command is scheduled.
  @Override
  public void execute() {
    // _shooter.setAngle(_angle.getAsDouble());
    _shooter.setAngle(0);
  }

  // Called once the command ends or is interrupted.
  @Override
  public void end(boolean interrupted) {
    _shooter.stopAngle();
  }

  // Returns true when the command should end.
  @Override
  public boolean isFinished() {
    return _shooter.atDesiredAngle();
  }
}