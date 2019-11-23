package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

@TeleOp(name="TetrixChasisTeleop", group="Iterative Opmode")
public class TetrixChasisTeleop extends OpMode
{
    private JoystickCalc Jc = null;
    public UDC_Teleop Teleop = null;
    public ArmAttachmentTetrix arm;
    Gripper gripper;

    private double DriveSpeedMultiplier;
    private double TurnSpeedMultiplier;
    private double gripperPosition = 0.5;

    private double JoystickThreshold = 0.2;

    private int[] MaxMotorPositions = {0,0,0,0};
    private int[] PreviousMotorPositions = {0,0,0,0};
    private int[] TotalMotorClicks = {0,0,0,0};
    boolean FirstRun = true;

    @Override
    public void init()
    {
        telemetry.addData("start",5 );
        telemetry.update();
        gripperPosition = 0.5;

        Jc = new JoystickCalc(this);

        Teleop = new UDC_Teleop(this);
        Teleop.Init();

        arm = new ArmAttachmentTetrix(this);
        arm.Init();
        //set speeds:
        telemetry.addData("endstart",5 );

        telemetry.update();

        gripper = new Gripper(this);
        gripper.Init();
    }

    @Override
    public void start()
    {
        Teleop.Start();
    }

    @Override
    public void loop()
    {
        Teleop.Loop();
        //Update telemetry and get joystick input
        Jc.calculate();
        Teleop.UpdateTurnSpeed(Math.abs(Jc.rightStickX));
        //calculate the absolute value of the right x for turn speed
        double turnSpeed = Math.abs(Jc.rightStickX);

        //Reset Gyro if needed
        if(gamepad1.x)
        {
            Teleop.gyroOffset();
        }

        ManageDriveMovement();

        //switch between normal and slow modes
        if(gamepad1.left_bumper) { Teleop.fullSpeed(); }
        if(gamepad1.right_bumper) { Teleop.twoThirdsSpeed(); }
        if(gamepad1.left_trigger>0.2) { Teleop.halfSpeed(); }
        if(gamepad1.right_trigger>0.2) { Teleop.thirdSpeed(); }


        if(gamepad2.x){
            //pick up stone
        }
        else if (gamepad2.y){
            //put down stone
        }

        ManageArmMovement();
        ManageGripperMovement();

        telemetry.update();
    }

    public void ManageDriveMovement()//Manages general drive input
    {
        if(Jc.leftStickPower > JoystickThreshold) //Move
        {
            Teleop.chooseDirection(Jc.rightStickX, Jc.leftStickBaring, Jc.leftStickPower);
        }

        else if(Jc.rightStickX > JoystickThreshold) //Turn Right
        {
            Teleop.turnRight();
        }


        else if(Jc.rightStickX < -JoystickThreshold) //Turn Left
        {
            Teleop.turnLeft();
        }

        else //STOP
        {
            Teleop.stopWheels();
        }
    }

    public void ManageArmMovement()//Manages the Arm
    {
        if(gamepad2.dpad_up)//move lift up
        {
            arm.LiftUp();
        }
        else if(gamepad2.dpad_down)//move lift down
        {
            arm.LiftDown();
        }
        else//stop the arm from moving up or down
        {
            arm.LiftStopVertical();
        }

        if(gamepad2.dpad_left)////extend arm
        {
            arm.LiftExtend();
        }

        else if (gamepad2.dpad_right)//retract arm
        {
            arm.LiftRetract();
        }

        else//stop the arm from moving left or right
        {
            arm.LiftStopHorizontal();
        }
    }

    public void ManageGripperMovement()//Manages the Gripper
    {
        if(gamepad2.left_stick_x>JoystickThreshold)//rotate the gripper right
        {
            gripperRight();
        }
        else if(gamepad2.left_stick_x<JoystickThreshold)//rotate the gripper left
        {
            gripperLeft();
        }
        if(gamepad2.left_bumper)//Open the gripper
        {
            openGripper();
        }
        if(gamepad2.right_bumper)//close the gripper
        {
            closeGripper();
        }
        if(gamepad2.left_trigger>0.2)//Open the left side of the gripper
        {
            gripperOpenLeft();
        }
        if(gamepad2.right_trigger>0.2)//Open the right side of the gripper
        {
            gripperOpenRight();
        }
    }

    //Gripper Management Methods
    public void closeGripper(){
        gripper.GripperClose();
    }
    public void openGripper(){
        gripper.GripperOpen();
    }

    public void gripperLeft() {
        gripperPosition+=0.005;
        gripper.GripperRotatePosition(gripperPosition);
    }

    public void gripperRight() {
        gripperPosition -= 0.005;
        gripper.GripperRotatePosition(gripperPosition);
    }
    public void gripper1(){ gripperPosition = 1;}
    public void gripper0(){gripperPosition=0;}

    public void gripperOpenLeft(){
        gripper.GripperOpenLeft();
    }
    public void gripperOpenRight(){
        gripper.GripperOpenRight();
    }
}