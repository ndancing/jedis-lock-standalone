package com.github.example.spring.jedis.lock;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

final class ScriptLoader {

    private ScriptLoader() {}

    static String load(String path) throws IOException {
        StringBuilder sb = new StringBuilder();

        InputStream stream = ScriptLoader.class.getClassLoader().getResourceAsStream(path);
        try (BufferedReader br = new BufferedReader(new InputStreamReader(stream))){
            String str;
            while ((str = br.readLine()) != null) {
                sb.append(str).append(System.lineSeparator());
            }
        }
        return sb.toString();
    }

}
