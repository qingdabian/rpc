package com.crabapple.core.client.servicecenter.balance;


import java.util.List;

public interface LoadBalance {
    public String balance(List<String> addrlist);
    public void addNode(String addr);
    public void removeNode(String addr);
}
