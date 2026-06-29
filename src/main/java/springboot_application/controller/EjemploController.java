package springboot_application.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;




@Controller 
public class EjemploController {


    @GetMapping("/detalles_infor")

    public String info(java.util.Map<String, Object> model){

        
        model.put("Empleado", "Datos de empleado");
     

        return "detalles_infor";

    }
 
  

}
