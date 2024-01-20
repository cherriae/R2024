/*                                  Team 334                                  */
/* Copyright (c) 2024 Team 334. All Rights Reserved.                          */

package frc.robot.subsystems;

import com.ctre.phoenix6.Orchestra;
import com.pathplanner.lib.auto.AutoBuilder;
import com.pathplanner.lib.util.HolonomicPathFollowerConfig;
import com.pathplanner.lib.util.PIDConstants;
import com.pathplanner.lib.util.ReplanningConfig;
import edu.wpi.first.math.VecBuilder;
import edu.wpi.first.math.estimator.SwerveDrivePoseEstimator;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.kinematics.SwerveModulePosition;
import edu.wpi.first.math.kinematics.SwerveModuleState;
import edu.wpi.first.wpilibj.BuiltInAccelerometer;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.DriverStation.Alliance;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.smartdashboard.Field2d;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants;
import frc.robot.utils.BNO055;
import frc.robot.utils.SwerveModule;
import java.util.Optional;

/**
 * @author Peter Gutkovich
 * @author Elvis Osmanov
 * @author Cherine Soewignjo
 * @author Peleh Liu
 */
public class SwerveDriveSubsystem extends SubsystemBase {
  // each swerve module
  private final SwerveModule _frontLeft =
      new SwerveModule(
          Constants.CAN.DRIVE_FRONT_LEFT,
          Constants.CAN.ROT_FRONT_LEFT,
          Constants.CAN.ENC_FRONT_LEFT,
          Constants.Offsets.ENCODER_FRONT_LEFT,
          .05,
          0.15);
  private final SwerveModule _frontRight =
      new SwerveModule(
          Constants.CAN.DRIVE_FRONT_RIGHT,
          Constants.CAN.ROT_FRONT_RIGHT,
          Constants.CAN.ENC_FRONT_RIGHT,
          Constants.Offsets.ENCODER_FRONT_RIGHT,
          .05,
          0.17);
  private final SwerveModule _backRight =
      new SwerveModule(
          Constants.CAN.DRIVE_BACK_RIGHT,
          Constants.CAN.ROT_BACK_RIGHT,
          Constants.CAN.ENC_BACK_RIGHT,
          Constants.Offsets.ENCODER_BACK_RIGHT,
          .05,
          0.18);
  private final SwerveModule _backLeft =
      new SwerveModule(
          Constants.CAN.DRIVE_BACK_LEFT,
          Constants.CAN.ROT_BACK_LEFT,
          Constants.CAN.ENC_BACK_LEFT,
          Constants.Offsets.ENCODER_BACK_LEFT,
          .05,
          0.17);

  private final BNO055 _gyro =
      BNO055.getInstance(BNO055.opmode_t.OPERATION_MODE_IMUPLUS, BNO055.vector_type_t.VECTOR_EULER);

  private VisionSubsystem _visionSubsystem;

  private double _robotSpeed = 0;

  Orchestra _orchestra = new Orchestra();
  String song = "output.chrp";

  // estimated pose
  private Pose2d _pose = new Pose2d();

  private Field2d _field = new Field2d();

  /** A boolean for whether the swerve is field oriented or not. */
  public boolean fieldOriented = false;

  private final BuiltInAccelerometer _imu = new BuiltInAccelerometer();

  // Pose Estimator -> Has built in odometry and uses supplied vision measurements
  private final SwerveDrivePoseEstimator _estimator =
      new SwerveDrivePoseEstimator(
          Constants.Physical.SWERVE_KINEMATICS,
          getHeadingRaw(),
          new SwerveModulePosition[] {
            _frontLeft.getPosition(),
            _frontRight.getPosition(),
            _backRight.getPosition(),
            _backLeft.getPosition()
          },
          new Pose2d(),
          VecBuilder.fill(0.008, 0.008, 0.0075),
          VecBuilder.fill(0.2, .2, .75));

  /** Return the estimated pose of the swerve chassis. */
  public Pose2d getPose() {
    return _pose;
  }

  /** Get the drive's chassis speeds (robot relative). */
  public ChassisSpeeds getRobotRelativeSpeeds() {
    return Constants.Physical.SWERVE_KINEMATICS.toChassisSpeeds(
        _frontLeft.getState(), _frontRight.getState(), _backRight.getState(), _backLeft.getState());
  }

  /** Creates a new SwerveDriveSubsystem. */
  public SwerveDriveSubsystem(VisionSubsystem visionSubsystem) {
    _visionSubsystem = visionSubsystem;

    SwerveModule[] modules = new SwerveModule[4];
    modules[0] = _frontLeft;
    modules[1] = _frontRight;
    modules[2] = _backRight;
    modules[3] = _backLeft;

    // for (int i = 0; i < modules.length; i++) {
    //   for (int j = 0; j < 2; j++) {
    //     _orchestra.addInstrument(modules[i].returnTalons()[j]);
    //   }
    // }

    // _orchestra.loadMusic(song);

    // _orchestra.play();

    // pathplannerlib setup
    AutoBuilder.configureHolonomic(
        this::getPose, 
        this::resetPose,
        this::getRobotRelativeSpeeds,
        this::driveChassis,
        new HolonomicPathFollowerConfig(
            new PIDConstants(2.5, 0, 0),
            new PIDConstants(2.8, 0, 0),
            Constants.Speeds.SWERVE_DRIVE_MAX_SPEED,
            Constants.Physical.SWERVE_DRIVE_BASE_RADIUS,
            new ReplanningConfig()),
        () -> {
          Optional<Alliance> alliance = DriverStation.getAlliance();

          if (alliance.isPresent()) {
            return alliance.get() == DriverStation.Alliance.Red;
          }
          return false;
        },
        this);
  }

  @Override
  public void periodic() {
    // This method will be called once per scheduler run
    SmartDashboard.putNumber("Gyro", getHeading().getDegrees());
    SmartDashboard.putBoolean("Field Oriented", fieldOriented);

    SmartDashboard.putNumber("Front Left Angle", _frontLeft.getAngle());
    SmartDashboard.putNumber("Front Right Angle", _frontRight.getAngle());
    SmartDashboard.putNumber("Back Left Angle", _backLeft.getAngle());
    SmartDashboard.putNumber("Back Right Angle", _backRight.getAngle());

    // SmartDashboard.putNumber("IMU SPEED", _gyro.);
    SmartDashboard.putNumber("Front Left Velocity", _frontLeft.getDriveVelocity());
    SmartDashboard.putNumber("Front Right Velocity", _frontRight.getDriveVelocity());
    SmartDashboard.putNumber("Back Left Velocity", _backLeft.getDriveVelocity());
    SmartDashboard.putNumber("Back Right Velocity", _backRight.getDriveVelocity());


    // Update the bot's pose
    _pose =
        _estimator.update(
            getHeadingRaw(),
            new SwerveModulePosition[] {
              _frontLeft.getPosition(),
              _frontRight.getPosition(),
              _backRight.getPosition(),
              _backLeft.getPosition()
            });

    if (_visionSubsystem.isApriltagVisible()) {
      _estimator.addVisionMeasurement(_visionSubsystem.getBotpose(), Timer.getFPGATimestamp());
    }

    _field.setRobotPose(_pose);
    SmartDashboard.putData("FIELD", _field);

    _robotSpeed =
        Math.sqrt(
            Math.pow(getRobotRelativeSpeeds().vxMetersPerSecond, 2)
                + Math.pow(getRobotRelativeSpeeds().vyMetersPerSecond, 2));

    // SmartDashboard.putNumber("ACTUAL X SPEED", getRobotRelativeSpeeds().vxMetersPerSecond);
    // SmartDashboard.putNumber("ACTUAL Y SPEED", getRobotRelativeSpeeds().vyMetersPerSecond);
  }

  /**
   * Set the chassis speed of the swerve drive.
   *
   * <p>Chassis speed will be treated as field oriented if the fieldOriented class attribute is set
   * to true, otherwise it will be robot-relative.
   *
   * @see ChassisSpeeds (wpilib chassis speeds class)
   */
  public void driveChassis(ChassisSpeeds chassisSpeeds) {
    // IMPORTANT: X-axis and Y-axis are flipped (based on wpilib coord system)
    if (fieldOriented) {
      chassisSpeeds = ChassisSpeeds.fromFieldRelativeSpeeds(chassisSpeeds, getHeading());
    }

    SwerveModuleState[] moduleStates =
        Constants.Physical.SWERVE_KINEMATICS.toSwerveModuleStates(chassisSpeeds);
    setStates(moduleStates);
  }

  /**
   * Sets the state of each SwerveModule through an array.
   *
   * <p>Order -> front left, front right, back right, back left
   */
  public void setStates(SwerveModuleState[] states) {
    _frontLeft.setState(states[0]);
    _frontRight.setState(states[1]);
    _backRight.setState(states[2]);
    _backLeft.setState(states[3]);
  }

  /** Resets the pose estimator's heading of the drive to 0. */
  public void resetGyro() {
    Pose2d new_pose =
        new Pose2d(
            _pose.getTranslation().getX(),
            _pose.getTranslation().getY(),
            Rotation2d.fromDegrees(0));

    resetPose(new_pose);
  }

  /** Resets pose estimator's translation of the drive to (0, 0). */
  public void resetTranslation() {
    Pose2d new_pose = new Pose2d(0, 0, _pose.getRotation());

    resetPose(new_pose);
  }

  /** Resets the pose estimator to the supplied new pose. */
  public void resetPose(Pose2d newPose) {
    _estimator.resetPosition(
        getHeadingRaw(),
        new SwerveModulePosition[] {
          _frontLeft.getPosition(),
          _frontRight.getPosition(),
          _backRight.getPosition(),
          _backLeft.getPosition()
        },
        newPose);
  }

  /** Get heading of the drive from the pose estimator. */
  public Rotation2d getHeading() {
    return _estimator.getEstimatedPosition().getRotation();
  }

  /** Get heading DIRECTLY from the BNO055 gyro as a Rotation2d. */
  public Rotation2d getHeadingRaw() {
    return Rotation2d.fromDegrees(-Math.IEEEremainder(_gyro.getHeading(), 360));
  }
}
