/*                                  Team 334                                  */
/* Copyright (c) 2024 Team 334. All Rights Reserved.                          */

package frc.robot.subsystems;

import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants;

import com.revrobotics.CANSparkMax;
import com.revrobotics.CANSparkLowLevel.MotorType;

public class ShooterSubsystem extends SubsystemBase {
  private CANSparkMax _leftMotor = new CANSparkMax(Constants.CAN.SHOOTER_LEFT, MotorType.kBrushless); 
  private CANSparkMax _rightMotor = new CANSparkMax(Constants.CAN.SHOOTER_RIGHT, MotorType.kBrushless); 

  /** Creates a new ShooterSubsystem. */
  public ShooterSubsystem() {
  }



  @Override
  public void periodic() {
    // This method will be called once per scheduler run
  }

  public void spinMotor(){
    _leftMotor.set(-1.0);
    _rightMotor.set(1.0);
  }

  public void stopMotors(){
    _leftMotor.set(0);
    _rightMotor.set(0);
  }
}