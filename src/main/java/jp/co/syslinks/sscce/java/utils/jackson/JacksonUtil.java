package jp.co.syslinks.sscce.java.utils.jackson;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

public class JacksonUtil {

    public static void main(String... args) throws IOException {
        final ObjectMapper objectMapper = new ObjectMapper();
        {
            // com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException の回避策
            objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            // or @JsonIgnoreProperties(ignoreUnknown=true)
        }
        {
            // java8 localdatetime
            final SimpleModule module = new SimpleModule();
            module.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer());
            module.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer());
            objectMapper.registerModule(module);
        }
        final String jsonStr = jsonStr();
        // 配列
        array(objectMapper, jsonStr);
        // List1
        arrayConvertListUsingArraysAsList(objectMapper, jsonStr);
        // List2
        listUsingTypeReference(objectMapper, jsonStr);
    }

    static public void array(ObjectMapper objectMapper, String ARRAY_JSON) throws IOException {
        Todo[] todoArray = objectMapper.readValue(ARRAY_JSON, Todo[].class);
        for (Todo todo : todoArray) {
            System.out.println(todo.getTitle());
            System.out.println(todo.getDatetime());
            System.out.println(todo._final);
        }
    }

    static public void arrayConvertListUsingArraysAsList(ObjectMapper objectMapper, String ARRAY_JSON) throws IOException {
        List<Todo> todoList = Arrays.asList(objectMapper.readValue(ARRAY_JSON, Todo[].class));
        for (Todo todo : todoList) {
            System.out.println(todo.getTitle());
            System.out.println(todo.getDatetime());
            System.out.println(todo._final);
        }
    }

    static public void listUsingTypeReference(ObjectMapper objectMapper, String ARRAY_JSON) throws IOException {
        List<Todo> todoList = objectMapper.readValue(ARRAY_JSON, new TypeReference<List<Todo>>() {
        });
        for (Todo todo : todoList) {
            System.out.println(todo.getTitle());
            System.out.println(todo.getDatetime());
            System.out.println(todo._final);
        }
    }

    static private String jsonStr() {
        return new StringBuilder() //
                .append("[") //
                .append("  {") //
                .append("    \"id\" : 1,") //
                .append("    \"final\" : 2,") // java keyword
                .append("    \"title\" : \"Jsckson勉強会\",") //
                .append("    \"datetime\" : \"2020-01-01 01:23:45\"") //
                .append("  }") //
                .append("  ,") //
                .append("  {") //
                .append("    \"id\" : 3,") //
                .append("    \"final\" : 4,") // java keyword
                .append("    \"title\" : \"飲み会\",") //
                .append("    \"datetime\" : \"2020-02-03 01:23:45\"") //
                .append("  }") //
                .append("]").toString();
    }

    static class Todo {
        private String title;
        private LocalDateTime datetime;
        @JsonProperty("final")
        public int _final;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public LocalDateTime getDatetime() {
            return datetime;
        }

        public void setDatetime(LocalDateTime datetime) {
            this.datetime = datetime;
        }
    }

}
