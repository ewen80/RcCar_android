package pw.ewen.mycar;

/**
 * 小车移动参数类
 * 方向和速度
 */
class CarMoveParam {

    private int speed;
    private int angle;

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

    public int getAngle() {
        if(angle < 0){
            return 0;
        } else if(angle > 360){
            return 360;
        } else {
            return angle;
        }
    }

    public void setAngle(int angle) {
        this.angle = angle;
    }
}
