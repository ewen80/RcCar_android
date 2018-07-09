package pw.ewen.mycar;

import java.net.SocketException;

class CarCommand {

    public static final int MIN_THROTTLE = 100; //最小油門
    public static final int MAX_THROTTLE = 230; //最大油门

    public static final int MAX_ANGLE = 50; //UI最大角度表示实际角度（angle=0或者180，对应实际舵机几度）

    private CarCommandTypeEnum commandType = CarCommandTypeEnum.Other;
    private CarMoveParam moveParam;


    public CarCommand() {
    }

    public CarCommandTypeEnum getCommandType() {
        return commandType;
    }

    public void setCommandType(CarCommandTypeEnum commandType) {
        this.commandType = commandType;
    }

    public CarMoveParam getMoveParam() {
        return moveParam;
    }

    public void setMoveParam(CarMoveParam moveParam) {
        this.moveParam = moveParam;
        if(moveParam.getSpeed() > 0){
            this.commandType = CarCommandTypeEnum.Drive;
        } else{
            this.commandType = CarCommandTypeEnum.Stop;
        }
    }

    //从虚拟摇杆的力度到速度的转换（0-100 到 MIN_THROTTLE 到 MAX_THROTTLE 的转换）
    private int strenthToSpeedTransform(int strength){
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
     * @param angle 相对角度即和90度（270度）的夹角
     * @return  比例对应实际角度值
     */
    private int angleToDirectionTransform(int angle){
        if(angle >=0 && angle <= 90){
            return (angle * MAX_ANGLE) / 90;
        }else{
            return 0;
        }
    }


    @Override
    public String toString(){
        if(this.commandType != null){
            switch (commandType){
                case Drive:
                    //前进
                    return getDriveCommandStr();
                case Stop:
                    //制动
                    return getStopCommandStr();
                case Find:
                    //确认服务器
                    return getFindCommandSTr();
                default:
                    return "";
            }

        } else {
            return "";
        }
    }

    private String getDriveCommandStr(){
        int speed = this.moveParam.getSpeed();
        int angle = this.moveParam.getAngle();
        int tranformedSpeed = strenthToSpeedTransform(speed);

        String throttleCommand = "", directionCommand = "";

        if(angle > 0 && angle <= 90){
            //直行右转
            throttleCommand = "F" + String.valueOf(tranformedSpeed) + "|";
            directionCommand = "R" + String.valueOf(angleToDirectionTransform(90 - angle)) + "|";
        } else if(angle > 90 && angle <= 180){
            //直行左转
            throttleCommand = "F" + String.valueOf(tranformedSpeed) + "|";
            directionCommand = "L" + String.valueOf(angleToDirectionTransform(angle - 90)) + "|";
        } else if(angle > 180 && angle <= 270){
            //后退左转
            throttleCommand = "B" + String.valueOf(tranformedSpeed) + "|";
            directionCommand = "L" + String.valueOf(angleToDirectionTransform(270- angle)) + "|";
        } else if(angle > 270 && angle <= 360){
            //后退右转
            throttleCommand = "B" + String.valueOf(tranformedSpeed) + "|";
            directionCommand = "R" + String.valueOf(angleToDirectionTransform(angle - 270)) + "|";
        }
        return  throttleCommand + directionCommand;
    }

    //制动
    private String getStopCommandStr(){
        return "S0|";
    }

    //确认服务器
    private String getFindCommandSTr(){
        return "W0|";
    }

}
