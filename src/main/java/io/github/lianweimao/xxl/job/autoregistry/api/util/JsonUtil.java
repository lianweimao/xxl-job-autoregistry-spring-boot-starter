package io.github.lianweimao.xxl.job.autoregistry.api.util;

import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonUtil {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * 将json字符串转换成JsonNode
     * @param json
     * @return
     */
    public static JsonNode parse(String json) {
        try {
            return OBJECT_MAPPER.readTree(json);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 将jsonNode转换成bean
     * @param jsonNode
     * @param typeReference
     * @return
     * @param <T>
     */
    public static <T> T toBean(TreeNode jsonNode, TypeReference<T> typeReference){
        try {
            return OBJECT_MAPPER.convertValue(jsonNode, typeReference);
        } catch (Exception e) {
            return null;
        }
    }
}
