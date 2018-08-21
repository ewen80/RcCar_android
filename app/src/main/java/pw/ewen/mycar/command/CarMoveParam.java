package pw.ewen.mycar.command;

import static java.lang.Math.abs;

/**
 * 小车移动参数类
 * 方向和速度
 * 方向:
 *                  90
 *          180             0
 *                  270
 */
public class CarMoveParam {

    public static final int MIN_THROTTLE = 100; //最小油門
    public static final int MAX_THROTTLE = 230; //最大油门

    public static final int MAX_ANGLE = 50; //UI最大角度表示实际角度（angle=0或者180，对应实际舵机角度）

    private int speed;
    private int angle;

    private CarMoveThrottleEnum moveThrottle;
    private CarMoveDirectionEnum moveDirection;

    public CarMoveParam() {
    }

    public CarMoveParam(int speed, int angle) {
        this.speed = speed;
        this.angle = angle;
    }

    public int getSpeed() {
        if(speed < 0){
            return 0;
        } else if(speed > 100){
            return 100;
        } else {
            return speed;
        }
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    //小车识别的角度值(和90度或者270度的夹角)
    public int getAngle() {
        if(angle < 0){
            return 0;
        } else if(angle > MAX_ANGLE){
            return MAX_ANGLE;
        } else {
            return angle;
        }
    }

    public void setAngle(int angle) {
        this.angle = angle;
    }

    //移动方向(前进,后退)
    public CarMoveThrottleEnum getMoveThrottle() {
        return moveThrottle;
    }

    public void setMoveThrottle(CarMoveThrottleEnum moveThrottle) {
        this.moveThrottle = moveThrottle;
    }

    //移动角度(左转,右转)
    public CarMoveDirectionEnum getMoveDirection() {
        return moveDirection;
    }

    public void setMoveDirection(CarMoveDirectionEnum moveDirection) {
        this.moveDirection = moveDirection;
    }

    //从虚拟摇杆的力度到速度的转换（0-100 到 MIN_THROTTLE 到 MAX_THROTTLE 的转换）
    private int strengthToSpeedTransform(int strength){
        //检查strength是否在0-100内
        if(strength >=0 && strength <= 100){
            return ((100 - strength) * MIN_THROTTLE + MAX_THROTTLE * strength) / 100;
        } else if(strength < 0){
            return MIN_THROTTLE;
        } else {
            return MAX_THROTTLE;
        }
    }

    /**
     * 从UI的操作角度到实际舵机角度的转换
     * @param direction 摇杆方向
     * @return  比例对应实际角度值
     */
    private int angleToDirectionTransform(int direction){

        if(direction >=0 && direction < 90){
            this.moveDirection = CarMoveDirectionEnum.Right;
            this.moveThrottle = CarMoveThrottleEnum.Forward;
            return (abs(direction - 90) * MAX_ANGLE) / 90;

        } else if(direction >= 90 && direction < 180){
            this.moveDirection = CarMoveDirectionEnum.Left;
            this.moveThrottle = CarMoveThrottleEnum.Forward;
            return (abs(direction - 90) * MAX_ANGLE) / 90;

        } else if(direction >= 180 && direction < 270){
            this.moveDirection = CarMoveDirectionEnum.Left;
            this.moveThrottle = CarMoveThrottleEnum.Reverse;
            return (abs(direction - 270) * MAX_ANGLE) / 90;

        } else if(direction >= 270 && direction <= 360){
            this.moveDirection = CarMoveDirectionEnum.Right;
            this.moveThrottle = CarMoveThrottleEnum.Reverse;
            return (abs(direction - 270) * MAX_ANGLE) / 90;
        } else{
            return 0;
        }
    }

    /**
     * 摇杆数据到小车实际移动参数的变换
     * @param strength  摇杆力度
     * @param direction     摇杆方向
     */
    public void transformJoystickParam(int strength, int direction){
        this.speed = strengthToSpeedTransform(strength);
        this.angle = angleToDirectionTransform(direction);
    }
}
