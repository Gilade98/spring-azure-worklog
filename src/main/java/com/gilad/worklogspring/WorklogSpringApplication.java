package com.gilad.worklogspring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.*;

@SpringBootApplication
@RestController
public class WorklogSpringApplication {

    private Map<String,List<List<String>>> employeeTable = new HashMap<>();

    @GetMapping("/enter")
    public ResponseEntity<?> enter (@RequestParam("id") String ID){
        if (ID.isEmpty()){
            return ResponseEntity.badRequest().body("Missing ID");
        }

        employeeTable.putIfAbsent(ID,new ArrayList<>());
        List<List<String>> dates = employeeTable.get(ID);
        if (!dates.isEmpty() && dates.get(dates.size()-1).size()==1){ //last entry already has an enter request
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Enter request has been received already"); //return http 500
        }
        List<String> newEntry = new ArrayList<>();
        String time = LocalDateTime.now().toString();
        newEntry.add(time);
        dates.add(newEntry);

        Map<String,Object> response = new HashMap<>();
        response.put("Status","Success");
        response.put("Message","Entrance logged successfully");
        response.put("employee_id",ID);
        response.put("entrance_time",time);

        return ResponseEntity.ok(response);

    }

    @GetMapping("/exit")
    public ResponseEntity<?> exit (@RequestParam("id") String ID){
        if (ID.isEmpty()) {
            return ResponseEntity.badRequest().body("Missing ID");
        }

        List<List<String>> dates = employeeTable.getOrDefault(ID,null);
        if (dates == null || dates.get(dates.size()-1).size()==2){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("No enter request or exit request already received");
        }

        String time = LocalDateTime.now().toString();
        dates.get(dates.size()-1).add(time);

        Map<String,Object> response = new HashMap<>();
        response.put("Status","Success");
        response.put("Message","Exit logged successfully");
        response.put("employee_id",ID);
        response.put("entrance_time",time);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/info")
    public ResponseEntity<?> info (@RequestParam(value = "id", required = false) String ID){
        if (ID != null && !ID.isEmpty()) //id is provided
        {
            if (!employeeTable.containsKey(ID)){ //not found
                return ResponseEntity.notFound().build();
            }

            Map<String, Object> response = new HashMap<>();
            response.put("employee_id",ID);
            response.put("dates", employeeTable.get(ID));
            return ResponseEntity.ok(response);
        }

        else { //no id is provided
            if (employeeTable.isEmpty()) return ResponseEntity.ok(employeeTable);

            List<Map<String,Object>> response  = new ArrayList<>();
            for (Map.Entry<String,List<List<String>>> entry : employeeTable.entrySet()){
                String id = entry.getKey();
                List<List<String>> dates = entry.getValue();

                Map<String,Object> employee = new HashMap<>();
                employee.put("employee_id",id);
                employee.put("dates",dates);

                response.add(employee);
            }
            return ResponseEntity.ok(response);
        }
    }


    public static void main(String[] args) {
        SpringApplication.run(WorklogSpringApplication.class, args);
    }

}
