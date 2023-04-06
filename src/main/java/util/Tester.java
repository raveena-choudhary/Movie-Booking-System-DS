package util;

import java.util.*;
import java.util.stream.IntStream;

public class Tester {

    public static void main(String[] args) {
        Map<Integer,List<Employee>> eMap = new HashMap<>();
        List<Employee> employeeList = new ArrayList<>();
        eMap.put(1,employeeList);
        for(int i=0; i<10; i++){
            employeeList.add(new Employee("emp"+i));
        }
        eMap.entrySet().stream().forEach(System.out::println);
        ListIterator<Employee> itr = employeeList.listIterator();
        while(itr.hasNext()){
            Employee currentEmployee = itr.next();
            if(currentEmployee.getName().equals("emp5")){
                currentEmployee.setName("employeeNameUpdated");
            }
        }
        System.out.println("Employee name updated! >>");
        eMap.entrySet().stream().forEach(System.out::println);
    }



}
class Employee{
    private String name;
    public Employee(String name){
        this.name=name;
    }
    public String getName(){
        return name;
    }
    public void setName(String name){
        this.name=name;
    }

    @Override
    public String toString() {
        return "Employee{" +
                "name='" + name + '\'' +
                '}';
    }
}
