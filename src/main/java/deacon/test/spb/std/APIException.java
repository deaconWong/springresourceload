package deacon.test.spb.std;

import lombok.Getter;

@Getter //只要getter方法，无需setter
public class APIException extends RuntimeException {
    /**
     *
     */
    private static final long serialVersionUID = 9050172752857347251L;
    private final int code;
    private final String msg;

    public APIException() {
        this(1001, "接口错误");
    }

    public APIException(String msg) {
        this(1001, msg);
    }

    public APIException(int code, String msg) {
        super(msg);
        this.code = code;
        this.msg = msg;
    }
}
