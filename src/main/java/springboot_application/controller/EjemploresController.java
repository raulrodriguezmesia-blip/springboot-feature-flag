package springboot_application.controller;


import java.util.HashMap;
import java.util.Map;



import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import springboot_application.models.Empleados;

import org.springframework.web.bind.annotation.RequestMethod;


@RestController
@RequestMapping("/api") 


public class EjemploresController {

@RequestMapping(path = "/detalles_infor", method = RequestMethod.GET)



    public Map<String, Object> detalles_infor2(){

       Empleados empleado = new Empleados("Juan", "Perez", "Calle Falsa 123", "Desarrollador", 30, 5551234, 1);

        Map<String, Object> model = new HashMap<>();

        model.put("Empleado", "Datos de empleado");
        model.put("Informacion", empleado);
       


        return model;

    }

  

}
