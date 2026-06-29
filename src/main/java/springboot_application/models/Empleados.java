package springboot_application.models;

public class Empleados {

private String nombre, apellido, direccion, puesto;
private int edad, telefono, idEmpleado;

public Empleados(String nombre, String apellido, String direccion, String puesto, int edad, int telefono,int idEmpleado) {
    this.nombre = nombre;
    this.apellido = apellido;
    this.direccion = direccion;
    this.puesto = puesto;
    this.edad = edad;
    this.telefono = telefono;
    this.idEmpleado = idEmpleado;
}

public String getNombre() {
    return nombre;
}
public void setNombre(String nombre) {
    this.nombre = nombre;
}
public String getApellido() {
    return apellido;
}
public void setApellido(String apellido) {
    this.apellido = apellido;
}
public String getDireccion() {
    return direccion;
}
public void setDireccion(String direccion) {
    this.direccion = direccion;
}
public String getPuesto() {
    return puesto;
}
public void setPuesto(String puesto) {
    this.puesto = puesto;
}
public int getEdad() {
    return edad;
}
public void setEdad(int edad) {
    this.edad = edad;
}
public int getTelefono() {
    return telefono;
}
public void setTelefono(int telefono) {
    this.telefono = telefono;
}
public int getIdEmpleado() {
    return idEmpleado;
}
public void setIdEmpleado(int idEmpleado) {
    this.idEmpleado = idEmpleado;
}



}
