package com.aicostguard.module.cache.service.impl;

import com.aicostguard.module.cache.service.SimHashService;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

@Service
public class SimHashServiceImpl implements SimHashService {

    private static final int HASH_BITS = 64;

    @Override
    public long computeSimHash(String text) {
        if (text == null || text.isBlank()) {
            return 0;
        }

        // 分词（简单按空格和标点分割，支持中文按字符分割）
        List<String> tokens = tokenize(text);

        int[] vector = new int[HASH_BITS];

        for (String token : tokens) {
            long hash = hash(token);
            for (int i = 0; i < HASH_BITS; i++) {
                if (((hash >> i) & 1) == 1) {
                    vector[i]++;
                } else {
                    vector[i]--;
                }
            }
        }

        long simHash = 0;
        for (int i = 0; i < HASH_BITS; i++) {
            if (vector[i] > 0) {
                simHash |= (1L << i);
            }
        }
        return simHash;
    }

    @Override
    public int hammingDistance(long hash1, long hash2) {
        return Long.bitCount(hash1 ^ hash2);
    }

    private List<String> tokenize(String text) {
        List<String> tokens = new ArrayList<>();
        // 按非字母数字和非中文字符分割
        StringBuilder sb = new StringBuilder();
        for (char c : text.toCharArray()) {
            if (Character.isLetterOrDigit(c) || Character.UnicodeScript.of(c) == Character.UnicodeScript.HAN) {
                sb.append(c);
            } else {
                if (sb.length() > 0) {
                    tokens.add(sb.toString());
                    sb.setLength(0);
                }
            }
        }
        if (sb.length() > 0) {
            tokens.add(sb.toString());
        }

        // 对中文文本，追加 bigram
        String joined = String.join("", tokens);
        for (int i = 0; i < joined.length() - 1; i++) {
            char c1 = joined.charAt(i);
            char c2 = joined.charAt(i + 1);
            if (Character.UnicodeScript.of(c1) == Character.UnicodeScript.HAN
                    || Character.UnicodeScript.of(c2) == Character.UnicodeScript.HAN) {
                tokens.add("" + c1 + c2);
            }
        }

        return tokens;
    }

    private long hash(String token) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(token.getBytes(StandardCharsets.UTF_8));
            return new BigInteger(1, digest).longValue();
        } catch (NoSuchAlgorithmException e) {
            // fallback to simple hash
            return token.hashCode();
        }
    }
}
