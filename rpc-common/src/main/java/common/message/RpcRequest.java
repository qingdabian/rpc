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
public class RpcRequest implements Serializable {
    private RequestType type=RequestType.NORMAL;
    private String interfacename;
    private String methodname;
    private Object[] params;
    private Class<?>[] paramTypes;
    public static RpcRequest heartBeat(){
        return RpcRequest.builder().type(RequestType.HEARTBEAT).build();
    }
}
