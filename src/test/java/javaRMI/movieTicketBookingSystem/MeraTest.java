package javaRMI.movieTicketBookingSystem;

import Replica2.Message;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.concurrent.ConcurrentHashMap;

@RunWith(JUnit4.class)
public class MeraTest {


    @Test
    public void test() throws JsonProcessingException {

        ConcurrentHashMap<Integer, Message> map = new ConcurrentHashMap<>();

        map.put(1, new Message(1, "ip", null, null, null, null, null, null, null, 0, 0));

        String asString = new ObjectMapper().writeValueAsString(map);
        System.out.println(asString);
        ConcurrentHashMap<Integer, Message> concurrentHashMap = new ObjectMapper().readValue(asString, new TypeReference<ConcurrentHashMap<Integer, Message>>() {
        });

        concurrentHashMap.forEach((o, o2) -> System.out.println("key : " + o + " val : " + o2));

        map.remove(1);
        map.put(2, new Message(1, "ip", null, null, null, null, null, null, null, 0, 0));
        map.putAll(concurrentHashMap);
        map.forEach((integer, message) -> System.err.println("RM1 Merged map to " + integer + " : " + message.toString()));

        Assert.assertEquals(concurrentHashMap.size(), map.size());
    }


}