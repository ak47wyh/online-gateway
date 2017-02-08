package com.iboxpay.settlement.gateway.common.util;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.iboxpay.settlement.gateway.common.inout.payment.PaymentCustomerResult;

public class JsonUtil {

    private static final DefaultPrettyPrinter mPrettyPrinter = new DefaultPrettyPrinter("\n");

    /**
     * 输出属性换行
     * @param o
     * @return
     * @throws IOException
     */
    public static String toJson(Object o) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            StringWriter sw = new StringWriter();
            JsonGenerator gen = new JsonFactory().createJsonGenerator(sw);
            //gen.setPrettyPrinter(mPrettyPrinter);
            mapper.writeValue(gen, o);
            gen.close();
            return sw.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 不换行输出
     * @param o
     * @return
     * @throws IOException
     */
    public static String toJsonInLine(Object o) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            StringWriter sw = new StringWriter();
            JsonGenerator gen = new JsonFactory().createJsonGenerator(sw);
            mapper.writeValue(gen, o);
            gen.close();
            return sw.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Object jsonToObject(String jsonStr, String encoding, Class<?> beanClass) throws JsonParseException, JsonMappingException, IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(jsonStr.getBytes(encoding), beanClass);
    }

    /**
     * 解决Json字符串出现单引号'时候De映射问题Single quotes
     * @param jsonStr
     * @param encoding
     * @param beanClass
     * @throws JsonParseException
     * @throws JsonMappingException
     * @throws IOException
     * @return
     */
    public static Object jsonToObjectSingleQuotes(String jsonStr, String encoding, Class<?> beanClass) throws JsonParseException, JsonMappingException, IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        return objectMapper.readValue(jsonStr.getBytes(encoding), beanClass);
    }
    
    public static Map<String, Object> parseJSON2Map(String jsonStr)
			throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper objectMapper = new ObjectMapper();
		return objectMapper.readValue(jsonStr, Map.class);
	}
    
	//过滤属性  
	public static String toJson(Object obj, String... filterFields) throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		//        mapper.setSerializationConfig(mapper.getSerializationConfig().withSerializationInclusion( 
		//                JsonSerialize.Inclusion.NON_NULL)); 

		FilterProvider filters = new SimpleFilterProvider().addFilter(obj.getClass().getName(), SimpleBeanPropertyFilter.serializeAllExcept(filterFields));
		mapper.setFilters(filters);
		mapper.setAnnotationIntrospector(new JacksonAnnotationIntrospector() {
			@Override
			public Object findFilterId(AnnotatedClass ac) {
				return ac.getName();
			}
		});

		return mapper.writeValueAsString(obj);
	}
    
    public static void main(String[] args) throws JsonParseException, JsonMappingException, IOException {
    	PaymentCustomerResult PaymentCustomerResult = new PaymentCustomerResult();
    	PaymentCustomerResult.setAccName("aaa");
    	PaymentCustomerResult.setAccNo("adffds");
    	String json = toJson(PaymentCustomerResult, "accNo");
    	System.out.println(json);
	}
}
