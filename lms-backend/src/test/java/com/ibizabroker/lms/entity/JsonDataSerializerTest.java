package com.ibizabroker.lms.entity;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

class JsonDataSerializerTest {

    @Test
    void testSerialize() throws IOException {
        // 创建 JsonDataSerializer 实例
        JsonDataSerializer serializer = new JsonDataSerializer();

        // 创建模拟的 JsonGenerator
        JsonGenerator jsonGenerator = Mockito.mock(JsonGenerator.class);
        SerializerProvider serializerProvider = Mockito.mock(SerializerProvider.class);

        // 测试日期
        Date testDate = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
        String expectedDate = simpleDateFormat.format(testDate);

        // 调用序列化方法
        serializer.serialize(testDate, jsonGenerator, serializerProvider);

        // 验证 writeString 被调用并传递了正确的日期格式
        verify(jsonGenerator, times(1)).writeString(expectedDate);
    }
}
