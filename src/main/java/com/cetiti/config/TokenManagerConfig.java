package com.cetiti.config;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class TokenManagerConfig {
    private static final String TOKEN_1 = "guoqi/scene/auto/main/signal/command";
    private static final String TOKEN_2 = "guoqi/scene/auto/sub/signal/command";

    // 标记令牌是否被使用
    private final ConcurrentHashMap<String, AtomicBoolean> tokenPool = new ConcurrentHashMap<>();
    // 映射从 esn 到令牌
    private final ConcurrentHashMap<String, String> esnToTokenMap = new ConcurrentHashMap<>();

    public TokenManagerConfig() {
        tokenPool.put(TOKEN_1, new AtomicBoolean(false));
        tokenPool.put(TOKEN_2, new AtomicBoolean(false));
    }

    /**
     * 根据操作类型获取或释放令牌。
     *
     * @param start 如果为true，则尝试获取令牌；如果为false，则释放令牌。
     * @param esn   设备序列号，用于记录令牌的使用。
     * @return 获取或释放令牌时返回令牌字符串，如果操作失败返回null。
     */
    public String manageToken(boolean start, String esn) {
        if (start) {
            for (String token : tokenPool.keySet()) {
                // 尝试获取未被使用的令牌
                if (tokenPool.get(token).compareAndSet(false, true)) {
                    esnToTokenMap.put(esn, token);
                    return token;  // 返回分配的令牌
                }
            }
            return null;  // 没有可用令牌时返回null
        } else {
            // 释放令牌
            if (esnToTokenMap.containsKey(esn)) {
                String token = esnToTokenMap.get(esn);
                if (tokenPool.get(token).compareAndSet(true, false)) {
                    esnToTokenMap.remove(esn);
                    return token;  // 返回被释放的令牌
                }
            }
            return null;  // 释放失败或令牌不存在
        }
    }
}

