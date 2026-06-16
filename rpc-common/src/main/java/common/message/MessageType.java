package common.message;


public enum MessageType {
    REQUEST(0),
    RESPONSE(1);
    private int type;

    MessageType(int type){
        this.type=type;
    }
    public int getType(){
        return type;
    }
}
