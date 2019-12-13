package org.firstinspires.ftc.teamcode;

import android.app.Activity;
import android.graphics.Color;
import android.view.View;

import com.qualcomm.hardware.rev.RevTouchSensor;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.NormalizedColorSensor;
import com.qualcomm.robotcore.hardware.NormalizedRGBA;
import com.qualcomm.robotcore.hardware.SwitchableLight;

@TeleOp(name="TestCustomChassis", group="Linear Opmode")
public class TestCustomChassis extends OpMode
{
    private double HueThreshold = 15;
    private double RedHue = 0;
    private double BlueHue = 207;

    private JoystickCalc Jc = null;
    public UDC_Teleop Teleop = null;
    public ArmAttachmentCustom arm;
    private GripperCustom gripper;
    private ColorSensor colorSensor;

    private double DriveSpeedMultiplier;
    private double TurnSpeedMultiplier;

    private double JoystickThreshold = 0.2;

    private int[] MaxMotorPositions = {0,0,0,0};
    private int[] PreviousMotorPositions = {0,0,0,0};
    private int[] TotalMotorClicks = {0,0,0,0};
    boolean FirstRun = true;
    boolean stopping = false;

    private RevTouchSensor armRetractStop;

    @Override
    public void init()
    {
        Jc = new JoystickCalc(this, 180);//was 180

        Teleop = new UDC_Teleop(this, false);
        Teleop.Init();

        arm = new ArmAttachmentCustom(this);
        arm.Init();

        gripper = new GripperCustom(this);
        gripper.Init();

        armRetractStop = hardwareMap.get(RevTouchSensor.class, "ArmRetractStop");

        colorSensor = new ColorSensor(this);
        colorSensor.Init();
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
        gripper.Loop();
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

        //SwitchModes
        if(gamepad1.y)
        {
            Teleop.headlessMode = true;
        }
        if(gamepad1.b)
        {
            Teleop.headlessMode = false;
        }

        ManageDriveMovement();

        //switch between normal and slow modes
        if(gamepad1.left_bumper) { Teleop.fullSpeed(); }
        if(gamepad1.right_bumper) { Teleop.threeFourthsSpeed(); }
        if(gamepad1.left_trigger>0.2) { Teleop.halfSpeed(); }
        if(gamepad1.right_trigger>0.2) { Teleop.forthSpeed(); }

        if(gamepad2.x)
        {
            //pick up stone
        }
        else if (gamepad2.y)
        {
            //put down stone
        }

        if(gamepad2.left_stick_y>JoystickThreshold){
            Teleop.FoundationGrab(gamepad2.left_stick_y);
        }
        if(gamepad2.left_stick_y<-JoystickThreshold){
            Teleop.FoundationGrab(gamepad2.left_stick_y);
        }

        ManageArmMovement();
        ManageGripperMovement();

        double hue = colorSensor.returnHue();
        telemetry.addData("hue: ", hue);

        telemetry.update();
    }

    public void ManageDriveMovement()//Manages general drive input
    {

        //Raw D-pad stuff
        if(gamepad1.dpad_up)
        {
            Teleop.RawForwards(1);
        }
        if(gamepad1.dpad_down)
        {
            Teleop.RawForwards(-1);
        }
        if(gamepad1.dpad_right)
        {
            Teleop.RawRight(1);
        }
        if(gamepad1.dpad_left)
        {
            Teleop.RawRight(-1);
        }

        if(Jc.leftStickPower > JoystickThreshold) //Move
        {
            Teleop.chooseDirection(Jc.rightStickX, Jc.leftStickBaring, Jc.leftStickPower);
            stopping = false;
        }

        else if(Jc.rightStickX > JoystickThreshold) //Turn Right
        {
            Teleop.turnRight();
            stopping = false;
        }


        else if(Jc.rightStickX < -JoystickThreshold) //Turn Left
        {
            Teleop.turnLeft();
            stopping = false;
        }

        /*else if(gamepad1.right_trigger>0.2 && !stopping)
        {
            Teleop.brake();
            telemetry.addData("Brake: ", true);
            stopping = true;
        }*/

        else //STOP
        {
            Teleop.stopWheels();
        }
        /*else //STOP
        {
            if(!stopping)
            {
                Teleop.brake();
                stopping = true;
            }
            else
            {

            }
        }*/
    }

    public void ManageArmMovement()//Manages the Arm/Lift
    {
        if(gamepad2.right_trigger > 0.2)//turn the wheel intake on
        {
            arm.IntakeOn();
        }
        else if(gamepad2.left_trigger > 0.2)//turn the wheel intake on
        {
            arm.IntakeReverse();
        }
        else//turn the intake off
        {
            arm.IntakeOff();
        }

        if(gamepad2.right_stick_y > 0.4)//move lift up
        {
            arm.LiftUp();
        }
        else if(gamepad2.right_stick_y < -0.4)//move lift down
        {
            arm.LiftDown();
        }
        else//stop the arm from moving up or down
        {
            arm.LiftStopVertical();
        }

        if(gamepad2.left_stick_y > 0.4 && !armRetractStop.isPressed())////extend arm
        {
            arm.LiftRetract();
        }

        else if (gamepad2.left_stick_y < -0.4 )//retract arm
        {
            arm.LiftExtend();
        }

        else//stop the arm from moving left or right
        {
            arm.LiftStopHorizontal();
        }
    }

    public void ManageGripperMovement()//Manages the GripperTetrix
    {
        if(gamepad2.dpad_right)//rotate the gripper right
        {
            gripperRight();
        }
        else if(gamepad2.dpad_left)//rotate the gripper left
        {
            gripperLeft();
        }
        if(gamepad2.left_bumper)//open the gripper
        {
            closeGripper();
        }
        if(gamepad2.right_bumper)//close the gripper
        {
            openGripper();
        }
    }

    //GripperTetrix Management Methods
    public void closeGripper(){
        gripper.GripperClose();
    }
    public void openGripper(){
        gripper.GripperOpen();
    }

    public void gripperLeft() {
        gripper.GripperRotatePosition(0.05);
    }

    public void gripperRight() {
        gripper.GripperRotatePosition(-0.05);
    }
    public void gripper1(){ /*gripperPosition = 1;*/}
    public void gripper0(){/*gripperPosition=0;*/}

    public void gripperOpenLeft(){
        gripper.GripperOpenLeft();
    }
    public void gripperOpenRight(){
        gripper.GripperOpenRight();
    }
    public void gripperCloseLeft(){gripper.GripperCloseLeft();}
    public void gripperCloseRight(){gripper.GripperCloseRight();}
}