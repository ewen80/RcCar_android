package pw.ewen.mycar.command;

public class CarCommand {

    private CarCommandTypeEnum commandType = CarCommandTypeEnum.Other;
    private CarMoveParam moveParam;
    private CarCameraCommandEnum cameraCommand;


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

    public CarCameraCommandEnum getCameraCommand() {
        return cameraCommand;
    }

    public void setCameraCommand(CarCameraCommandEnum cameraCommand) {
        this.cameraCommand = cameraCommand;
    }

    @Override
    public String toString(){
        if(this.commandType != null){
            switch (commandType){
                case Drive:
                    //前进
                    return getDriveCommandStr(this.moveParam);
                case Stop:
                    //制动
                    return getStopCommandStr();
                case Find:
                    //确认服务器
                    return getFindCommandStr();
                case Camera:
                    //摄像头
                    return getCamCommandStr();
                default:
                    return "";
            }

        } else {
            return "";
        }
    }

    //行驶命令
    private String getDriveCommandStr(CarMoveParam moveParam){

        String throttleCommand = "";
        String directionCommand = "";

        String speed = String.valueOf(moveParam.getSpeed());
        String angle = String.valueOf(moveParam.getAngle());

        if(moveParam.getMoveThrottle() == CarMoveThrottleEnum.Forward){
            //直行
            throttleCommand = "F" + speed  + "|";
            if(moveParam.getMoveDirection() == CarMoveDirectionEnum.Left){
                //左转
                directionCommand = "L" + angle + "|";
            } else if(moveParam.getMoveDirection() == CarMoveDirectionEnum.Right){
                //右转
                directionCommand = "R" + angle + "|";
            }

        } else if(moveParam.getMoveThrottle() == CarMoveThrottleEnum.Reverse){
            //倒车
            throttleCommand = "B" + speed + "|";
            if(moveParam.getMoveDirection() == CarMoveDirectionEnum.Left){
                //左转
                directionCommand = "L" + angle + "|";
            } else if(moveParam.getMoveDirection() == CarMoveDirectionEnum.Right){
                //右转
                directionCommand = "R" + angle + "|";
            }
        }

        return  throttleCommand + directionCommand;
    }

    //制动命令
    private String getStopCommandStr(){
        return "S0|";
    }

    //确认服务器命令
    private String getFindCommandStr(){
        return "W0|";
    }

    //摄像头命令
    private String getCamCommandStr(){
        switch (this.cameraCommand){
            case On:
                return "V|1";
            case Off:
                return "V|0";
            case Restart:
                return "V|2";
            default:
                return "";
        }
    }

}
