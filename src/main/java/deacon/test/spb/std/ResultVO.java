package deacon.test.spb.std;

import lombok.Getter;

@Getter
public class ResultVO<T> {
    /**
     * 状态码，比如1000代表响应成功
     */
    private int code;
    /**
     * 响应信息，用来说明响应情况
     */
    private String msg;
    /**
     * 响应的具体数据
     */
    private T data;

    // 返回具体数据
    public ResultVO(T data) {
        this(ResultCode.SUCCESS, data);
    }

    // 返回对应的枚举响应码和响应信息，以及具体的数据。
    public ResultVO(ResultCode resultCode, T data) {
        this.code = resultCode.getCode();
        this.msg = resultCode.getMsg();
        this.data = data;
    }

    // 返回对应的枚举响应码和响应信息。
    public ResultVO(ResultCode resultCode) {
        this.code = resultCode.getCode();
        this.msg = resultCode.getMsg();
    }

}
