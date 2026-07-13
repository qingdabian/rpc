package common.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RpcResponse implements Serializable {

    private String requestId;
    private int code;
    private String message;
    private Object data;
    private Class<?> dataType;



    public static RpcResponse success(Object data) {
        return RpcResponse.builder()
                .code(200)
                .message("success")
                .data(data)
                .dataType(data.getClass())
                .build();
    }
    public static RpcResponse success(Object data, String requestId) {
        return RpcResponse.builder()
                .code(200)
                .message("success")
                .data(data)
                .dataType(data.getClass())
                .requestId(requestId)
                .build();
    }
    public static RpcResponse error() {
        return RpcResponse.builder()
                .code(500)
                .message("error")
                .build();
    }
    public static RpcResponse error(String requestId) {
        return RpcResponse.builder()
                .code(500)
                .message("error")
                .requestId(requestId)
                .build();
    }
}
